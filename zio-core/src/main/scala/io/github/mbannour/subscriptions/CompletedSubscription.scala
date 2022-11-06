package io.github.mbannour.subscriptions

import io.github.mbannour.result.Completed
import zio._

case class CompletedSubscription(p: JavaPublisher[Void]) extends SingleSubscription[Completed] {

  override def fetch[F[_]]: IO[Throwable, Completed] = ZIO.async[Any, Throwable, Completed] { callback =>
    p.subscribe {
      new JavaSubscriber[Void] {

        override def onSubscribe(s: JavaSubscription): Unit = s.request(1)

        override def onNext(t: Void): Unit = ()

        override def onError(t: Throwable): Unit = callback(ZIO.fail(t))

        override def onComplete(): Unit = callback(ZIO.succeed(Completed()))
      }
    }
  }
}
