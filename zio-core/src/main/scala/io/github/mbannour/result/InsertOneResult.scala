package io.github.mbannour.result

import com.mongodb.client.result.{ InsertOneResult => JInsertOneResult }
import org.bson.BsonValue

/**
  * The result of an insert one operation. If the insert one was unacknowledged, then {@code wasAcknowledged} will
  * return false and all other methods will throw {@code UnsupportedOperationException}.
  *
  * @see
  *   com.mongodb.WriteConcern#UNACKNOWLEDGED
  * @since 4.0
  */
final class InsertOneResult private (val wrapper: JInsertOneResult) extends JInsertOneResult {

  override def wasAcknowledged(): Boolean = wrapper.wasAcknowledged()

  override def getInsertedId: BsonValue = wrapper.getInsertedId()
}

object InsertOneResult {

  def apply(wrapper: JInsertOneResult): InsertOneResult = new InsertOneResult(wrapper)
}
