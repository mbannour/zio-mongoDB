package io.github.mbannour.subscriptions

import com.mongodb.ExplainVerbosity
import com.mongodb.client.model.Collation
import com.mongodb.reactivestreams.client.AggregatePublisher
import org.bson.Document
import org.bson.conversions.Bson
import org.reactivestreams.{ Subscriber => JSubscriber, Subscription => JSubscription }
import zio.IO

import java.util.concurrent.TimeUnit
import scala.collection.mutable.ArrayBuffer

case class AggregateSubscription[T](p: AggregatePublisher[T]) extends Subscription[Iterable[T]] {

  override def fetch[_]: IO[Throwable, Iterable[T]] =
    IO.effectAsync[Throwable, Iterable[T]] { callback =>
      p.subscribe {
        new JSubscriber[T] {

          val items = new ArrayBuffer[T]()

          override def onSubscribe(s: JSubscription): Unit = s.request(Long.MaxValue)

          override def onNext(t: T): Unit = items += t

          override def onError(t: Throwable): Unit = callback(IO.fail(t))

          override def onComplete(): Unit = callback(IO.succeed(items.toSeq))
        }
      }
    }

  def allowDiskUse(allowDiskUse: Boolean): AggregateSubscription[T] = this.copy(p.allowDiskUse(allowDiskUse))

  def maxTime(maxTime: Long, timeUnit: TimeUnit): AggregateSubscription[T] = this.copy(p.maxTime(maxTime, timeUnit))

  def maxAwaitTime(maxAwaitTime: Long, timeUnit: TimeUnit): AggregateSubscription[T] =
    this.copy(p.maxAwaitTime(maxAwaitTime, timeUnit))

  def bypassDocumentValidation(bypassDocumentValidation: Boolean): AggregateSubscription[T] =
    this.copy(p.bypassDocumentValidation(bypassDocumentValidation))

  def collation(collation: Collation): AggregateSubscription[T] = this.copy(p.collation(collation))

  def comment(comment: String): AggregateSubscription[T] = this.copy(p.comment(comment))

  def hint(hint: Bson): AggregateSubscription[T] = this.copy(p.hint(hint))

  def let(variables: Bson): AggregateSubscription[T] = this.copy(p.let(variables))

  def batchSize(batchSize: Int): AggregateSubscription[T] = this.copy(p.batchSize(batchSize))

  def first(): SingleItemSubscription[T] = SingleItemSubscription(p.first())

  def explain(): SingleItemSubscription[Document] = SingleItemSubscription(p.explain())

  def explain(verbosity: ExplainVerbosity): SingleItemSubscription[Document] = SingleItemSubscription(p.explain(verbosity))

  def explain[E](explainResultClass: Class[E]): SingleItemSubscription[E] = SingleItemSubscription(
    p.explain(explainResultClass)
  )

  def explain[E](explainResultClass: Class[E], verbosity: ExplainVerbosity): SingleItemSubscription[E] = SingleItemSubscription(
    p.explain(explainResultClass, verbosity)
  )

}
