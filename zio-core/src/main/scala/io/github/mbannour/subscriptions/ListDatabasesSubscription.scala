package io.github.mbannour.subscriptions

import com.mongodb.reactivestreams.client.ListDatabasesPublisher
import org.bson.conversions.Bson

import java.lang
import java.util.concurrent.TimeUnit

case class ListDatabasesSubscription[T](p: ListDatabasesPublisher[T]) extends Subscription[T, ListDatabasesPublisher[T]](p) {

  def maxTime(maxTime: Long, timeUnit: TimeUnit): ListDatabasesSubscription[T] = copy(p.maxTime(maxTime, timeUnit))

  def filter(filter: Bson): ListDatabasesSubscription[T] = copy(p.filter(filter))

  def nameOnly(nameOnly: lang.Boolean): ListDatabasesSubscription[T] = copy(p.nameOnly(nameOnly))

  def authorizedDatabasesOnly(authorizedDatabasesOnly: lang.Boolean): ListDatabasesSubscription[T] =
    copy(p.authorizedDatabasesOnly(authorizedDatabasesOnly))

  def batchSize(batchSize: Int): ListDatabasesSubscription[T] = copy(p.batchSize(batchSize))

  def first(): SingleItemSubscription[T] = SingleItemSubscription(p.first())

}
