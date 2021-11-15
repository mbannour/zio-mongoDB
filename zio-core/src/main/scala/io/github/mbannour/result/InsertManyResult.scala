package io.github.mbannour.result

import scala.jdk.CollectionConverters._
import com.mongodb.client.result.{InsertManyResult => JInsertManyResult}
import org.bson.BsonValue
import zio.{IO, Task}


/**
  * The result of an insert many operation.  If the insert many was unacknowledged, then {@code wasAcknowledged} will
  * return false and all other methods will throw {@code UnsupportedOperationException}.
  *
  * @see com.mongodb.WriteConcern#UNACKNOWLEDGED
  * @since 4.0
  */
case class InsertManyResult(private val wrapper: JInsertManyResult) {

  /**
    * Returns true if the write was acknowledged.
    *
    * @return true if the write was acknowledged
    */
  def wasAcknowledged(): Boolean = wrapper.wasAcknowledged()

  /**
    * An unmodifiable map of the index of the inserted document to the id of the inserted document.
    *
    * <p>Note: Inserting RawBsonDocuments does not generate an _id value and it's corresponding value will be null.</p>
    *
    * @return A Task of type map of the index of the inserted document to the id of the inserted document.
    */
  def getInsertedIds: Task[Map[Integer, BsonValue]] = IO.attempt(wrapper.getInsertedIds.asScala.toMap)
}
