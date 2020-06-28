package hass.controller

import org.scalatest._
import utils.IdDispatcher

import scala.concurrent.duration.{FiniteDuration, _}

class WebsocketIOTest extends FunSuite
 /* test("WebsocketIO virtual test") {
    val messages = Seq[(String, FiniteDuration)](("a", 1.second), ("b", 1.second))
    val lock = new Object()
    val ids = IdDispatcher(1)
    OutputSocket.virtual(autoMessages = messages.iterator, autoReceiver = {
      case "msg" if ids.next == 3 =>
        lock.synchronized(lock.notify())
      case _ => fail()
    }, receiver = {
      case "a" if ids.next == 1 =>
      case "b" if ids.next == 2 =>
        lock.synchronized(lock.notify())
      case _ => fail()
    }) match {
      case Some(value) =>
        lock.synchronized(lock.wait(3000))
        value.send("msg")
        lock.synchronized(lock.wait(1000))
      case None => fail()
    }
    assert(ids.next == 4)
  }*/

