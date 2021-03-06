package models

case class BlogPost(
    id: String,
    title: String,
    content: String,
    timestamp: Long,
    author: User,
    likes: List[User]
)

case class AddBlogPost(
    title: String,
    content: String
)

case class BlogPostLike(postID: String)
