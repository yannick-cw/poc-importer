package elasticserach_API

import akka.http.scaladsl.client.RequestBuilding
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpResponse}
import elasticserach_API.Queries.CleanedDoc
import spray.json._
import util._

import scala.concurrent.Future
import scala.util.Failure

/**
  * Created by yannick on 07.05.16.
  */
trait Requests extends HttpRequester with Protocols {
  val (rep, dem) = ("rep", "dem")

  def insert(cleanedDoc: CleanedDoc): Future[HttpResponse] = {
    val index = matchIndex(cleanedDoc)
    val docType = cleanedDoc.src.toLowerCase

    index match {
      case Some(str) =>
        val request = RequestBuilding.Post(s"/$str/$docType/",
          entity = HttpEntity(ContentTypes.`application/json`, cleanedDoc.toJson.compactPrint))
        futureHttpResponse(request, "172.17.0.2", 9200)

      case None =>
        import scala.concurrent.ExecutionContext.Implicits.global
        Future(throw new IllegalArgumentException("Undefined source"))
    }
  }

  def bulkInsert(docs: List[CleanedDoc]): Future[HttpResponse] = {
    val querie = docs.map{ doc =>
      //todo
      val index = matchIndex(doc).get
      val docType = doc.src.toLowerCase
      s"""{ "index": { "_index": "$index", "_type": "$docType" }}\n${doc.toJson.compactPrint}\n"""
    }.mkString

    val request = RequestBuilding.Post(s"/_bulk",
      entity = HttpEntity(ContentTypes.`application/json`, querie))
    futureHttpResponse(request, "172.17.0.2", 9200)
  }

  private def matchIndex(cleanedDoc: CleanedDoc): Option[String] = cleanedDoc.src match {
    case "Republicans" => Some(rep)
    case "Republican" => Some(rep)
    case "Democrats" => Some(dem)
    case "Democrat" => Some(dem)
    case any => None
  }
}
