package models

case class User(
    id: String,
    username: String,
    password: String,
    firstName: String,
    lastName: String
)

case class UserLogin(
    username: String,
    password: String
)