import com.ning.http.client.Response
import dispatch._, Defaults._
import java.io.File
import java.util.Date
import net.liftweb.json._
import scala.concurrent.Await
import scala.concurrent.duration._
import com.typesafe.config.ConfigFactory


object EventlyTest extends App {

  implicit val formats = net.liftweb.json.DefaultFormats
  
  val apiClient = SalesForceApiClient()
  apiClient.createEvent(s"Some event ${new Date()}")
  
  // list all events
  val fEvents = apiClient.events()
  fEvents.map(events => events.foreach(e => println(e.url)))
}

case class SalesForceApiClient(instanceUrl: String, accessToken: String) {

  implicit val formats = net.liftweb.json.DefaultFormats

  private val headers = Map("Authorization" -> Seq(s"Bearer $accessToken"), "Content-Type" -> Seq("application/json"))
  
  def createEvent(name: String) = {
    val apiUrl = s"$instanceUrl/services/data/v29.0/sobjects/eventforce__Events__c"
    url(apiUrl).POST.setHeaders(headers).setBody(s"""{"Name" : "$name"}""")
  }
  
  def events(): Future[List[Event]] = {
    val apiUrl = s"$instanceUrl/services/data/v29.0/sobjects/eventforce__Events__c"
    val req = url(apiUrl).GET.setHeaders(headers)
    Http(req).map { resp =>
      events(resp)
    }
  }
  
  private def events(resp: Response): List[Event] = {
    val json = parse(resp.getResponseBody) \\ "recentItems" \ "attributes"
    json.extract[List[Event]]
  }

}

case class Event(url: String)

object SalesForceApiClient {

  import Config._
  implicit val formats = net.liftweb.json.DefaultFormats

  private lazy val params = Map(
    "grant_type" -> Seq(conf.getString("grant_type")),
    "client_id" -> Seq(conf.getString("client_id")),
    "client_secret" -> Seq(conf.getString("client_secret")),
    "username" -> Seq(conf.getString("username")),
    "password" -> Seq(conf.getString("password"))
  )

  def apply(): SalesForceApiClient = {

    val req = url("https://login.salesforce.com/services/oauth2/token")
      .POST
      .setParameters(params)

    val result = Http(req).map {
      resp =>
        val body = resp.getResponseBody
        val value1: JsonAST.JValue = parse(body) \\ "access_token"
        val accessToken = value1.extract[String]

        val value2: JsonAST.JValue = parse(body) \\ "instance_url"
        val instanceUrl = value2.extract[String]
        SalesForceApiClient(instanceUrl, accessToken)
    }
    Await.result(result, 10 seconds)
  }
}

object Config {
  lazy val conf = {
    val path = System.getProperty("user.home") + "/.config/test/eventley.json"
    ConfigFactory.parseFile(new File(path))
  }
}
