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
                timestamp <- bson.getAs[Long]("timestamp")
                author    <- bson.getAs[List[User]]("author") flatMap (_.headOption)
                likes     <- bson.getAs[List[User]]("likes")
            } yield new BlogPost(id.stringify, title, content, timestamp.toInt, author, likes)

            opt.get
        }
    }

    implicit object BlogPostWriter extends BSONDocumentWriter[BlogPost] {
        def write(post: BlogPost): BSONDocument =
          BSONDocument(
              "title" -> post.title,
              "content" -> post.content,
              "timestamp" -> post.timestamp,
              "authorID" -> BSONObjectID.parse(post.author.id).get,
              "likesIDs" -> post.likes.map(lu => BSONObjectID.parse(lu.id).get)
          )
      }
}

class BlogPostStorage @Inject()(implicit ec: ExecutionContext, reactiveMongoApi: ReactiveMongoApi) extends WithBlogPostReaderAndWriter{

    def blogPostCollection: Future[BSONCollection] = reactiveMongoApi.database.map(_.collection[BSONCollection]("posts"))

    def addPost(post: BlogPost): Future[WriteResult] = blogPostCollection.flatMap(_.insert(ordered = false).one(post))

    def getAll(): Future[Seq[BlogPost]] = 
        blogPostCollection flatMap { bpCol =>
            import bpCol.BatchCommands.AggregationFramework.{ Lookup, Sort, Descending }

            bpCol.aggregatorContext[BlogPost](
                Lookup("users", "authorID", "_id", "author"),
                List(
                 Lookup("users", "likesIDs", "_id", "likes"),
                 Sort(Descending("timestamp")))
            ).prepared.cursor
            .collect[Seq](-1, Cursor.FailOnError[Seq[BlogPost]]()) // ??? limit
        }

    def like(postID: String, userID: String): Future[Option[BlogPost]] = {

        val bsonPostID = BSONObjectID.parse(postID).get
        val bsonUserID = BSONObjectID.parse(userID).get

        blogPostCollection.flatMap { bpCol => 
            import bpCol.BatchCommands.AggregationFramework.{ Lookup, Match }
            
            for {
                upd <- bpCol.update(ordered=false).one(
                    BSONDocument("_id" -> bsonPostID),
                    BSONDocument("$addToSet" -> BSONDocument("likesIDs" -> bsonUserID)))
                
                res <- bpCol.aggregateWith[BlogPost](){ _ =>
                    (Match(BSONDocument("_id" -> bsonPostID)),
                     List(
                      Lookup("users", "authorID", "_id", "author"),
                      Lookup("users", "likesIDs", "_id", "likes")))
                }.headOption
            } yield res
        }
    }
}