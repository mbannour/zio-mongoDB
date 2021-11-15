package io.github.mbannour.result

import com.mongodb.client.result.{DeleteResult => JDeleteResult}
import zio.{IO, Task}

/**
  * The result of a delete operation. If the delete was unacknowledged, then {@code wasAcknowledged} will return false and all other methods
  * will throw {@code UnsupportedOperationException}.
  *
  * @see com.mongodb.WriteConcern#UNACKNOWLEDGED
  * @since 3.0
  */
case class DeleteResult(wrapper: JDeleteResult)  {

  /**
    * Returns true if the write was acknowledged.
    *
    * @return true if the write was acknowledged
    */
  def wasAcknowledged(): Boolean = wrapper.wasAcknowledged()

  /**
    * Gets the number of documents deleted.
    *
    * @return a Task of the number of documents deleted
    */
  def getDeletedCount: Task[Long] = IO.attempt(wrapper.getDeletedCount())
}
