package io.github.mbannour.subscriptions

import com.mongodb.client.model.Collation
import com.mongodb.reactivestreams.client.DistinctPublisher
import org.bson.conversions.Bson
import java.util.concurrent.TimeUnit

case class DistinctSubscription[T](p: DistinctPublisher[T]) extends Subscription[T, DistinctPublisher[T]](p) {

  def filter(filter: Bson): DistinctSubscription[T] = this.copy(p.filter(filter))

  def maxTime(maxTime: Long, timeUnit: TimeUnit): DistinctSubscription[T] = this.copy(p.maxTime(maxTime, timeUnit))

  def collation(collation: Collation): DistinctSubscription[T] = this.copy(p.collation(collation))

  def batchSize(batchSize: Int): DistinctSubscription[T] = this.copy(p.batchSize(batchSize))

  def first(): SingleItemSubscription[T] = SingleItemSubscription(p.first())

}
