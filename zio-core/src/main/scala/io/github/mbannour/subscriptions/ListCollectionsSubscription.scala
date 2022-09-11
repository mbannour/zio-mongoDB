package io.github.mbannour.subscriptions

import com.mongodb.reactivestreams.client.ListCollectionsPublisher
import org.bson.conversions.Bson
import org.reactivestreams.{Subscription => JSubscription}
import zio._

import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import scala.collection.mutable.ArrayBuffer

case class ListCollectionsSubscription[T](p: ListCollectionsPublisher[T]) extends Subscription[Iterable[T]] {

  override def fetch[F[_]]: IO[Throwable, Iterable[T]] = ZIO.async[Any, Throwable, Iterable[T]] { callback =>
    p.subscribe {
      new JavaSubscriber[T] {

        val items = new ArrayBuffer[T]()

        val isSubscribedOrInterrupted = new AtomicBoolean

        override def onSubscribe(s: JSubscription): Unit =  if (s == null) {
          val e = new NullPointerException("s was null in onSubscribe")
          throw e
        } else {
          val shouldCancel = isSubscribedOrInterrupted.getAndSet(true)
          if (shouldCancel)
            s.cancel()
          else
            s.request(Long.MaxValue)
        }

        override def onNext(t: T): Unit = items += t

        override def onError(t: Throwable): Unit = callback(ZIO.fail(t))

        override def onComplete(): Unit = callback(ZIO.succeed(items.toSeq))
      }
    }
  }

  def filter(filter: Bson): ListCollectionsSubscription[T] = this.copy(p.filter(filter))

  def maxTime(maxTime: Long, timeUnit: TimeUnit): ListCollectionsSubscription[T] =
    this.copy(p.maxTime(maxTime, timeUnit))

  def batchSize(batchSize: Int): ListCollectionsSubscription[T] = this.copy(p.batchSize(batchSize))

  def first(): SingleItemSubscription[T] = SingleItemSubscription(p.first())

}

