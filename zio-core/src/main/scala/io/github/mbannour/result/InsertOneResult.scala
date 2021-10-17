package io.github.mbannour.result

import com.mongodb.client.result.{ InsertOneResult => JInsertOneResult }
import org.bson.BsonValue

/**
  * The result of an insert one operation.  If the insert one was unacknowledged, then {@code wasAcknowledged} will
  * return false and all other methods will throw {@code UnsupportedOperationException}.
  *
  * @see com.mongodb.WriteConcern#UNACKNOWLEDGED
  * @since 4.0
  */
case class InsertOneResult(private val wrapper: JInsertOneResult) {

  /**
    * Returns true if the write was acknowledged.
    *
    * @return true if the write was acknowledged
    */
  def wasAcknowledged(): Boolean = wrapper.wasAcknowledged()

  /**
    * If the _id of the inserted document if available, otherwise null
    *
    * <p>Note: Inserting RawBsonDocuments does not generate an _id value.</p>
    *
    * @return if _id of the inserted document if available, otherwise None
    */
  def getInsertedId: Option[BsonValue] = Option(wrapper.getInsertedId)
}

