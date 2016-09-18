package models

import slick.driver.SQLiteDriver.api._


case class UserQuery(id: Option[Long], siteUrl:String, htmlTags:String)

object UserQuery extends ((Option[Long], String ,String) => UserQuery) {
  def apply(siteUrl:String, htmlTags:String):UserQuery = UserQuery(None, siteUrl, htmlTags)
  def unpick(u: UserQuery): Option[( Option[Long], String,String )]  = Some(u.id,u.siteUrl,u.htmlTags)
}


class UsersQuiries(tag: Tag) extends Table[UserQuery](tag, "USER_QUERIES") {
  def id = column[Long]("ID", O.PrimaryKey, O.AutoInc)
  def siteUrl = column[String]("SITE_URL")
  def htmlTags = column[String]("HTML_TAGS")
  def * = (id.?, siteUrl, htmlTags) <> (UserQuery.tupled, UserQuery.unapply)

}