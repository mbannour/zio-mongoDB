package io.github.mbannour.subscriptions

import com.mongodb.ExplainVerbosity
import com.mongodb.client.model.Collation
import com.mongodb.reactivestreams.client.AggregatePublisher
import io.github.mbannour.DefaultHelper.MapTo
import org.bson.conversions.Bson
import org.mongodb.scala.bson.collection.immutable.Document
import zio.IO

import java.util.concurrent.TimeUnit
import scala.collection.mutable.ArrayBuffer
import scala.reflect.ClassTag

case class AggregateSubscription[T](p: AggregatePublisher[T]) extends Subscription[Iterable[T]] {

  override def fetch[_]: IO[Throwable, Iterable[T]] =
    IO.async[Throwable, Iterable[T]] { callback =>
      p.subscribe {
        new JavaSubscriber[T] {

          val items = new ArrayBuffer[T]()

          override def onSubscribe(s: JavaSubscription): Unit = s.request(Long.MaxValue)

          override def onNext(t: T): Unit = items += t

          override def onError(t: Throwable): Unit = callback(IO.fail(t))

          override def onComplete(): Unit = callback(IO.succeed(items.toSeq))
        }
      }
    }

  /**
    * Enables writing to temporary files. A null value indicates that it's unspecified.
    *
    * @param allowDiskUse true if writing to temporary files is enabled
    * @return this
    * @mongodb.driver.manual reference/command/aggregate/ Aggregation
    */
  def allowDiskUse(allowDiskUse: Boolean): AggregateSubscription[T] = this.copy(p.allowDiskUse(allowDiskUse))


  /**
    * Sets the maximum execution time on the server for this operation.
    *
    * @param maxTime  the max time
    * @param timeUnit the time unit, which may not be null
    * @return this
    * @mongodb.driver.manual reference/method/cursor.maxTimeMS/#cursor.maxTimeMS Max Time
    */
  def maxTime(maxTime: Long, timeUnit: TimeUnit): AggregateSubscription[T] = this.copy(p.maxTime(maxTime, timeUnit))

  /**
    * The maximum amount of time for the server to wait on new documents to satisfy a aggregation.
    *
    * A zero value will be ignored.
    *
    * @param maxAwaitTime the max await time
    * @param timeUnit     the time unit to return the result in
    * @return the maximum await execution time in the given time unit
    * @mongodb.server.release 3.6
    * @since 1.6
    */
  def maxAwaitTime(maxAwaitTime: Long, timeUnit: TimeUnit): AggregateSubscription[T] =
    this.copy(p.maxAwaitTime(maxAwaitTime, timeUnit))

  /**
    * Sets the bypass document level validation flag.
    *
    * <p>Note: This only applies when an $out stage is specified</p>.
    *
    * @param bypassDocumentValidation If true, allows the write to opt-out of document level validation.
    * @return this
    * @since 1.2
    * @mongodb.driver.manual reference/command/aggregate/ Aggregation
    * @mongodb.server.release 3.2
    */
  def bypassDocumentValidation(bypassDocumentValidation: Boolean): AggregateSubscription[T] =
    this.copy(p.bypassDocumentValidation(bypassDocumentValidation))

  /**
    * Sets the collation options
    *
    * <p>A null value represents the server default.</p>
    *
    * @param collation the collation options to use
    * @return this
    * @since 1.3
    * @mongodb.server.release 3.4
    */
  def collation(collation: Collation): AggregateSubscription[T] = this.copy(p.collation(collation))

  /**
    * Sets the comment to the aggregation. A null value means no comment is set.
    *
    * @param comment the comment
    * @return this
    * @mongodb.server.release 3.6
    * @since 1.7
    */
  def comment(comment: String): AggregateSubscription[T] = this.copy(p.comment(comment))

  /**
    * Sets the hint for which index to use. A null value means no hint is set.
    *
    * @param hint the hint
    * @return this
    * @mongodb.server.release 3.6
    * @since 1.7
    */
  def hint(hint: Bson): AggregateSubscription[T] = this.copy(p.hint(hint))

  /**
    * Add top-level variables to the aggregation.
    * <p>
    * For MongoDB 5.0+, the aggregate command accepts a {@code let} option. This option is a document consisting of zero or more
    * fields representing variables that are accessible to the aggregation pipeline.  The key is the name of the variable and the value is
    * a constant in the aggregate expression language. Each parameter name is then usable to access the value of the corresponding
    * expression with the "$$" syntax within aggregate expression contexts which may require the use of $expr or a pipeline.
    * </p>
    *
    * @param variables the variables
    * @return this
    * @since 4.3
    * @mongodb.server.release 5.0
    */
  def let(variables: Bson): AggregateSubscription[T] = this.copy(p.let(variables))

  /**
    * Sets the number of documents to return per batch.
    *
    * <p>Overrides the {@link org.reactivestreams.Subscription# request ( long )} value for setting the batch size, allowing for fine grained
    * control over the underlying cursor.</p>
    *
    * @param batchSize the batch size
    * @return this
    * @since 1.8
    * @mongodb.driver.manual reference/method/cursor.batchSize/#cursor.batchSize Batch Size
    */
  def batchSize(batchSize: Int): AggregateSubscription[T] = this.copy(p.batchSize(batchSize))

  /**
    * Helper to return a publisher limited to the first result.
    *
    * @return a Publisher which will contain a single item.
    * @since 1.8
    */
  def first(): SingleItemSubscription[T] = SingleItemSubscription(p.first())

  /**
    * Explain the execution plan for this operation with the server's default verbosity level
    *
    * @param <E> the type of the document class
    * @param explainResultClass the document class to decode into
    * @return the execution plan
    * @since 4.2
    * @mongodb.driver.manual reference/command/explain/
    * @mongodb.server.release 3.6
    */
  def explain[E]()(implicit e: E MapTo Document, ct: ClassTag[E]): SingleItemSubscription[E] = SingleItemSubscription(
    p.explain(clazz(ct))
  )

  /**
    * Explain the execution plan for this operation with the given verbosity level
    *
    * @param <E> the type of the document class
    * @param explainResultClass the document class to decode into
    * @param verbosity          the verbosity of the explanation
    * @return the execution plan
    * @since 4.2
    * @mongodb.driver.manual reference/command/explain/
    * @mongodb.server.release 3.6
    */
  def explain[E](verbosity: ExplainVerbosity)(implicit e: E MapTo Document, ct: ClassTag[E]): SingleItemSubscription[E] = SingleItemSubscription(
    p.explain(clazz(ct), verbosity)
  )

}