package io.github.mbannour.reactivestreams

import com.mongodb.client.result.UpdateResult
import io.github.mbannour.result.{ DeleteResult, InsertManyResult, UpdateResult }
import com.mongodb.reactivestreams.client.MongoCollection
import org.bson.conversions.Bson
import org.mongodb.scala.model.{ DeleteOptions, InsertManyOptions, UpdateOptions }
import org.reactivestreams.Publisher
import zio.stream.ZStream
import zio.interop.reactivestreams._

import scala.jdk.CollectionConverters._

object ZMongoSource {

  def apply[T](query: Publisher[T]): ZStream[Any, Throwable, T] = query.toZIOStream()

  private val DefaultInsertManyOptions = new InsertManyOptions()

  private val DefaultUpdateOptions = new UpdateOptions()

  private val DefaultDeleteOptions = new DeleteOptions()

  def apply[T](publisher: Publisher[T]): ZStream[Any, Throwable, T] = publisher.toZIOStream()

  /**
    * @param publisher
    * @param bufferSize
    *   the size used as internal buffer. If possible, set to a power of 2 value for best performance.
    */
  def apply[T](query: Publisher[T], bufferSize: Int): ZStream[Any, Throwable, T] = query.toZIOStream(qSize = bufferSize)

  def insertMany[T](
      collection: MongoCollection[T],
      elements: Seq[T],
      options: InsertManyOptions = DefaultInsertManyOptions
  ): ZStream[Any, Throwable, InsertManyResult] =
    collection.insertMany(elements.asJava, options).toZIOStream().map(r => InsertManyResult(r))

  /**
    * @param collection
    *   the mongo db collection to update.
    * @param filter
    *   a document describing the query filter, which may not be null.
    * @param update
    *   a pipeline describing the update, which may not be nul
    * @param options
    *   options to apply to the operation
    */
  def updateMany[T <: Bson](
      collection: MongoCollection[T],
      filter: Bson,
      elements: Seq[T],
      options: UpdateOptions = DefaultUpdateOptions
  ): ZStream[Any, Throwable, UpdateResult] =
    collection.updateMany(filter, elements.asJava, options).toZIOStream().map(r => UpdateResult(r))

  /**
    * @param filter
    *   the query filter to apply the delete operation
    * @param collection
    *   the mongo db collection to update.
    * @param options
    *   options to apply to the operation
    */
  def deleteMany[T](
      collection: MongoCollection[T],
      filter: Bson,
      options: DeleteOptions = DefaultDeleteOptions
  ): ZStream[Any, Throwable, DeleteResult] =
    collection.deleteMany(filter, options).toZIOStream().map(r => DeleteResult(r))

}
