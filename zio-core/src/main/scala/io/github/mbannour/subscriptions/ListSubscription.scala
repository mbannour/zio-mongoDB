package io.github.mbannour.subscriptions

import zio._

import scala.collection.mutable.ArrayBuffer

case class ListSubscription[T](p: JavaPublisher[T]) extends Subscription[Iterable[T]] {

  override def fetch[_]: IO[Throwable, Iterable[T]] =
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
}
