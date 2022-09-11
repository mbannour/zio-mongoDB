package io.github.mbannour.subscriptions

import zio._

case class SingleItemSubscription[T](p: JavaPublisher[T]) extends Subscription[T] {

  override def fetch[F[_]]: IO[Throwable, T] = ZIO.async[Any, Throwable, T] { callback =>
    p.subscribe {
      new JavaSubscriber[T] {
        @volatile
        var item: T = _

        override def onSubscribe(s: JavaSubscription): Unit = s.request(1)

        override def onNext(t: T): Unit = item = t

        override def onError(t: Throwable): Unit = callback(ZIO.fail(t))

        override def onComplete(): Unit = callback(ZIO.succeed(item))
      }
    }
  }

  def headOption[F[_]]: IO[Throwable, Option[T]] = ZIO.async[Any, Throwable, Option[T]] { callback =>
    p.subscribe {
      new JavaSubscriber[T] {
        @volatile
        var item: T = _

        override def onSubscribe(s: JavaSubscription): Unit = s.request(1)

        override def onNext(t: T): Unit = item = t

        override def onError(t: Throwable): Unit = callback(ZIO.fail(t))

        override def onComplete(): Unit = callback(ZIO.succeed(Option(item)))
      }
    }
  }

}

