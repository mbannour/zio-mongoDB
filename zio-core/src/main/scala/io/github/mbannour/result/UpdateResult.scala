package io.github.mbannour.result

import com.mongodb.client.result.{UpdateResult => JUpdateResult}
import org.bson.BsonValue
import zio.{IO, Task}

/**
  * The result of an update operation.  If the update was unacknowledged, then {@code wasAcknowledged} will return false and all other
  * methods will throw {@code UnsupportedOperationException}.
  *
  * @see com.mongodb.WriteConcern#UNACKNOWLEDGED
  * @since 3.0
  */
case class UpdateResult(private val wrapper: JUpdateResult) {

  /**
    * Returns true if the write was acknowledged.
    *
    * @return true if the write was acknowledged
    */
  def wasAcknowledged(): Boolean = wrapper.wasAcknowledged()

  /**
    * Gets the number of documents matched by the query.
    *
    * @return a Task of the number of documents matched
    */
  def getMatchedCount: Task[Long] = IO.attempt(wrapper.getMatchedCount())

  /**
    * Gets the number of documents modified by the update.
    *
    * @return a Task the number of documents modified
    */
  def getModifiedCount:Task[Long]=  IO.attempt(wrapper.getModifiedCount())

  /**
    * If the replace resulted in an inserted document, gets the _id of the inserted document, otherwise None.
    *
    * @return if the replace resulted in an inserted document, the _id of the inserted document, otherwise Noe
    */
  def getUpsertedId: Option[BsonValue] = Option(wrapper.getUpsertedId())
}

