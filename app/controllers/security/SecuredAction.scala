package controllers.security

import javax.inject._
import play.api._
import play.api.mvc._
import play.api.mvc.Results._
import scala.concurrent.{ ExecutionContext, Future }

import models.User
import storage.UserStorage



case class UserRequest[A](val currentUser: User, val request: Request[A])
  extends WrappedRequest[A](request)

class SecuredAction @Inject()(val parser: BodyParsers.Default)
  (implicit val executionContext: ExecutionContext, usrStorage: UserStorage)
    extends ActionBuilder[UserRequest, AnyContent] {


    override def invokeBlock[A](request: Request[A], block: (UserRequest[A]) => Future[Result]): Future[Result] = {
        request.session.get(UserIdKey).map { uID =>
            usrStorage.getUser(uID).flatMap { _ match {
                case Some(u:User) => block(UserRequest(u, request))
                case None => Future.successful(Unauthorized("Unauthorized access!"))
            }}
        }.getOrElse(Future.successful(Unauthorized("Unauthorized access!")))
    }
}
