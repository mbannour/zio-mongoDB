package io.github.mbannour.subscriptions

import com.mongodb.client.model.{Collation, MapReduceAction}
import com.mongodb.reactivestreams.client.MapReducePublisher
import org.bson.conversions.Bson
import zio.IO

import java.lang
import java.util.concurrent.TimeUnit
import scala.collection.mutable.ArrayBuffer

case class MapReduceSubscription[T](p: MapReducePublisher[T]) extends Subscription[Iterable[T]] {

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

  def collectionName(collectionName: String): MapReduceSubscription[T] = this.copy(p.collectionName(collectionName))

  def finalizeFunction(finalizeFunction: String): MapReduceSubscription[T] = this.copy(p.finalizeFunction(finalizeFunction))

  def scope(scope: Bson): MapReduceSubscription[T] = this.copy(p.scope(scope))

  def sort(sort: Bson): MapReduceSubscription[T] = this.copy(p.sort(sort))

  def filter(filter: Bson): MapReduceSubscription[T] = this.copy(p.filter(filter))

  def limit(limit: Int): MapReduceSubscription[T] = this.copy(p.limit(limit))

  def jsMode(jsMode: Boolean): MapReduceSubscription[T] = this.copy(p.jsMode(jsMode))

  def verbose(verbose: Boolean): MapReduceSubscription[T] = this.copy(p.verbose(verbose))

  def maxTime(maxTime: Long, timeUnit: TimeUnit): MapReduceSubscription[T] = this.copy(p.maxTime(maxTime, timeUnit))

  def action(action: MapReduceAction): MapReduceSubscription[T] = this.copy(p.action(action))

  def databaseName(databaseName: String): MapReduceSubscription[T] = this.copy(p.databaseName(databaseName))

  def sharded(sharded: Boolean): MapReduceSubscription[T] = this.copy(p.sharded(sharded))

  def nonAtomic(nonAtomic: Boolean): MapReduceSubscription[T] = this.copy(p.nonAtomic(nonAtomic))

  def bypassDocumentValidation(bypassDocumentValidation: lang.Boolean): MapReduceSubscription[T] =
    this.copy(p.bypassDocumentValidation(bypassDocumentValidation))

  def toCollection: CompletedSubscription = CompletedSubscription(p.toCollection)

  def collation(collation: Collation): MapReduceSubscription[T] = this.copy(p.collation(collation))

  def batchSize(batchSize: Int): MapReduceSubscription[T] = this.copy(p.batchSize(batchSize))

  def first(): SingleItemSubscription[T] = SingleItemSubscription(p.first())

}
