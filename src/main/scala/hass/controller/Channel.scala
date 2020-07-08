package hass.controller

import hass.model.common.Observable
import scalaz.-\/
import scalaz.concurrent.Task
import utils.IdDispatcher

import scala.collection.mutable.{Map => MutableMap}
import scala.concurrent.duration.{FiniteDuration, _}

object Channel {
  private val channels: MutableMap[String, Channel] = MutableMap()

  def apply(name: String)(implicit hass: Hass): Channel = channels.synchronized {
    channels.get(name) match {
      case Some(value) => value
      case None =>
        val channel = createNew(name)
        channels += (name -> channel)
        channel
    }
  }

  private def createNew(name: String)(implicit hass: Hass): Channel = new Channel with Observable[Any] {

    import scala.concurrent.duration.FiniteDuration

    private val runIds: IdDispatcher = IdDispatcher(1)

    override def signal(value: Any, delay: FiniteDuration): Unit = {
      val runId = runIds.peekNext
      Task.schedule({
        if (runId == runIds.peekNext) {
          notifyObservers(value)
        }
      }, delay).unsafePerformAsync {
        case -\/(a) => hass.log.err("[CHANNEL " + name + "]: " + a.getMessage)
        case _ =>
      }
    }

    override def reset(): Unit = runIds.next

    override def onSignal(f: PartialFunction[Any, Unit]): Unit = addObserver(f)
  }
}

trait Channel {
  def signal(value: Any, fromNow: FiniteDuration): Unit

  def signal(value: Any): Unit = signal(value, 0.seconds)

  def reset(): Unit

  def onSignal(f: PartialFunction[Any, Unit]): Unit
}


