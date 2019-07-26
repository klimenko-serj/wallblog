package controllers

import javax.inject._
import play.api._
import play.api.mvc._

import play.api.libs.json._

import scala.concurrent.{ ExecutionContext, Future }

import models.{ User, BlogPost }
import storage.BlogPostStorage

trait WithPostJSON extends WithUserJSON {
  implicit val postWrites = new Writes[BlogPost] {
    def writes(post: BlogPost) = Json.obj(
      "id" -> post.id,
      "title" -> post.title ,
      "content" -> post.content,
      "author" -> post.author,
      "likes" -> post.likes
    )
  }
}

@Singleton
class BlogPostsController @Inject()(implicit cc: ControllerComponents, postsStorage: BlogPostStorage) extends AbstractController(cc) with WithPostJSON {

  implicit def ec: ExecutionContext = cc.executionContext

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
