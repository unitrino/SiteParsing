package models

import slick.driver.SQLiteDriver.api._


case class TagQuery(id: Option[Int], htmlTag:String, userId: Int)

object TagQuery extends ((Option[Int], String, Int) => TagQuery) {
  def apply(htmlTag:String, userId:Int):TagQuery = {
    TagQuery(None, htmlTag, userId)
  }
  def unpick(u: TagQuery): Option[( Option[Int], String, Int )]  = {
    Some(u.id, u.htmlTag, u.userId)
  }
}

class TagsQueries(tag: Tag) extends Table[TagQuery](tag, "SITE_TAGS") {
  def id = column[Int]("ID", O.PrimaryKey, O.AutoInc)
  def htmlTag = column[String]("TAG_VALUE")
  def userId = column[Int]("USER_ID")
  def usersOwners = foreignKey("USERS_FK", userId, TableQuery[UsersQueries])(_.id, onUpdate=ForeignKeyAction.Restrict, onDelete=ForeignKeyAction.Cascade)
  def * = (id.?, htmlTag, userId) <> (TagQuery.tupled, TagQuery.unapply)
}


case class UserQuery(id: Option[Int], siteUrl:String)

object UserQuery extends ((Option[Int], String) => UserQuery) {
  def apply(siteUrl:String): UserQuery = {
    UserQuery(None, siteUrl)
  }
  def unpick(u: UserQuery): Option[( Option[Int], String )]  = {
    Some(u.id, u.siteUrl)
  }
}


class UsersQueries(tag: Tag) extends Table[UserQuery](tag, "USER_QUERIES") {
  def id = column[Int]("USER_ID", O.PrimaryKey, O.AutoInc)
  def siteUrl = column[String]("SITE_URL")
  def * = (id.?, siteUrl) <> (UserQuery.tupled, UserQuery.unapply)

}