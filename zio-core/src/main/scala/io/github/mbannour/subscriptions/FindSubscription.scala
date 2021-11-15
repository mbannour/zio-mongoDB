package io.github.mbannour.subscriptions

import com.mongodb.client.model.Collation
import com.mongodb.{CursorType, ExplainVerbosity}
import com.mongodb.reactivestreams.client.FindPublisher
import org.bson.Document
import org.bson.conversions.Bson
import zio.IO

import java.util.concurrent.TimeUnit
import scala.collection.mutable.ArrayBuffer

case class FindSubscription[T](p: FindPublisher[T]) extends Subscription[Iterable[T]] {

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
