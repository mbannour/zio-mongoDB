package io.github.mbannour.subscriptions

import com.mongodb.client.model.Collation
import com.mongodb.client.model.changestream.{ChangeStreamDocument, FullDocument}
import com.mongodb.reactivestreams.client.ChangeStreamPublisher
import io.github.mbannour.DefaultHelper.MapTo
import org.bson.{BsonDocument, BsonTimestamp}
import org.mongodb.scala.bson.collection.immutable.Document
import zio.IO

import java.util.concurrent.TimeUnit
import scala.reflect.ClassTag

case class ChangeStreamSubscription[T](p: ChangeStreamPublisher[T]) extends Subscription[ChangeStreamDocument[T]] {

  override def fetch[_]: IO[Throwable, ChangeStreamDocument[T]] = IO.async[Throwable, ChangeStreamDocument[T]] {
    callback =>
      p.subscribe {
        new JavaSubscriber[ChangeStreamDocument[T]] {
          @volatile
          var docStream: ChangeStreamDocument[T] = _

          override def onSubscribe(s: JavaSubscription): Unit = s.request(Long.MaxValue)

          override def onError(t: Throwable): Unit = callback(IO.fail(t))

          override def onComplete(): Unit = callback(IO.succeed(docStream))

          override def onNext(t: ChangeStreamDocument[T]): Unit = docStream = t
        }
      }
  }

  def fullDocument(fullDocument: FullDocument): ChangeStreamSubscription[T] = this.copy(p.fullDocument(fullDocument))

  def resumeAfter(resumeToken: BsonDocument): ChangeStreamSubscription[T] = this.copy(p.resumeAfter(resumeToken))

  def startAtOperationTime(startAtOperationTime: BsonTimestamp): ChangeStreamSubscription[T] = this.copy(p.startAtOperationTime(startAtOperationTime))

  def startAfter(startAfter: BsonDocument): ChangeStreamSubscription[T] = this.copy(p.startAfter(startAfter))

  def maxAwaitTime(maxAwaitTime: Long, timeUnit: TimeUnit):ChangeStreamSubscription[T] = this.copy(p.maxAwaitTime(maxAwaitTime,timeUnit))

  def collation(collation: Collation): ChangeStreamSubscription[T] = this.copy(p.collation(collation))

  def withDocumentClass[TDocument](implicit e: TDocument MapTo Document, ct: ClassTag[TDocument]): SingleItemSubscription[TDocument] =
    SingleItemSubscription(p.withDocumentClass(clazz(ct)))

  def batchSize(batchSize: Int): ChangeStreamSubscription[T] = this.copy(p.batchSize(batchSize))

  def first(): SingleItemSubscription[ChangeStreamDocument[T]] = SingleItemSubscription(p.first())

}
