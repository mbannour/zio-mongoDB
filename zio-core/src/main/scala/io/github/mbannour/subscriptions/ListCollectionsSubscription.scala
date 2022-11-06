package io.github.mbannour.subscriptions

import com.mongodb.reactivestreams.client.ListCollectionsPublisher
import org.bson.conversions.Bson

import java.util.concurrent.TimeUnit

case class ListCollectionsSubscription[T](p: ListCollectionsPublisher[T]) extends Subscription[T, ListCollectionsPublisher[T]](p) {

  def filter(filter: Bson): ListCollectionsSubscription[T] = this.copy(p.filter(filter))

  def maxTime(maxTime: Long, timeUnit: TimeUnit): ListCollectionsSubscription[T] =
    this.copy(p.maxTime(maxTime, timeUnit))

  def batchSize(batchSize: Int): ListCollectionsSubscription[T] = this.copy(p.batchSize(batchSize))

  def first(): SingleItemSubscription[T] = SingleItemSubscription(p.first())

}

