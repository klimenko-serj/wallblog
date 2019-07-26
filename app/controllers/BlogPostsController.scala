package controllers

import javax.inject._
import play.api._
import play.api.mvc._

import play.api.libs.json._

import scala.concurrent.{ ExecutionContext, Future }

import models.{ User, BlogPost }
import storage.BlogPostStorage

@Singleton
class BlogPostsController @Inject()(implicit cc: ControllerComponents, postsStorage: BlogPostStorage) extends AbstractController(cc) {

  implicit def ec: ExecutionContext = cc.executionContext

  implicit val postWrites = new Writes[BlogPost] {
    def writes(post: BlogPost) = Json.obj(
      "id" -> post.id,
      "title" -> post.title ,
      "content" -> post.content
    )
  }

  def list() = Action.async {
   postsStorage.getAll().map { lst =>
      Ok(Json.toJson(lst))
   } 
  }

  def add() = Action { implicit request: Request[AnyContent] =>
   NotImplemented 
  }

  def like() = Action { implicit request: Request[AnyContent] =>
   NotImplemented 
  }
}
