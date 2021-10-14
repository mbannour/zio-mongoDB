package io.github.mbannour.result

import scala.collection.JavaConverters._
import com.mongodb.client.result.{InsertManyResult => JInsertManyResult}
import org.bson.BsonValue

case class InsertManyResult(private val wrapper: JInsertManyResult) {

  def wasAcknowledged(): Boolean = wrapper.wasAcknowledged()

  def getInsertedIds: Map[Integer, BsonValue] = wrapper.getInsertedIds.asScala.toMap
}
