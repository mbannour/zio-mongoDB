package io.github.mbannour.subscriptions

import io.github.mbannour.result.Completed
import org.reactivestreams.{Publisher => JPublisher, Subscriber => JSubscriber, Subscription => JSubscription}
import zio.IO

case class CompletedSubscription(p: JPublisher[Void]) extends Subscription[Completed] {

  override def subscribe[_]: IO[Throwable, Completed] = IO.async[Throwable, Completed] { callback =>
    p.subscribe {
      new JSubscriber[Void] {

        override def onSubscribe(s: JSubscription): Unit = s.request(1)

        override def onNext(t: Void): Unit = ()

        override def onError(t: Throwable): Unit = callback(IO.fail(t))

        override def onComplete(): Unit = callback(IO.succeed(Completed()))
      }
    }
  }
}