package io.github.mbannour.subscriptions

import com.mongodb.reactivestreams.client.ListIndexesPublisher
import org.reactivestreams.{ Subscriber => JSubscriber, Subscription => JSubscription }
import zio.IO

import java.util.concurrent.TimeUnit
import scala.collection.mutable.ArrayBuffer

case class ListIndexesSubscription[T](p: ListIndexesPublisher[T]) extends Subscription[Iterable[T]] {

  override def subscribe[_]: IO[Throwable, Iterable[T]] =
    IO.async[Throwable, Iterable[T]] { callback =>
      p.subscribe {
        new JSubscriber[T] {

          val items = new ArrayBuffer[T]()

          override def onSubscribe(s: JSubscription): Unit = s.request(Long.MaxValue)

          override def onNext(t: T): Unit = items += t

          override def onError(t: Throwable): Unit = callback(IO.fail(t))

          override def onComplete(): Unit = callback(IO.succeed(items))
        }
      }
    }

  def maxTime(maxTime: Long, timeUnit: TimeUnit): ListIndexesSubscription[T] = this.copy(p.maxTime(maxTime, timeUnit))

  def batchSize(batchSize: Int): ListIndexesSubscription[T] = this.copy(p.batchSize(batchSize))

  def first(): SingleItemSubscription[T] = SingleItemSubscription(p.first())

}