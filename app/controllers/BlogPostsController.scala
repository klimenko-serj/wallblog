package controllers

import javax.inject._
import play.api._
import play.api.mvc._

import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._


import scala.concurrent.{ ExecutionContext, Future }

import models.{ User, BlogPost, AddBlogPost, BlogPostLike }
import storage.BlogPostStorage
import security.{ SecuredAction, UserRequest }

trait WithPostJSON extends WithUserJSON {
  
  implicit val blogPostLikeReads: Reads[BlogPostLike] = 
    (JsPath \ "postID").read[String].map(pid => BlogPostLike(pid))

  implicit val addPostReads: Reads[AddBlogPost] = (
    (JsPath \ "title").read[String](minLength[String](1)) and
    (JsPath \ "content").read[String](minLength[String](1))
  )(AddBlogPost.apply _)
  
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
class BlogPostsController @Inject()(implicit cc: ControllerComponents, postsStorage: BlogPostStorage, securedAction: SecuredAction)
 extends AbstractController(cc) with WithPostJSON {

  implicit def ec: ExecutionContext = cc.executionContext

  def list() = Action.async {
    postsStorage.getAll().map { lst =>
      Ok(Json.toJson(lst))
    }
  }

  def add() = securedAction.async(parse.json) { implicit request =>
    request.body.validate[AddBlogPost].map { newPost => 
      val timestamp: Long = System.currentTimeMillis / 1000
      val bPost = BlogPost("", newPost.title, newPost.content, timestamp, request.currentUser, List())
      postsStorage.addPost(bPost).map { _ => Ok(Json.toJson(bPost)) }
    }.getOrElse(Future.successful(BadRequest("Invalid BlogPost format")))
  }

  def like() = securedAction.async(parse.json) { implicit request =>
    request.body.validate[BlogPostLike].map { postLikeID => 
      postsStorage.like(postLikeID.postID, request.currentUser.id).map{ _ match {
        case None => InternalServerError
        case Some(bp) => Ok(Json.toJson(bp))
      }}
    }.getOrElse(Future.successful(BadRequest("Invalid LikeRequest format")))
  }
}
