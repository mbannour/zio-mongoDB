package io.github.mbannour.subscriptions

import com.mongodb.reactivestreams.client.ListIndexesPublisher
import zio._

import java.util.concurrent.TimeUnit
import scala.collection.mutable.ArrayBuffer

case class ListIndexesSubscription[T](p: ListIndexesPublisher[T]) extends Subscription[Iterable[T]] {

  override def fetch[F[_]]: IO[Throwable, Iterable[T]] =
    ZIO.async[Any, Throwable, Iterable[T]] { callback =>
      p.subscribe {
        new JavaSubscriber[T] {

          val items = new ArrayBuffer[T]()

          override def onSubscribe(s: JavaSubscription): Unit = s.request(Long.MaxValue)

          override def onNext(t: T): Unit = items += t

          override def onError(t: Throwable): Unit = callback(ZIO.fail(t))

          override def onComplete(): Unit = callback(ZIO.succeed(items.toSeq))
        }
      }
    }

  def maxTime(maxTime: Long, timeUnit: TimeUnit): ListIndexesSubscription[T] = this.copy(p.maxTime(maxTime, timeUnit))

  def batchSize(batchSize: Int): ListIndexesSubscription[T] = this.copy(p.batchSize(batchSize))

  def first(): SingleItemSubscription[T] = SingleItemSubscription(p.first())

}
