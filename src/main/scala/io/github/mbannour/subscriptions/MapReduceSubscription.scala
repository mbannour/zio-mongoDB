package io.github.mbannour.subscriptions

import com.mongodb.reactivestreams.client.MapReducePublisher
import org.reactivestreams.{ Subscriber => JSubscriber, Subscription => JSubscription }
import zio.IO

case class MapReduceSubscription[T](p: MapReducePublisher[T]) extends Subscription[T] {

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
}
