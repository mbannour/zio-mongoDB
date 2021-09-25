package io.github.mbannour.subscriptions

import org.reactivestreams.{Subscription => JSubscription, Publisher => JPublisher, Subscriber => JSubscriber}
import zio.IO

case class SingleItemSubscription[T](p: JPublisher[T]) extends Subscription[T] {

 override def subscribe[_]: IO[Throwable, T] = IO.async[Throwable, T] { callback =>
    p.subscribe {
      new JSubscriber[T] {
        @volatile
        var item: T = _

        override def onSubscribe(s: JSubscription): Unit = s.request(1)

        override def onNext(t: T): Unit = item = t

        override def onError(t: Throwable): Unit = callback(IO.fail(t))

        override def onComplete(): Unit = callback(IO.succeed(item))
      }
    }
  }

 def optionalSubscribe[_]: IO[Throwable, Option[T]] = IO.async[Throwable, Option[T]] { callback =>
    p.subscribe {
      new JSubscriber[T] {
        @volatile
        var item: T = _

        override def onSubscribe(s: JSubscription): Unit = s.request(1)

        override def onNext(t: T): Unit = item = t

        override def onError(t: Throwable): Unit = callback(IO.fail(t))

        override def onComplete(): Unit = callback(IO.succeed(Option(item)))
      }
    }
  }

}
