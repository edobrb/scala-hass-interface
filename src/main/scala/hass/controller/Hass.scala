package hass.controller

import java.util.concurrent.{CompletableFuture, ExecutorService, Executors}

import hass.controller.State.{ID, Message}
import hass.model.common.Observable
import hass.model.event.{ConnectionClosedEvent, ConnectionReadyEvent, Event, StateChangedEvent}
import hass.model.service.{Result, Service}
import hass.model.state.EntityState
import utils.{ConsoleLogger, Logger}

import scala.compat.java8.FutureConverters.toScala
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.util.{Failure, Success, Try}

object Hass {
  def apply(host: String, token: String, log: Logger = ConsoleLogger): Hass =
    new Hass(IOPipe.websocket(s"ws://$host/api/websocket"), token, log)
}

class Hass(io: IOPipe, token: String, log: Logger) extends Observable[Event] {

  private var state: State = State.empty(log)
  private var outputPipe: Option[OutputPipe] = None
  private val transformExecutor: ExecutorService = Executors.newSingleThreadExecutor()
  private val pingExecutor: ExecutorService = Executors.newSingleThreadExecutor()
  private val connectionExecutor: ExecutorService = Executors.newSingleThreadExecutor()
  private val inputPipe: InputPipe = {
    case Success(value) =>
      transformAndVent(_.digest(value))
    case Failure(exception) =>
      log err "Connection error: " + exception.getMessage
  }
  private val ventState: State => State = state => {
    val (notifications, state1) = state.extractPendingEventNotifications()
    val (outputs, state2) = state1.extractPendingOutputs()
    notifications.foreach(notifyObservers)
    outputs.foreach(sendMessage)
    state2
  }

  transformExecutor.execute(() => Thread.currentThread.setName("transform-thread"))
  pingExecutor.execute(() => Thread.currentThread.setName("ping-thread"))
  connectionExecutor.execute(() => Thread.currentThread.setName("connection-thread"))

  connect()

  private def connect(): Unit =
    connectionExecutor.execute(() => {
      close()
      log inf "Connecting..."
      io(inputPipe) match {
        case Some(value) =>
          transform(_.clear)
          log inf "Connected."
          outputPipe = Some(value)
          auth()
          ping()
        case None =>
          log err "Error while opening connecting. Will retry..."
          notifyObservers(ConnectionClosedEvent)
          connect()
      }
    })

  private def auth(): Unit =
    sendMessage("{\"type\":\"auth\",\"access_token\":\"" + token + "\"}")

  private def ping(): Unit =
    pingExecutor.execute(() => {
      Try(Thread.sleep(1000))
      Try(Await.result(send(id => "{\"id\":" + id + ",\"type\":\"ping\"}"), 2.seconds)) match {
        case Success(Result(true, _)) => ping()
        case Failure(_) | Success(Result(false, _)) =>
          log err "Not received pong response in 2 seconds!"
          connect()
      }
    })

  private def transform(t: State => State): Unit =
    transformExecutor.execute(() => synchronized(state = t(state)))

  private def transformAndVent(t: State => State): Unit = transform(t.andThen(ventState))

  def call(service: Service): Future[Result] =
    send(id => service.materialize(id).toString)

  private def send(f: ID => Message): Future[Result] = {
    val future = new CompletableFuture[Result]
    transformAndVent(_.generateRequest(f, {
      case (state, result) => future.complete(result); state
    }))
    toScala(future)
  }

  private def sendMessage(msg: Message): Unit =
    outputPipe.collect { case pipe => pipe.push(msg) }

  def stateOf[E <: EntityState[_]](entityId: String): Option[E] =
    state.entitiesStates.get(entityId).map(_.asInstanceOf[EntityState[E]].state)

  def onEvent(f: PartialFunction[Event, Unit]): Unit = addObserver(f)

  def onStateChange(f: PartialFunction[EntityState[_], Unit]): Unit = onEvent {
    case StateChangedEvent(_, _, newState, _, _) if f.isDefinedAt(newState) => f(newState)
  }

  def onConnection(f: () => Unit): Unit = onEvent { case ConnectionReadyEvent => f() }

  def onClose(f: () => Unit): Unit = onEvent { case ConnectionClosedEvent => f() }

  def close(): Unit = {
    outputPipe.collect({ case value =>
      log inf "Closing..."
      value.close()
      log inf "Closed."
      notifyObservers(ConnectionClosedEvent)
    })
    outputPipe = None
  }
}
