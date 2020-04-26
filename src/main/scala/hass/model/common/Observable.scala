package hass.model.common

trait Observable[T] {
  private val observers = scala.collection.mutable.ListBuffer[PartialFunction[T, Unit]]()

  protected def addObserver(observer: PartialFunction[T, Unit]): Unit = observers.synchronized(observers.append(observer))

  protected def notifyObservers(value: T): Unit = observers.synchronized(observers.filter(_.isDefinedAt(value)).foreach(_ (value)))
}
