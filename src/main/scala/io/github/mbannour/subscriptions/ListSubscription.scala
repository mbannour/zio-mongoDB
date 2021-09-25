package io.github.mbannour.subscriptions

import org.reactivestreams.{ Publisher => JPublisher, Subscriber => JSubscriber, Subscription => JSubscription }
import zio.IO

import scala.collection.mutable.ArrayBuffer

case class ListSubscription[T](p: JPublisher[T]) extends Subscription[Iterable[T]] {

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

}
