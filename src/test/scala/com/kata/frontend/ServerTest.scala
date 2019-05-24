package com.kata.frontend

import org.scalatest.{BeforeAndAfter, FlatSpec, Matchers}
import Matchers._
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpResponse, StatusCodes}
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.http.scaladsl.server._
import Directives._
import akka.http.scaladsl.unmarshalling.FromResponseUnmarshaller
import com.kata.model.{Messages, QuickReplyContainer, QuickReplyOption}
import com.typesafe.scalalogging.StrictLogging
import spray.json.DefaultJsonProtocol.{jsonFormat1, jsonFormat2}
import spray.json._

import scala.concurrent.Await

class ServerTest extends FlatSpec with BeforeAndAfter with ScalatestRouteTest with StrictLogging {

  var server = new Server

  "Get " should "be OK" in {
    Get("/chatfuelWebHook?firstname=Beppe&lastname=Catanese") ~> server.route ~> check {
      responseAs[String] shouldEqual "GET OK"
    }
  }

  "Get without parameters" should "be OK" in {
    Get("/chatfuelWebHook") ~> server.route ~> check {
      responseAs[String] shouldEqual "GET OK"
    }
  }

  "Post " should "be 2 text messages" in {
    val body = "firstname=Beppe&lastname=Catanese&last+user+freeform+input=Hi"
    Post("/chatfuelWebHook").withEntity(body) ~> server.route ~> check {
      responseAs[String] shouldEqual "{\"messages\":[{\"text\":\"hello Beppe\"},{\"text\":\"ciao Mr Catanese\"}]}"
    }
  }

  it should "be an IMAGE attachment" in {
    val body = "firstname=Beppe&lastname=Catanese&last+user+freeform+input=Hello"
    Post("/chatfuelWebHook").withEntity(body) ~> server.route ~> check {
      responseAs[String] shouldEqual "{\"messages\":[{\"attachment\":{\"payload\":{\"url\":\"https://rockets.chatfuel.com/assets/welcome.png\"},\"type\":\"image\"}}]}"
    }
  }

  it should "be a VIDEO attachment" in {
    val body = "firstname=Beppe&lastname=Catanese&last+user+freeform+input=Hiya"
    Post("/chatfuelWebHook").withEntity(body) ~> server.route ~> check {
      responseAs[String] shouldEqual "{\"messages\":[{\"attachment\":{\"payload\":{\"url\":\"https://rockets.chatfuel.com/assets/video.mp4\"},\"type\":\"video\"}}]}"
    }
  }

  it should "be an AUDIO attachment" in {
    val body = "firstname=Beppe&lastname=Catanese&last+user+freeform+input=Audio"
    Post("/chatfuelWebHook").withEntity(body) ~> server.route ~> check {
      responseAs[String] shouldEqual "{\"messages\":[{\"attachment\":{\"payload\":{\"url\":\"https://rockets.chatfuel.com/assets/hello.mp3\"},\"type\":\"audio\"}}]}"
    }
  }

  it should "be a FILE attachment" in {
    val body = "firstname=Beppe&lastname=Catanese&last+user+freeform+input=File"
    Post("/chatfuelWebHook").withEntity(body) ~> server.route ~> check {
      responseAs[String] shouldEqual "{\"messages\":[{\"attachment\":{\"payload\":{\"url\":\"https://rockets.chatfuel.com/assets/ticket.pdf\"},\"type\":\"file\"}}]}"
    }
  }

  it should "be a QUICK REPLY " in {
    val body = "firstname=Beppe&lastname=Catanese&last+user+freeform+input=Quick"
    Post("/chatfuelWebHook").withEntity(body) ~> server.route ~> check {

      responseAs[String].contains("\"text\":\"Did you like it\"") shouldEqual true
      responseAs[String].contains("\"_type\":\"json_plugin_url\"") shouldEqual true
    }
  }

  it should "be a QUICK REPLY WITH BLOCK" in {
    val body = "firstname=Beppe&lastname=Catanese&last+user+freeform+input=Quick2"
    Post("/chatfuelWebHook").withEntity(body) ~> server.route ~> check {

      responseAs[String].contains("\"text\":\"Did you like it\"") shouldEqual true
      responseAs[String].contains("\"block_names\":[\"BL1\"]") shouldEqual true
    }
  }

  it should "be a GALLERY" in {
    val body = "firstname=Beppe&lastname=Catanese&last+user+freeform+input=Gallery"
    Post("/chatfuelWebHook").withEntity(body) ~> server.route ~> check {

      logger.info(responseAs[String])

      responseAs[String].contains("\"type\":\"template\"") shouldEqual true
      responseAs[String].contains("\"template_type\":\"generic\"") shouldEqual true
      responseAs[String].contains("\"title\":\"Chatfuel Rockets Jersey\"") shouldEqual true
    }
  }

  it should "be a LIST" in {
    val body = "firstname=Beppe&lastname=Catanese&last+user+freeform+input=List"
    Post("/chatfuelWebHook").withEntity(body) ~> server.route ~> check {

      logger.info(responseAs[String])

      responseAs[String].contains("\"type\":\"template\"") shouldEqual true
      responseAs[String].contains("\"template_type\":\"list\"") shouldEqual true
    }
  }


}
