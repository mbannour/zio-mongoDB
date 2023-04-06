package io.github.mbannour.subscriptions

import zio.{ IO, ZIO }

import java.util.concurrent.ConcurrentLinkedQueue
import scala.jdk.CollectionConverters._

abstract class Subscription[T, Publisher <: JavaPublisher[T]](p: Publisher) {
  def fetch[F[_]]: IO[Throwable, Iterator[T]] =
    ZIO.async[Any, Throwable, Iterator[T]] { callback =>
      p.subscribe {
        new JavaSubscriber[T] {

          val items = new ConcurrentLinkedQueue[T]()

          override def onSubscribe(s: JavaSubscription): Unit = s.request(Long.MaxValue)

          override def onNext(t: T): Unit = if (Option(t).nonEmpty) items.add(t)

          override def onError(t: Throwable): Unit = callback(ZIO.fail(t))

          override def onComplete(): Unit = callback(ZIO.succeed(items.iterator().asScala))
        }
      }
    }

  def toList[F[_]]: IO[Throwable, List[T]]       = fetch.map(_.toList)
  def head[F[_]]: IO[Throwable, T]               = fetch.map(_.next())
  def headOption[F[_]]: IO[Throwable, Option[T]] = fetch.map(r => Option(r.next()))

}

trait SingleSubscription[T] {
  def fetch[F[_]]: IO[Throwable, T]
  def headOption[F[_]]: IO[Throwable, Option[T]] = fetch.map(Option(_))

}
