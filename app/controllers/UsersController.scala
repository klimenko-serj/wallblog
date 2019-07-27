
package controllers

import javax.inject._
import play.api._
import play.api.mvc._

import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._

import scala.concurrent.{ ExecutionContext, Future }

import com.github.t3hnar.bcrypt._

import models.{ User, UserLogin }
import storage.UserStorage
import security.UserIdKey

trait WithUserJSON {
  implicit val userReads: Reads[User] = (
    Reads[String](_ => JsSuccess[String]("")) and
    (JsPath \ "username").read[String](minLength[String](2)) and
    (JsPath \ "password").read[String](minLength[String](8)) and
    (JsPath \ "firstName").read[String](minLength[String](1)) and
    (JsPath \ "lastName").read[String](minLength[String](1))
  )(User.apply _)

  implicit val userLoginReads: Reads[UserLogin] = (
    (JsPath \ "username").read[String](minLength[String](2)) and
    (JsPath \ "password").read[String](minLength[String](8))
  )(UserLogin.apply _)

  implicit val userWrites = new Writes[User] {
    def writes(user: User) = Json.obj(
      "id" -> user.id,
      "username" -> user.username,
      "firstName" -> user.firstName,
      "lastName" -> user.lastName
    )
  }
}

@Singleton
class UsersController @Inject()(cc: ControllerComponents, usrStorage: UserStorage) extends AbstractController(cc) with WithUserJSON {

  implicit def ec: ExecutionContext = cc.executionContext

  def signUp() = Action.async(parse.json) {
    _.body.validate[User].map { user =>
      val usr: User = user.copy(password = user.password.bcrypt)
      usrStorage.addUser(usr).map { _ match {
        case None => Forbidden("User exists")
        case Some(_) => Created
      }}
    }.getOrElse(Future.successful(BadRequest("Invalid User format")))
  }

  def signIn() = Action.async(parse.json) { implicit request =>
    request.body.validate[UserLogin].map { userLogin =>
      usrStorage.findUserByName(userLogin.username).map { _ match {
        case None => NotFound
        case Some(u) => if (userLogin.password.isBcrypted(u.password)){
           Ok("Logged in!").withSession(request.session + (UserIdKey -> u.id))
           } else Forbidden("Wrong password")
      }}
    }.getOrElse(Future.successful(BadRequest("Invalid User format")))
  }

  def signOut() = Action { implicit request: Request[AnyContent] =>
    Ok("signed out").withSession(request.session - UserIdKey)
  }

  def currentUser() = Action.async { implicit request: Request[AnyContent] =>
    request.session.get(UserIdKey).map({ uid:String =>
      usrStorage.getUser(uid) map { _ match {
        case None => Ok("")
        case Some(u) => Ok(Json.toJson(u))
    }}
    }).getOrElse(Future.successful(Ok("")))
  }
}
