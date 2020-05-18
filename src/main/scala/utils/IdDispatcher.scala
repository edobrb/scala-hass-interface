package utils

case class IdDispatcher(start: Long) {
  private val idsLock = new Object()
  private val ids: Iterator[Long] = Stream.continually(1.toLong).scanLeft(start) {
    case (Long.MaxValue, _) => start
    case (id, inc) => id + inc
  }.iterator

  def next: Long = idsLock.synchronized(ids.next())
}
