package io.github.mbannour.result

import com.mongodb.client.result.{DeleteResult => JDeleteResult}

case class DeleteResult(wrapper: JDeleteResult)  {

  def wasAcknowledged(): Boolean = wrapper.wasAcknowledged()

  def getDeletedCount: Long = long2Long(wrapper.getDeletedCount())
}
