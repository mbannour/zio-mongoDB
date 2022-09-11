package io.github.mbannour.subscriptions

import com.mongodb.client.model.Collation
import com.mongodb.{CursorType, ExplainVerbosity}
import com.mongodb.reactivestreams.client.FindPublisher
import org.bson.{BsonValue, Document}
import org.bson.conversions.Bson
import zio._

import java.lang
import java.util.concurrent.TimeUnit
import scala.collection.mutable.ArrayBuffer

case class FindSubscription[T](p: FindPublisher[T]) extends Subscription[Iterable[T]]{

  override def fetch[F[_]]: IO[Throwable, Iterable[T]] =
    ZIO.async[Any, Throwable, Iterable[T]] { callback =>
      p.subscribe {
        new JavaSubscriber[T] {

          val items = new ArrayBuffer[T]()

          override def onSubscribe(s: JavaSubscription): Unit = s.request(Long.MaxValue)

          override def onNext(t: T): Unit = items += t

          override def onError(t: Throwable): Unit = callback(ZIO.fail(t))

          override def onComplete(): Unit = callback(ZIO.succeed(items.toSeq))
        }
      }
    }

  def first(): SingleItemSubscription[T] = SingleItemSubscription(p.first())

  def filter(filter: Bson): FindSubscription[T] = this.copy(p.filter(filter))

  def limit(limit: Int): FindSubscription[T] = this.copy(p.limit(limit))

  def skip(skip: Int): FindSubscription[T] = this.copy(p.skip(skip))

  def maxTime(maxTime: Long, timeUnit: TimeUnit): FindSubscription[T] = this.copy(p.maxTime(maxTime, timeUnit))

  def maxAwaitTime(maxAwaitTime: Long, timeUnit: TimeUnit): FindSubscription[T] = this.copy(p.maxAwaitTime(maxAwaitTime, timeUnit))

  def projection(projection: Bson): FindSubscription[T] = this.copy(p.projection(projection))

  def sort(sort: Bson): FindSubscription[T] = this.copy(p.sort(sort))

  def noCursorTimeout(noCursorTimeout: Boolean): FindSubscription[T] = this.copy(p.noCursorTimeout(noCursorTimeout))

  def oplogReplay(oplogReplay: Boolean): FindSubscription[T] = this.copy(p.oplogReplay(oplogReplay))

  def partial(partial: Boolean): FindSubscription[T] = this.copy(p.partial(partial))

  def cursorType(cursorType: CursorType): FindSubscription[T] = this.copy(p.cursorType(cursorType))

  def collation(collation: Collation): FindSubscription[T] = this.copy(p.collation(collation))

  def comment(comment: String): FindSubscription[T] = this.copy(p.comment(comment))

  def hint(hint: Bson): FindSubscription[T] = this.copy(p.hint(hint))

  def hintString(hint: String): FindSubscription[T] = this.copy(p.hintString(hint))

  def max(max: Bson): FindSubscription[T] = this.copy(p.max(max))

  def min(min: Bson): FindSubscription[T] = this.copy(p.min(min))

  def returnKey(returnKey: Boolean): FindSubscription[T] = this.copy(p.returnKey(returnKey))

  def showRecordId(showRecordId: Boolean): FindSubscription[T] = this.copy(p.showRecordId(showRecordId))

  def batchSize(batchSize: Int): FindSubscription[T] = this.copy(p.batchSize(batchSize))

  def allowDiskUse(allowDiskUse: Boolean): FindSubscription[T] = this.copy(p.allowDiskUse(allowDiskUse))

  def explain(): SingleItemSubscription[Document] = SingleItemSubscription(p.explain())

  def explain(verbosity: ExplainVerbosity):SingleItemSubscription[Document] = SingleItemSubscription(p.explain(verbosity))

}


final class FindSubscription1[T] private(p: FindPublisher[T]) extends Subscription[Iterable[T]] with FindPublisher[T] {


