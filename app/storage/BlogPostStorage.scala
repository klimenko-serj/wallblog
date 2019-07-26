package storage

import models._

import javax.inject.Inject

import scala.concurrent.{ ExecutionContext, Future }

import play.modules.reactivemongo.ReactiveMongoApi

import reactivemongo.bson._
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.api.commands.WriteResult
import reactivemongo.api.{ Cursor, ReadPreference }

import play.api.Logging

trait WithBlogPostReaderAndWriter extends WithUserReaderAndWriter{

    implicit object BlogPostReader extends BSONDocumentReader[BlogPost] {
        def read(bson: BSONDocument): BlogPost = {

            val opt: Option[BlogPost] = for {
                id        <- bson.getAs[BSONObjectID]("_id")
                title     <- bson.getAs[String]("title")
                content   <- bson.getAs[String]("content")
                timestamp <- bson.getAs[Double]("timestamp")
                author    <- bson.getAs[List[User]]("author") flatMap (_.headOption)
                likes     <- bson.getAs[List[User]]("likes")
            } yield new BlogPost(id.stringify, title, content, timestamp.toInt, author, likes)

            opt.get
        }
    }

    implicit object BlogPostWriter extends BSONDocumentWriter[BlogPost] {
        def write(post: BlogPost): BSONDocument =
          BSONDocument(
              // ??? "_id" -> BSONObjectID(user.id), how to convert string to BSONObjectID?
              "title" -> post.title,
              "content" -> post.content,
              "timestamp" -> post.timestamp,
              "authorID" -> post.author.id,
              "likesIDs" -> post.likes.map(_.id)
          )
      }
}

class BlogPostStorage @Inject()(implicit ec: ExecutionContext, reactiveMongoApi: ReactiveMongoApi) extends WithBlogPostReaderAndWriter{

    def blogPostCollection: Future[BSONCollection] = reactiveMongoApi.database.map(_.collection[BSONCollection]("posts"))

    def addPost(post: BlogPost): Future[WriteResult] = blogPostCollection.flatMap(_.insert(ordered = false).one(post))

    def getAll(): Future[Seq[BlogPost]] = 
        blogPostCollection flatMap { bpCol =>
            import bpCol.BatchCommands.AggregationFramework.Lookup

            bpCol.aggregatorContext[BlogPost](
                Lookup("users", "authorID", "_id", "author"),
                List(Lookup("users", "likesIDs", "_id", "likes"))
            ).prepared.cursor
            .collect[Seq](-1, Cursor.FailOnError[Seq[BlogPost]]()) // ??? limit
        }
}