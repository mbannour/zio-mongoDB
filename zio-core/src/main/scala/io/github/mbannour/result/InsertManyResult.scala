package io.github.mbannour.result

import scala.jdk.CollectionConverters._
import com.mongodb.client.result.{ InsertManyResult => JInsertManyResult }
import org.bson.BsonValue

/**
  * The result of an insert many operation. If the insert many was unacknowledged, then {@code wasAcknowledged} will
  * return false and all other methods will throw {@code UnsupportedOperationException}.
  *
  * @see
  *   com.mongodb.WriteConcern#UNACKNOWLEDGED
  * @since 4.0
  */
final class InsertManyResult private (val acknowledged: Boolean, insertedIds: Map[Integer, BsonValue])  {

  def wasAcknowledged(): Boolean = acknowledged

  def getInsertedIds: Map[Integer, BsonValue] = insertedIds
}
object InsertManyResult {

  def apply(wrapper: JInsertManyResult) = new InsertManyResult(wrapper.wasAcknowledged(), wrapper.getInsertedIds.asScala.toMap)
}
