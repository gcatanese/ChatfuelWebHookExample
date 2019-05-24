package com.kata.frontend

import com.kata.model.{Attachment, AttachmentContainer, AttachmentUrl, Button, ChatfuelAction, ChatfuelAttribute, Element, Gallery, GalleryContainer, GalleryPayload, ListContainer, ListItem, ListPayload, Messages, QuickReplyContainer, QuickReplyOption, QuickReplyOptionWithType, TextMessage}
import com.typesafe.config.ConfigFactory
import akka.http.scaladsl.model.{ContentTypes, HttpEntity}

import scala.concurrent.ExecutionContext
import scala.concurrent.Await
import scala.util.{Failure, Success}
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import akka.Done
import akka.http.scaladsl.Http.ServerBinding
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives.{host, _}
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._

import scala.concurrent.Future
import akka.http.scaladsl.model.headers._
import akka.http.scaladsl.model._
import com.typesafe.scalalogging.{Logger, StrictLogging}
import spray.json.DefaultJsonProtocol._
import spray.json.{DeserializationException, JsObject, JsString, JsValue, JsonFormat}


class Server extends StrictLogging with ChatfuelAction with ChatfuelAttribute {

  val binding: Future[ServerBinding] = null

  def init(): Unit = {

    logger.info("Init Server...")

    val conf = ConfigFactory.load()

    val host = conf.getString("myapp.server-address")
    val port: Int = sys.env.getOrElse("PORT", conf.getString("myapp.server-port")).toInt

    implicit val system: ActorSystem = ActorSystem("chatfuelWebHook")
    implicit val executor: ExecutionContext = system.dispatcher
    implicit val materializer: ActorMaterializer = ActorMaterializer()

    // startup
    val binding = Http().bindAndHandle(route, host, port)
    binding.onComplete {
      case Success(_) => logger.info("Started host:" + host + " port:" + port)
      case Failure(error) => logger.error(s"Failed: ${error.getMessage}")
    }

    import scala.concurrent.duration._
    Await.result(binding, 3.seconds)

    // shutdown
    //println("shutdown...")

    //    binding
    //      .flatMap(_.unbind()) // trigger unbinding from the port
    //      .onComplete(_ => system.terminate()) // and shutdown when done

  }

  implicit val implTextMessage = jsonFormat1(TextMessage)
  implicit val implMessages = jsonFormat1(Messages[TextMessage])

  implicit val implAttachmentUrl = jsonFormat1(AttachmentUrl)
  implicit val implAttachment = jsonFormat2(Attachment)
  implicit val implAttachmentContainer = jsonFormat1(AttachmentContainer)

  implicit val implMessagesWithAttachments = jsonFormat1(Messages[AttachmentContainer])

  implicit val implQuickReplyOptionWithType = jsonFormat3(QuickReplyOptionWithType)
  implicit val implQuickReplyOption = jsonFormat2(QuickReplyOption)
  implicit val implQuickReplyContainer = jsonFormat2(QuickReplyContainer[List[QuickReplyOptionWithType]])
  implicit val implMessagesWithQuickReplies = jsonFormat1(Messages[QuickReplyContainer[List[QuickReplyOptionWithType]]])
  implicit val implQuickReplyContainer2 = jsonFormat2(QuickReplyContainer[List[QuickReplyOption]])
  implicit val implMessagesWithQuickReplies2 = jsonFormat1(Messages[QuickReplyContainer[List[QuickReplyOption]]])

  implicit val implButton = jsonFormat3(Button)
  implicit val implElement = jsonFormat4(Element)

  implicit val implGalleryPayload = jsonFormat3(GalleryPayload)
  implicit val implGallery = jsonFormat2(Gallery)
  implicit val implGalleryContainer = jsonFormat1(GalleryContainer)
  implicit val implMessagesWithGalleries = jsonFormat1(Messages[GalleryContainer])

  implicit val implListPayload = jsonFormat3(ListPayload)
  implicit val implList = jsonFormat2(ListItem)
  implicit val implListContainer = jsonFormat1(ListContainer)
  implicit val implMessagesWithLists = jsonFormat1(Messages[ListContainer])

  val pathStr = "chatfuelWebHookExample"

  def route = path(pathStr) {
    get {
      parameterSeq { params =>
        var incomingParameters: RequestParameters = new RequestParameters(params.toString())
        complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, "GET OK"))
      }
    } ~
      post {
        entity(as[String]) { payload =>

          implicit val body = payload
          logger.info("post: " + body)

          val userInput = getUserInput
          val firstname = getFirstname
          val lastname = getLastname

          if (userInput.equalsIgnoreCase("Hi")) {
            // reply with text
            val messages = replyWithTextMessages(Array[String]("hello " + firstname, "ciao Mr " + lastname))
            complete(messages)
          } else if (userInput.equals("Hello")) {
            // reply with 2 text messages
            var messages = replyWithAttachments(Array[(String, String)](("image", "https://rockets.chatfuel.com/assets/welcome.png")))
            complete(messages)
          } else if (userInput.equalsIgnoreCase("Video")) {
            // reply with video
            var messages = replyWithAttachments(Array[(String, String)](("video", "https://rockets.chatfuel.com/assets/video.mp4")))
            complete(messages)
          } else if (userInput.equalsIgnoreCase("Audio")) {
            // reply with audio
            var messages = replyWithAttachments(Array[(String, String)](("audio", "https://rockets.chatfuel.com/assets/hello.mp3")))
            complete(messages)
          } else if (userInput.equalsIgnoreCase("File")) {
            // reply with PDF
            var messages = replyWithAttachments(Array[(String, String)](("file", "https://rockets.chatfuel.com/assets/ticket.pdf")))
            complete(messages)
          } else if (userInput.equalsIgnoreCase("Quick")) {
            // reply with Quick Reply
            var messages = replyWithQuickReplies("Did you like it", Array[(String, String, String)](("Yes!", "https://rockets.chatfuel.com/api/sad-match", "json_plugin_url")))
            complete(messages)
          } else if (userInput.equalsIgnoreCase("Quick2")) {
            // reply with Quick Reply (with predefined AI blocks)
            var list = List[String]("BL1");

            var messages = replyWithQuickReplies("Did you like it", Array[(String, List[String])](("Yes!", list)))
            complete(messages)
          } else if (userInput.equalsIgnoreCase("Gallery")) {
            // reply with Gallery
            var bt1 = Button("web_url", "https://rockets.chatfuel.com/store", "View Item")
            var element1 = Element("Chatfuel Rockets Jersey", "https://rockets.chatfuel.com/assets/shirt.jpg", "Size: M", List[Button](bt1))
            var galleryPayload = GalleryPayload("generic", "square", List[Element](element1))
            var gallery = Gallery("template", galleryPayload)

            var messages = replyWithGalleries(GalleryContainer(gallery))
            complete(messages)
          } else if (userInput.equalsIgnoreCase("List")) {
            // reply with List
            var bt1 = Button("web_url", "https://rockets.chatfuel.com/store", "View Item")
            var element1 = Element("Chatfuel Rockets Jersey", "https://rockets.chatfuel.com/assets/shirt.jpg", "Size: M", List[Button](bt1))
            var listPayload = ListPayload("list", "large", List[Element](element1))
            var list = ListItem("template", listPayload)

            var messages = replyWithLists(ListContainer(list))
            complete(messages)
          } else {
            complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, "POST OK"))
          }

        }

      }
  }


}

object ServerObject extends App with StrictLogging {

  logger.info("ServerObject..")

  var server = new Server
  server.init()

}
