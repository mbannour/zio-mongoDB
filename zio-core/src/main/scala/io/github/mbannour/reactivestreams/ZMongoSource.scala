package io.github.mbannour.reactivestreams

import com.mongodb.reactivestreams.client.MongoCollection
import io.github.mbannour.result.{ DeleteResult, InsertManyResult, InsertOneResult, UpdateResult }
import com.mongodb.client.model.{ DeleteOptions, InsertManyOptions, InsertOneOptions, ReplaceOptions, UpdateOptions }
import com.mongodb.client.result
import org.mongodb.scala.bson.conversions.Bson
import org.reactivestreams.Publisher
import zio.interop.reactivestreams.Adapters._
import zio.stream.ZStream

import scala.jdk.CollectionConverters._

object ZMongoSource {

  private val DefaultInsertOneOptions = new InsertOneOptions()

  private val DefaultInsertManyOptions = new InsertManyOptions()

  private val DefaultUpdateOptions = new UpdateOptions()

  private val DefaultDeleteOptions = new DeleteOptions()

  private[mbannour] val DefaultReplaceOptions = new ReplaceOptions()

  def default[T](publisher: Publisher[T]): ZStream[Any, Throwable, T] = publisherToStream(publisher, bufferSize = 16)

  /**
    * @param publisher
    * @param bufferSize
    *   the size used as internal buffer. If possible, set to a power of 2 value for best performance.
    */
  def apply[T](publisher: Publisher[T], bufferSize: Int): ZStream[Any, Throwable, T] =
    publisherToStream(publisher, bufferSize)


  def insertOne[T](
      collection: MongoCollection[T],
      element: T,
      options: InsertOneOptions = DefaultInsertOneOptions
  ): ZStream[Any, Throwable, InsertOneResult] =
    publisherToStream(collection.insertOne(element, options), bufferSize = 16).map(r => InsertOneResult(r))

  def insertMany[T](
      collection: MongoCollection[T],
      elements: Seq[T],
      options: InsertManyOptions = DefaultInsertManyOptions
  ): ZStream[Any, Throwable, InsertManyResult] =
    publisherToStream(collection.insertMany(elements.asJava, options), bufferSize = 16).map(r => InsertManyResult(r))


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
    publisherToStream(collection.updateMany(filter, elements.asJava, options), bufferSize = 16).map(r =>
      UpdateResult(r)
    )

  /**
    * @param collection
    *   the mongo db collection to update.
    * @param filter
    *   a document describing the query filter, which may not be null.
    * @param options
    *   options to apply to the operation
    */
  def deleteOne[T](
      collection: MongoCollection[T],
      filter: Bson,
      options: DeleteOptions = DefaultDeleteOptions
  ): ZStream[Any, Throwable, result.DeleteResult => DeleteResult] =
    publisherToStream(collection.deleteOne(filter, options), bufferSize = 16).map(r => DeleteResult(_))

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
    publisherToStream(collection.deleteMany(filter, options), bufferSize = 16).map(r => DeleteResult(r))

  /**
    * @param filter
    *   The query filter to apply the replace operation
    * @param replacement
    *   the replacement document
    * @param collection
    *   the mongo db collection to update.
    * @param options
    *   options to apply to the operation
    */
  def replaceOne[T](
      collection: MongoCollection[T],
      replacement: T,
      filter: Bson,
      options: ReplaceOptions = DefaultReplaceOptions
  ): ZStream[Any, Throwable, UpdateResult] =
    publisherToStream(collection.replaceOne(filter, replacement, options), bufferSize = 16).map(res =>
      UpdateResult(res)
    )

}
