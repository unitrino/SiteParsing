package controllers

import java.net.UnknownHostException
import javax.inject.Inject

import play.api._
import play.api.data._
import play.api.data.Forms._
import play.api.db.slick.DatabaseConfigProvider
import play.api.http.HttpEntity
import play.api.libs.json._
import play.api.mvc._
import models.{TagsQueries, TagQuery, UsersQueries, UserQuery}
import slick.driver.JdbcProfile
import slick.jdbc.meta.MTable
import slick.lifted.TableQuery
import slick.driver.PostgresDriver.api._

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
import net.ruippeixotog.scalascraper.dsl.DSL.Parse._
import net.ruippeixotog.scalascraper.model.Element

import scala.concurrent.{Await, Future}

class Application @Inject()(dbConfigProvider: DatabaseConfigProvider) extends Controller {
  val dbConfig = dbConfigProvider.get[JdbcProfile]
  val userQuery = TableQuery[UsersQueries]
  val tagQuery = TableQuery[TagsQueries]
  val link = "http://127.0.0.1:9000/id/"

  def getInfoWithList(url:String, htlmTags: List[String]): JsObject = {
    val doc = JsoupBrowser().get(url)
    val allText:Map[String, List[Element]] = htlmTags.map( elem => (elem, doc >> elementList(elem)) ).toMap
    val out: JsObject = JsObject(allText.map(zz => (zz._1, JsArray(zz._2.map( ll => JsString(ll.text))))))
    println(out)
    out
  }




  def getInfo(url:String, htlmTags: String): JsArray = {
      val doc = JsoupBrowser().get(url)
      val allText:List[Element] = doc >> elementList(htlmTags)
      allText.foreach((elem) => println(elem.text))
      val out: JsArray = JsArray(allText.map((elem) => JsString(elem.text)))
      println(out)
      out
  }

//  val userForm = Form(
//    mapping(
//      "id" -> optional(longNumber),
//      "siteUrl" -> nonEmptyText,
//      "htmlTags" -> nonEmptyText
//    )(UserQuery.apply)(UserQuery.unapply)
//  )



  def testingAction = Action {
    val answ = getInfoWithList("http://eax.me/", List(".post-title", ".entry"))
    println(answ)

    Ok(Json.toJson(Map("answ" -> answ)).toString())
  }


  def index = Action {

    try {
      def createTableIfNotInTables(tables: Vector[MTable]): Future[Unit] = {
        if (!tables.exists(_.name.name == userQuery.baseTableRow.tableName) || !tables.exists(_.name.name == tagQuery.baseTableRow.tableName)) {

            val ss = DBIO.seq(
              userQuery.schema.create,
              tagQuery.schema.create
            )

          dbConfig.db.run(ss)
        } else {
          Future()
        }
      }

      val createTableIfNotExist: Future[Unit] = dbConfig.db.run(MTable.getTables).flatMap(createTableIfNotInTables)
      Await.result(createTableIfNotExist, Duration.Inf)

    } finally {
      println("OK")
    }
    Ok(views.html.index())

  }

  def testingData = Action(parse.json) {
    implicit request => {
      val siteUrl = (request.body \ "siteUrl").as[String]
      val htmlTags = (request.body \ "htmlTags").as[List[String]]
      val answ = getInfoWithList(siteUrl, htmlTags)
      Ok(answ.toString())
    }
//    userForm.bindFromRequest.fold(
//      formWithErrors => {
//        BadRequest("Incorrect input. You must specify all fields.")
//      },
//      checkedForm => {
//        try {
//          val answ = getInfo(checkedForm.siteUrl, checkedForm.htmlTags)
//          println(answ)
//          Ok(answ)
//        }
//        catch {
//          case urlExp: IllegalArgumentException => BadRequest("Incorrect url")
//          case e: UnknownHostException => BadRequest("Error while parsing. Host is unreachable")
//          case _: Throwable => BadRequest("Error in system")
//        }
//      }
//    )
  }

  def generateLink = Action(parse.json) {
    implicit request =>
      val siteUrl = (request.body \ "siteUrl").as[String]
      val htmlTags = (request.body \ "htmlTags").as[List[String]]

        try {
          val uid:Int =  Await.result(dbConfig.db.run((userQuery returning userQuery.map(_.id)) += UserQuery(siteUrl)).map { answ => answ }, 30.seconds)
          println(uid)
          val resp = DBIO.seq(
            tagQuery ++= htmlTags.map(tag => TagQuery(tag, uid))
          )
          Await.result(dbConfig.db.run(resp).map { answ => Ok(link + uid.toString() + "/") }, 30.seconds)
        }
        catch {
          case e:Exception => BadRequest("Error while saving to database." + e.getMessage)
        }


//      userForm.bindFromRequest.fold(
//      formWithErrors => {
//        BadRequest("Incorrect input. You must specify all fields.")
//      },
//      checkedForm => {
//        println(checkedForm.siteUrl, checkedForm.htmlTags)
//
//        try {
//          Await.result(dbConfig.db.run((table returning table.map(_.id)) += UserQuery(checkedForm.siteUrl, checkedForm.htmlTags)).map { answ => Ok(link + answ.toString() + "/") }, 30.seconds)
//        }
//        catch {
//          case e:Exception => BadRequest("Error while saving to database." + e.getMessage)
//        }
//      }
//    )
}

  def getData(id:Int) = Action {
    implicit request =>
      val q = userQuery.filter {_.id === id}
      Await.result(dbConfig.db.run(q.result).map {
        answ => {
          try {
            val oneRowSiteUrl = answ.take(1)
            val tags:List[String] = Await.result(dbConfig.db.run(tagQuery.filter(_.userId === id).result), 20.seconds).map(_.htmlTag).toList
            Ok(Json.toJson(Map("answ" -> getInfoWithList(oneRowSiteUrl.head.siteUrl, tags))).toString())
          }
          catch {
            case urlExp: IllegalArgumentException => BadRequest("Incorrect url")
            case e: UnknownHostException => BadRequest("Error while parsing. Host is unreachable")
            case _: Throwable => BadRequest("Error in system")
          }
        }
      }, 10.seconds)
  }

}
