package io.github.mbannour.result

import com.mongodb.client.result.{ UpdateResult => JUpdateResult }
import org.bson.BsonValue

/**
  * The result of an update operation. If the update was unacknowledged, then {@code wasAcknowledged} will return false
  * and all other methods will throw {@code UnsupportedOperationException}.
  *
  * @see
  *   com.mongodb.WriteConcern#UNACKNOWLEDGED
  * @since 3.0
  */
final class UpdateResult private (val wrapper: JUpdateResult) extends JUpdateResult {

  override def wasAcknowledged(): Boolean = wrapper.wasAcknowledged()

  override def getMatchedCount: Long = wrapper.getMatchedCount

  override def getModifiedCount: Long = wrapper.getModifiedCount

  override def getUpsertedId: BsonValue = wrapper.getUpsertedId
}

object UpdateResult {

  def apply(wrapper: JUpdateResult): UpdateResult = new UpdateResult(wrapper)
}
