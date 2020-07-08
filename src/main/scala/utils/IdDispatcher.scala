package utils

case class IdDispatcher(start: Long) {
  private val inc = 1.toLong
  private val idsLock = new Object()
  private val ids: Iterator[Long] = Stream.continually(inc).scanLeft(start) {
    case (Long.MaxValue, _) => start
    case (id, inc) => id + inc
  }.iterator
  private var nextId: Long =  ids.next()

  def next: Long = idsLock.synchronized {
    val old = nextId
    nextId = ids.next()
    old
  }

  def peekNext : Long = idsLock.synchronized(nextId)
}
