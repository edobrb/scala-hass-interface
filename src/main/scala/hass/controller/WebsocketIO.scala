package hass.controller

import com.github.andyglow.websocket.WebsocketClient

import scala.util.{Failure, Success, Try}

object WebsocketIO {
  type MessageHandler = String => Unit
  type ExceptionHandler = Throwable => Unit

  def apply(uri: String,
            receiver: MessageHandler,
            exceptionHandler: ExceptionHandler): Option[WebsocketIO] = {
    val clientBuilder = WebsocketClient.Builder[String](uri)({
      case str => receiver(str)
    }).onFailure({
      case exception => exceptionHandler(exception)
    })
    val client = clientBuilder.build()
    Try(client.open()) match {
      case Failure(_) => None
      case Success(socket) => Some(new WebsocketIO {
        override def send(msg: String): Unit = socket ! msg

        override def close(): Unit = client.shutdownSync()
      })
    }
  }
}

trait WebsocketIO {
  def send(msg: String): Unit

  def close(): Unit
}

