package io.github.mbannour.result

import com.mongodb.client.result.{ InsertOneResult => JInsertOneResult }
import org.bson.BsonValue

case class InsertOneResult(private val wrapper: JInsertOneResult) {

  def wasAcknowledged(): Boolean = wrapper.wasAcknowledged()

  def getInsertedId: Option[BsonValue] = Option(wrapper.getInsertedId)
}
