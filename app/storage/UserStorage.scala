package storage

import models._

import javax.inject.Inject

import scala.concurrent.{ ExecutionContext, Future }

import play.modules.reactivemongo.ReactiveMongoApi

import reactivemongo.bson._
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.api.commands.WriteResult

trait WithUserReaderAndWriter {
    implicit object UserReader extends BSONDocumentReader[User] {
        def read(bson: BSONDocument): User = {
            val opt: Option[User] = for {
                id        <- bson.getAs[String]("_id") // ???
                username  <- bson.getAs[String]("username")
                password  <- bson.getAs[String]("password")
                firstName <- bson.getAs[String]("firstName")
                lastName  <- bson.getAs[String]("lastName")
            } yield new User(id, username, password, firstName, lastName)

            opt.get
        }
    }

    implicit object UserWriter extends BSONDocumentWriter[User] {
        def write(user: User): BSONDocument =
          BSONDocument(
              // ??? "_id" -> BSONObjectID(user.id), how to convert string to BSONObjectID?
              "username" -> user.username,
              "password" -> user.password,
              "firstName" -> user.firstName,
              "lastName" -> user.lastName
          )
    }
}

class UserStorage @Inject()(implicit ec: ExecutionContext, reactiveMongoApi: ReactiveMongoApi) extends WithUserReaderAndWriter {
    def usersCollection: Future[BSONCollection] = reactiveMongoApi.database.map(_.collection[BSONCollection]("users"))

    def addUser(user: User): Future[WriteResult] = usersCollection.flatMap(_.insert(ordered = false).one(user))

    def getUser(id: BSONObjectID): Future[Option[User]] = { // ??? id: String
        usersCollection.flatMap(_.find(
            selector = BSONDocument("_id" -> id),
            projection = Option.empty[BSONDocument])
            .one[User])
    }

    //...
    // update, delete, getAll... etc.
}