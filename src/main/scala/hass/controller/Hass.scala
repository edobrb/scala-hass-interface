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
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

object Hass {
  def apply(host: String, token: String, log: Logger = ConsoleLogger): Hass =
    new Hass(IOPipe.websocket(s"ws://$host/api/websocket"), token, log)
}

class Hass(io: IOPipe, token: String, val log: Logger) extends Observable[Event] {

  private var state: State = State.empty(token, log)
  private var outputPipe: Option[OutputPipe] = None
  private val transformExecutor: ExecutorService = Executors.newSingleThreadExecutor()
  private val pingExecutor: ExecutorService = Executors.newSingleThreadExecutor()
  private val connectionExecutor: ExecutorService = Executors.newCachedThreadPool()
  private val inputPipe: InputPipe = {
    case Success(value) =>
      transform(_.digest(value))
    case Failure(exception) =>
      log err "Connection error: " + exception.getMessage
  }


  transformExecutor.execute(() => Thread.currentThread.setName("transform-thread"))
  pingExecutor.execute(() => Thread.currentThread.setName("ping-thread"))
  connectionExecutor.execute(() => Thread.currentThread.setName("connection-thread"))

  connect()

  onConnection(ping)

  private def connect(): Unit = connectionExecutor.execute(() =>{
    val f = Future {
      Thread.currentThread.setName("connection-thread") //TODO: avoid this
      close()
      log inf "Connecting..."
      io(inputPipe)
    }(ExecutionContext.fromExecutor(connectionExecutor))

    Try(Await.result(f, 10.seconds)) match {
      case Success(Some(value)) =>
        transform(_.clear)
        log inf "Connected."
        outputPipe = Some(value)
      case Failure(_) =>
        log err "Can't connect in 10 seconds! Will retry..."
        notifyObservers(ConnectionClosedEvent)
        connect()
      case Success(None) =>
        log err "Error while opening connection. Will retry..."
        notifyObservers(ConnectionClosedEvent)
        connect()
    }
  })

  private def ping(): Unit =
    pingExecutor.execute(() => {
      Try(Thread.sleep(1000))
      Try(Await.result(send(id => "{\"id\":" + id + ",\"type\":\"ping\"}"), 10.seconds)) match {
        case Success(Result(true, _)) => ping()
        case Failure(_) | Success(Result(false, _)) =>
          log err "Not received pong response in 10 seconds!"
          connect()
      }
    })

  private def transform(t: State => State): Unit = {
    transformExecutor.execute(() => synchronized {
      state = t(state) //need to be two statements so the states is already updated when calling observers
      val (notifications, state1) = state.extractPendingEvents()
      val (outputs, state2) = state1.extractPendingOutputs()
      state = state2
      notifications.foreach(notifyObservers)
      outputs.foreach(sendMessage)
    })
  }

  private def send(f: ID => Message): Future[Result] = {
    val future = new CompletableFuture[Result]
    transform(_.generateRequest(f, {
      case (state, result) => future.complete(result); state
    }))
    toScala(future)
  }

  private def sendMessage(msg: Message): Unit =
    outputPipe.foreach { pipe => pipe.push(msg) }

  def call(service: Service): Future[Result] =
    send(id => service.materialize(id).toString)

  def stateOf[S, E <: EntityState[S]](entityId: String): Option[E] =
    state.entitiesStates.get(entityId).map(_.asInstanceOf[E])

  def onEvent(f: PartialFunction[Event, Unit]): Unit = addObserver(f)

  def onStateChange(f: PartialFunction[EntityState[_], Unit]): Unit = onEvent {
    case StateChangedEvent(_, _, newState, _, _) if f.isDefinedAt(newState) => f(newState)
  }

  def onConnection(f: () => Unit): Unit = onEvent { case ConnectionReadyEvent => f() }

  def onClose(f: () => Unit): Unit = onEvent { case ConnectionClosedEvent => f() }

  def close(): Unit = {
    outputPipe.foreach({ value =>
      log inf "Closing..."
      value.close()
      log inf "Closed."
      notifyObservers(ConnectionClosedEvent)
    })
    outputPipe = None
  }
}