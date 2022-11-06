package io.github.mbannour.subscriptions

import com.mongodb.reactivestreams.client.ListIndexesPublisher

import java.util.concurrent.TimeUnit

case class ListIndexesSubscription[T](p: ListIndexesPublisher[T]) extends Subscription[T, ListIndexesPublisher[T]](p) {
  def maxTime(maxTime: Long, timeUnit: TimeUnit): ListIndexesSubscription[T] = this.copy(p.maxTime(maxTime, timeUnit))

  def batchSize(batchSize: Int): ListIndexesSubscription[T] = this.copy(p.batchSize(batchSize))

  def first(): SingleItemSubscription[T] = SingleItemSubscription(p.first())

}
