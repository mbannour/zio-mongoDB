package io.github.mbannour.subscriptions

import com.mongodb.reactivestreams.client.ListCollectionsPublisher
import org.bson.conversions.Bson
import org.reactivestreams.{Subscription => JSubscription}
import zio.IO

import java.util.concurrent.TimeUnit
import scala.collection.mutable.ArrayBuffer

case class ListCollectionsSubscription[T](p: ListCollectionsPublisher[T]) extends Subscription[Iterable[T]] {

  override def fetch[_]: IO[Throwable, Iterable[T]] = IO.async[Throwable, Iterable[T]] { callback =>
    p.subscribe {
      new JavaSubscriber[T] {

        val items = new ArrayBuffer[T]()

        override def onSubscribe(s: JSubscription): Unit = s.request(Long.MaxValue)

        override def onNext(t: T): Unit = items += t

        override def onError(t: Throwable): Unit = callback(IO.fail(t))

        override def onComplete(): Unit = callback(IO.succeed(items.toSeq))
      }
    }
  }

  def filter(filter: Bson): ListCollectionsSubscription[T] = this.copy(p.filter(filter))

  def maxTime(maxTime: Long, timeUnit: TimeUnit): ListCollectionsSubscription[T] =
    this.copy(p.maxTime(maxTime, timeUnit))

  def batchSize(batchSize: Int): ListCollectionsSubscription[T] = this.copy(p.batchSize(batchSize))

  def first(): SingleItemSubscription[T] = SingleItemSubscription(p.first())

}

