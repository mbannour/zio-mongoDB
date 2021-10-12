package io.github.mbannour.result

import com.mongodb.client.result.{ UpdateResult => JUpdateResult }
import org.bson.BsonValue

case class UpdateResult(private val wrapper: JUpdateResult) {

  def wasAcknowledged(): Boolean = wrapper.wasAcknowledged()

  def getMatchedCount: Long = wrapper.getMatchedCount()

  def getModifiedCount: Long = wrapper.getModifiedCount()

  def getUpsertedId: Option[BsonValue] = Option(wrapper.getUpsertedId())
}

