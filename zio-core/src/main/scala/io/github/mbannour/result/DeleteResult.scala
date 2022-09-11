package io.github.mbannour.result

import com.mongodb.client.result.{ DeleteResult => JDeleteResult }

/**
  * The result of a delete operation. If the delete was unacknowledged, then {@code wasAcknowledged} will return false
  * and all other methods will throw {@code UnsupportedOperationException}.
  *
  * @see
  *   com.mongodb.WriteConcern#UNACKNOWLEDGED
  * @since 3.0
  */

final class DeleteResult private (wrapper: JDeleteResult) extends JDeleteResult {

  override def wasAcknowledged(): Boolean = wrapper.wasAcknowledged()

  override def getDeletedCount: Long = wrapper.getDeletedCount()

}

object DeleteResult {
  def apply(deleteResult: JDeleteResult): DeleteResult = new DeleteResult(deleteResult)
}
