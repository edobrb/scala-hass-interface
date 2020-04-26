package hass

import akka.Done
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.ws.{BinaryMessage, Message, TextMessage, WebSocketRequest}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Keep, Sink, Source}
import akka.util.ByteString
import org.reactivestreams.{Publisher, Subscriber}
import spray.json.{JsonParser, ParserInput}

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

object Test2 extends App {
  private val nextIdLock = new Object()
  private var nextIdVar: Long = 0

  private def nextId: Long = nextIdLock.synchronized {
    nextIdVar = nextIdVar + 1
    nextIdVar
  }

  val token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiIwZjQ1NWUwMWNkNzE0NGFkYjVkZmJhZDJkZDM2YzBlMiIsImlhdCI6MTU4NjEwODM5NCwiZXhwIjoxOTAxNDY4Mzk0fQ.1a2dodNofYhygdCybk-6MA7No8MnZozC94UTG_sbBQA"
  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  implicit val dispatcher = system.dispatcher

  println("Started")
  private val publisher = new Publisher[TextMessage] {
    private var subscribers = Seq[Subscriber[_ >: TextMessage]]()

    override def subscribe(s: Subscriber[_ >: TextMessage]): Unit = subscribers = subscribers :+ s

    def send(textMessage: TextMessage.Strict): Unit = {
      println("<- " + textMessage.text)
      subscribers.foreach(_.onNext(textMessage))
    }
  }
  val outgoing = Source.fromPublisher(publisher)


  val webSocketFlow = Http().webSocketClientFlow(WebSocketRequest("ws://192.168.1.10:8123/api/websocket"))

  def handleMessage(text:String): Unit = {
    println("-> " + text)
    Try {
      val json = JsonParser(ParserInput(text)).asJsObject
      if (json.fields.contains("type") && json.fields("type").compactPrint == "\"auth_required\"") {
        publisher.send(TextMessage("{\"type\":\"auth\",\"access_token\":\"" + token + "\"}"))
      }
      if (json.fields.contains("type") && json.fields("type").compactPrint == "\"auth_ok\"") {
        val subscribeEventsId = nextId
        publisher.send(TextMessage("{\"id\":" + subscribeEventsId + ",\"type\":\"subscribe_events\"}"))
      }
    }
  }
  val incoming: Sink[Message, Future[Done]] =
    Sink.foreach[Message] {
      case message: TextMessage.Strict => handleMessage(message.text)
      case message: BinaryMessage.Strict => handleMessage(message.data.utf8String)
      case a:Message => a.asBinaryMessage.toStrict(500, materializer).thenAccept(m => {
        handleMessage(m.data.utf8String)
      }).exceptionally(th => {
        println("LOL");
       ???
      })

    }


  val (upgradeResponse, closed) =
    outgoing
      .viaMat(webSocketFlow)(Keep.right) // keep the materialized Future[WebSocketUpgradeResponse]
      .toMat(incoming)(Keep.both) // also keep the Future[Done]
      .run()


  val connected = upgradeResponse.flatMap { upgrade =>
    if (upgrade.response.status == StatusCodes.SwitchingProtocols) {
      println("Connected!")
      Future.successful(Done)
    } else {
      throw new RuntimeException(s"Connection failed: ${upgrade.response.status}")
    }
  }

  // in a real application you would not side effect here
  //connected.onComplete(println)
  closed.foreach(_ => println("closed"))
}
