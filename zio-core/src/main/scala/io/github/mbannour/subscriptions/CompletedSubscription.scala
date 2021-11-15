package io.github.mbannour.subscriptions

import io.github.mbannour.result.Completed
import zio.IO

case class CompletedSubscription(p: JavaPublisher[Void]) extends Subscription[Completed] {

  override def fetch[_]: IO[Throwable, Completed] = IO.async[Throwable, Completed] { callback =>
    p.subscribe {
      new JavaSubscriber[Void] {

        override def onSubscribe(s: JavaSubscription): Unit = s.request(1)

        override def onNext(t: Void): Unit = ()

        override def onError(t: Throwable): Unit = callback(IO.fail(t))

        override def onComplete(): Unit = callback(IO.succeed(Completed()))
      }
    }
  }
}
