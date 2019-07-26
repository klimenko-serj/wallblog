package controllers

import javax.inject._
import play.api._
import play.api.mvc._

@Singleton
class PagesController @Inject()(cc: ControllerComponents) extends AbstractController(cc) {

  def index() = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.index())
  }

  def signUp() = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.signup())
  }

  def signIn() = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.signin())
  }
}
