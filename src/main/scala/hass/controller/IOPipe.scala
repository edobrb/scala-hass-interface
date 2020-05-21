package hass.controller

import com.github.andyglow.websocket.WebsocketClient

import scala.util.{Failure, Success, Try}


trait InputPipe extends (Try[String] => Unit)

trait OutputPipe {
  def push(msg: String): Unit

  def close(): Unit
}

object IOPipe {
  def websocket(uri: String): IOPipe = (inputPipe: InputPipe) => {
    val clientBuilder = WebsocketClient.Builder[String](uri)({
      case str => inputPipe(Success(str))
    }).onFailure({
      case exception => inputPipe(Failure(exception))
    })
    val client = clientBuilder.build()
    Try(client.open()) match {
      case Failure(_) => None
      case Success(socket) => Some(new OutputPipe {
        override def push(msg: String): Unit = Try(socket ! msg)

        override def close(): Unit = client.shutdownSync()
      })
    }
  }
}

trait IOPipe extends (InputPipe => Option[OutputPipe])