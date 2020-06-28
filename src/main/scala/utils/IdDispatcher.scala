package utils

case class IdDispatcher(start: Long) {
  private val idsLock = new Object()
  private var currentId: Long = start
  private val ids: Iterator[Long] = Stream.continually(1.toLong).scanLeft(start) {
    case (Long.MaxValue, _) => start
    case (id, inc) => id + inc
  }.iterator

  def next: Long = idsLock.synchronized {
    currentId = ids.next()
    currentId
  }

  def current : Long = idsLock.synchronized(currentId)
}
