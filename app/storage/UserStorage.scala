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
                id        <- bson.getAs[BSONObjectID]("_id")
                username  <- bson.getAs[String]("username")
                password  <- bson.getAs[String]("password")
                firstName <- bson.getAs[String]("firstName")
                lastName  <- bson.getAs[String]("lastName")
            } yield new User(id.stringify, username, password, firstName, lastName)

            opt.get
        }
    }

    implicit object UserWriter extends BSONDocumentWriter[User] {
        def write(user: User): BSONDocument = {
          val d = BSONDocument(
              "username" -> user.username,
              "password" -> user.password,
              "firstName" -> user.firstName,
              "lastName" -> user.lastName
          )

          if(user.id == "") d else d ++ (BSONDocument("_id" -> BSONObjectID.parse(user.id).get))
        }
    }
}

class UserStorage @Inject()(implicit ec: ExecutionContext, reactiveMongoApi: ReactiveMongoApi) extends WithUserReaderAndWriter {
    def usersCollection: Future[BSONCollection] = reactiveMongoApi.database.map(_.collection[BSONCollection]("users"))

    def addUser(user: User): Future[Option[Any]] = 
        for {
            uc: BSONCollection <- usersCollection
            exsts: Option[User] <- uc.find(
                        selector  = BSONDocument("username" -> user.username),
                        projection = Option.empty[BSONDocument]
                    ).one[User]
            res: Option[Any] <- exsts match {
                case None    => uc.insert(ordered = false).one(user).map(Some(_))
                case Some(_) => Future.successful(None)
            }
        } yield res
    

    def getUser(id: String): Future[Option[User]] = { // ??? id: String
        usersCollection.flatMap(_.find(
            selector = BSONDocument("_id" -> BSONObjectID.parse(id).get),
            projection = Option.empty[BSONDocument])
            .one[User])
    }

    def findUserByName(username: String): Future[Option[User]] = {
        usersCollection.flatMap(_.find(
            selector  = BSONDocument("username" -> username),
            projection = Option.empty[BSONDocument]
        ).one[User])
    }

    //...
    // update, delete, getAll... etc.
}