  override def fetch[F[_]]: IO[Throwable, Iterable[T]] =
    ZIO.async[Any, Throwable, Iterable[T]] { callback =>
      p.subscribe {
        new JavaSubscriber[T] {

          val items = new ArrayBuffer[T]()

          override def onSubscribe(s: JavaSubscription): Unit = s.request(Long.MaxValue)

          override def onNext(t: T): Unit = items += t

          override def onError(t: Throwable): Unit = callback(ZIO.fail(t))

          override def onComplete(): Unit = callback(ZIO.succeed(items.toSeq))
        }
      }
    }

  override def first(): JavaPublisher[T] = p.first()

  override def filter(filter: Bson): FindPublisher[T] = p.filter(filter)

  override def limit(limit: RuntimeFlags): FindPublisher[T] = p.limit(limit)

  override def skip(skip: RuntimeFlags): FindPublisher[T] = p.skip(skip)

  override def maxTime(maxTime: Long, timeUnit: TimeUnit): FindPublisher[T] = p.maxTime(maxTime, timeUnit)

  override def maxAwaitTime(maxAwaitTime: Long, timeUnit: TimeUnit): FindPublisher[T] = p.maxAwaitTime(maxAwaitTime, timeUnit)

  override def projection(projection: Bson): FindPublisher[T] = p.projection(projection)

  override def sort(sort: Bson): FindPublisher[T] = p.sort(sort)

  override def noCursorTimeout(noCursorTimeout: Boolean): FindPublisher[T] = p.noCursorTimeout(noCursorTimeout)

  override def oplogReplay(oplogReplay: Boolean): FindPublisher[T] = p.oplogReplay(oplogReplay)

  override def partial(partial: Boolean): FindPublisher[T] = p.partial(partial)

  override def cursorType(cursorType: CursorType): FindPublisher[T] = p.cursorType(cursorType)

  override def collation(collation: Collation): FindPublisher[T] = p.collation(collation)

  override def comment(comment: String): FindPublisher[T] = p.comment(comment)

  override def comment(comment: BsonValue): FindPublisher[T] = p.comment(comment)

  override def hint(hint: Bson): FindPublisher[T] = p.hint(hint)

  override def hintString(hint: String): FindPublisher[T] = p.hintString(hint)

  override def let(variables: Bson): FindPublisher[T] = p.let(variables)

  override def max(max: Bson): FindPublisher[T] = p.max(max)

  override def min(min: Bson): FindPublisher[T] = p.min(min)

  override def returnKey(returnKey: Boolean): FindPublisher[T] = p.returnKey(returnKey)

  override def showRecordId(showRecordId: Boolean): FindPublisher[T] = p.showRecordId(showRecordId)

  override def batchSize(batchSize: RuntimeFlags): FindPublisher[T] = p.batchSize(batchSize)

  override def allowDiskUse(allowDiskUse: lang.Boolean): FindPublisher[T] = p.allowDiskUse(allowDiskUse)

  override def explain(): JavaPublisher[Document] = p.explain()

  override def explain(verbosity: ExplainVerbosity): JavaPublisher[Document] = p.explain(verbosity)

  override def explain[E](explainResultClass: Class[E]): JavaPublisher[E] = p.explain(explainResultClass)

  override def explain[E](explainResultClass: Class[E], verbosity: ExplainVerbosity): JavaPublisher[E] = p.explain(explainResultClass, verbosity)

  override def subscribe(s: JavaSubscriber[_ >: T]): Unit = new JavaSubscriber[T] {

    val items = new ArrayBuffer[T]()

    override def onSubscribe(s: JavaSubscription): Unit = s.request(Long.MaxValue)

    override def onNext(t: T): Unit = items += t

    override def onError(t: Throwable): Unit = throw t

    override def onComplete(): Unit = items.toSeq
  }
}

object FindSubscription1 {
  def apply[T](p: FindPublisher[T]):FindSubscription1[T] = new FindSubscription1(p)
}
