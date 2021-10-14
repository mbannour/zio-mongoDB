package io.github.mbannour

import com.mongodb.bulk.BulkWriteResult
import com.mongodb.client.model._
import com.mongodb.{MongoNamespace, ReadConcern, ReadPreference, WriteConcern, client}
import com.mongodb.reactivestreams.client.{ClientSession, MongoCollection => JMongoCollection}
import io.github.mbannour.DefaultHelper.DefaultsTo
import io.github.mbannour.result.{Completed, DeleteResult, InsertManyResult, InsertOneResult, UpdateResult}
import io.github.mbannour.subscriptions.{AggregateSubscription, ChangeStreamSubscription, CompletedSubscription, DistinctSubscription, FindSubscription, ListIndexesSubscription, MapReduceSubscription, SingleItemSubscription}
import org.bson

import  scala.collection.JavaConverters._
import scala.reflect.ClassTag
import org.bson.codecs.configuration.CodecRegistry
import org.bson.conversions.Bson
import zio.{IO, ZIO}


/**
  * The MongoCollection representation.
  *
  * @param wrapped
  *   the underlying java MongoCollection
  * @tparam TResult
  *   The type that this collection will encode documents from and decode documents to.
  * @since 1.0
  */
case class MongoZioCollection[TResult](private val wrapped: JMongoCollection[TResult]) {

  /**
    * Gets the namespace of this collection.
    *
    * @return
    *   the namespace
    */
  lazy val namespace: MongoNamespace = wrapped.getNamespace

  /**
    * Get the default class to cast any documents returned from the database into.
    *
    * @return
    *   the default class to cast any documents into
    */
  lazy val documentClass: Class[TResult] = wrapped.getDocumentClass

  /**
    * Get the codec registry for the MongoDatabase.
    *
    * @return
    *   the { @link org.bson.codecs.configuration.CodecRegistry}
    */
  lazy val codecRegistry: CodecRegistry = wrapped.getCodecRegistry

  /**
    * Get the read preference for the MongoDatabase.
    *
    * @return
    *   the { @link com.mongodb.ReadPreference}
    */
  lazy val readPreference: ReadPreference = wrapped.getReadPreference

  /**
    * Get the write concern for the MongoDatabase.
    *
    * @return
    *   the { @link com.mongodb.WriteConcern}
    */
  lazy val writeConcern: WriteConcern = wrapped.getWriteConcern

  /**
    * Get the read concern for the MongoDatabase.
    *
    * @return
    *   the [[ReadConcern]]
    * @since 1.1
    */
  lazy val readConcern: ReadConcern = wrapped.getReadConcern

  /**
    * Create a new MongoCollection instance with a different default class to cast any documents returned from the
    * database into..
    *
    * @tparam C
    *   The type that the new collection will encode documents from and decode documents to
    * @return
    *   a new MongoCollection instance with the different default class
    */
  def withDocumentClass[C]()(implicit e: C DefaultsTo Document, ct: ClassTag[C]): MongoZioCollection[C] =
    MongoZioCollection(wrapped.withDocumentClass(ct.runtimeClass.asInstanceOf[Class[C]]))

  /**
    * Create a new MongoCollection instance with a different codec registry.
    *
    * @param codecRegistry
    *   the new { @link org.bson.codecs.configuration.CodecRegistry} for the collection
    * @return
    *   a new MongoCollection instance with the different codec registry
    */
  def withCodecRegistry(codecRegistry: CodecRegistry): MongoZioCollection[TResult] = MongoZioCollection(
    wrapped.withCodecRegistry(codecRegistry)
  )

  /**
    * Create a new MongoCollection instance with a different read preference.
    *
    * @param readPreference
    *   the new { @link com.mongodb.ReadPreference} for the collection
    * @return
    *   a new MongoCollection instance with the different readPreference
    */
  def withReadPreference(readPreference: ReadPreference): MongoZioCollection[TResult] = MongoZioCollection(
    wrapped.withReadPreference(readPreference)
  )

  /**
    * Create a new MongoCollection instance with a different write concern.
    *
    * @param writeConcern
    *   the new { @link com.mongodb.WriteConcern} for the collection
    * @return
    *   a new MongoCollection instance with the different writeConcern
    */
  def withWriteConcern(writeConcern: WriteConcern): MongoZioCollection[TResult] = MongoZioCollection(
    wrapped.withWriteConcern(writeConcern)
  )

  /**
    * Create a new MongoCollection instance with a different read concern.
    *
    * @param readConcern
    *   the new [[ReadConcern]] for the collection
    * @return
    *   a new MongoCollection instance with the different ReadConcern
    * @since 1.1
    */
  def withReadConcern(readConcern: ReadConcern): MongoZioCollection[TResult] = MongoZioCollection(
    wrapped.withReadConcern(readConcern)
  )

  /**
    * Gets an estimate of the count of documents in a collection using collection metadata.
    * @return
    *   a publisher with a single element indicating the estimated number of documents
    * @since 2.4
    */
  def estimatedDocumentCount(): IO[Throwable, Long] =
    SingleItemSubscription(wrapped.estimatedDocumentCount()).fetch.map(long2Long(_))

  /**
    * Gets an estimate of the count of documents in a collection using collection metadata.
    *
    * @param options
    *   the options describing the count
    * @return
    *   a publisher with a single element indicating the estimated number of documents
    * @since 2.4
    */
  def estimatedDocumentCount(options: EstimatedDocumentCountOptions): IO[Throwable, Long] =
    SingleItemSubscription(wrapped.estimatedDocumentCount(options)).fetch.map(long2Long(_))

  /**
    * Counts the number of documents in the collection.
    *
    * <p>
    * Note: For a fast count of the total documents in a collection see {@link #estimatedDocumentCount ( )}.<br>
    * Note: When migrating from {@code count()} to {@code countDocuments()} the following query operators must be replaced:
    * </p>
    * <pre>
    *
    * +-------------+--------------------------------+
    * | Operator    | Replacement                    |
    * +=============+================================+
    * | $where      |  $expr                         |
    * +-------------+--------------------------------+
    * | $near       |  $geoWithin with $center       |
    * +-------------+--------------------------------+
    * | $nearSphere |  $geoWithin with $centerSphere |
    * +-------------+--------------------------------+
    * </pre>
    *
    * @return a ZIO with a single element indicating the number of documents
    * @since 1.9
    */
  def countDocuments(): ZIO[Any, Throwable, Long] =
    SingleItemSubscription(wrapped.countDocuments()).fetch.map(long2Long(_))

  /**
    * Counts the number of documents in the collection according to the given options.
    *
    * <p>
    * Note: For a fast count of the total documents in a collection see {@link #estimatedDocumentCount ( )}.<br>
    * Note: When migrating from {@code count()} to {@code countDocuments()} the following query operators must be replaced:
    * </p>
    * <pre>
    *
    * +-------------+--------------------------------+
    * | Operator    | Replacement                    |
    * +=============+================================+
    * | $where      |  $expr                         |
    * +-------------+--------------------------------+
    * | $near       |  $geoWithin with $center       |
    * +-------------+--------------------------------+
    * | $nearSphere |  $geoWithin with $centerSphere |
    * +-------------+--------------------------------+
    * </pre>
    *
    * @param filter the query filter
    * @return a ZIO with a single element indicating the number of documents
    * @since 1.9
    */
  def countDocuments(filter: Bson): ZIO[Any, Throwable, Long] =
    SingleItemSubscription(wrapped.countDocuments(filter)).fetch.map(long2Long(_))

  /**
    * Counts the number of documents in the collection according to the given options.
    *
    * '''Note:''' For a fast count of the total documents in a collection see [[estimatedDocumentCount()]] When
    * migrating from `count()` to `countDocuments()` the following query operators must be replaced:
    *
    * {{{
    * +-------------+--------------------------------+
    * | Operator    | Replacement                    |
    * +=============+================================+
    * | \$where      |  \$expr                         |
    * +-------------+--------------------------------+
    * | \$near       |  \$geoWithin with \$center       |
    * +-------------+--------------------------------+
    * | \$nearSphere |  \$geoWithin with \$centerSphere |
    * +-------------+--------------------------------+
    * }}}
    * // * // * @param filter the query filter // * @param options the options describing the count // * @return a
    * publisher with a single element indicating the number of documents // * @since 2.4 //
    */
  def countDocuments(filter: Bson, options: CountOptions): IO[Throwable, Long] =
    SingleItemSubscription(wrapped.countDocuments(filter, options)).fetch.map(long2Long(_))

  /**
    * Counts the number of documents in the collection.
    *
    * '''Note:''' For a fast count of the total documents in a collection see [[estimatedDocumentCount()]] When
    * migrating from `count()` to `countDocuments()` the following query operators must be replaced:
    *
    * {{{
    * +-------------+--------------------------------+
    * | Operator    | Replacement                    |
    * +=============+================================+
    * | \$where      |  \$expr                         |
    * +-------------+--------------------------------+
    * | \$near       |  \$geoWithin with \$center       |
    * +-------------+--------------------------------+
    * | \$nearSphere |  \$geoWithin with \$centerSphere |
    * +-------------+--------------------------------+
    * }}}
    *
    * @param clientSession
    *   the client session with which to associate this operation
    * @return
    *   a publisher with a single element indicating the number of documents
    * @since 2.4
    * @note
    *   Requires MongoDB 3.6 or greater
    */
  def countDocuments(clientSession: ClientSession): ZIO[Any, Throwable, Long] =
    SingleItemSubscription(wrapped.countDocuments(clientSession)).fetch.map(long2Long(_))

  /**
    * Counts the number of documents in the collection according to the given options.
    *
    * '''Note:''' For a fast count of the total documents in a collection see [[estimatedDocumentCount()]] When
    * migrating from `count()` to `countDocuments()` the following query operators must be replaced:
    *
    * {{{
    * +-------------+--------------------------------+
    * | Operator    | Replacement                    |
    * +=============+================================+
    * | \$where      |  \$expr                         |
    * +-------------+--------------------------------+
    * | \$near       |  \$geoWithin with \$center       |
    * +-------------+--------------------------------+
    * | \$nearSphere |  \$geoWithin with \$centerSphere |
    * +-------------+--------------------------------+
    * }}}
    *
    * @param clientSession
    *   the client session with which to associate this operation
    * @param filter
    *   the query filter
    * @return
    *   a publisher with a single element indicating the number of documents
    * @since 2.4
    * @note
    *   Requires MongoDB 3.6 or greater
    */
  def countDocuments(clientSession: ClientSession, filter: Bson): ZIO[Any, Throwable, Long] =
    SingleItemSubscription(wrapped.countDocuments(clientSession, filter)).fetch.map(long2Long(_))

  /**
    * Counts the number of documents in the collection according to the given options.
    *
    * '''Note:''' For a fast count of the total documents in a collection see [[estimatedDocumentCount()]] When
    * migrating from `count()` to `countDocuments()` the following query operators must be replaced:
    *
    * {{{
    * +-------------+--------------------------------+
    * | Operator    | Replacement                    |
    * +=============+================================+
    * | \$where      |  \$expr                         |
    * +-------------+--------------------------------+
    * | \$near       |  \$geoWithin with \$center       |
    * +-------------+--------------------------------+
    * | \$nearSphere |  \$geoWithin with \$centerSphere |
    * +-------------+--------------------------------+
    * }}}
    *
    * @param clientSession
    *   the client session with which to associate this operation
    * @param filter
    *   the query filter
    * @param options
    *   the options describing the count
    * @return
    *   a publisher with a single element indicating the number of documents
    * @since 2.4
    * @note
    *   Requires MongoDB 3.6 or greater
    */
  def countDocuments(clientSession: ClientSession, filter: Bson, options: CountOptions): ZIO[Any, Throwable, Long] =
    SingleItemSubscription(wrapped.countDocuments(clientSession, filter, options)).fetch.map(long2Long(_))

  /**
    * Gets the distinct values of the specified field name.
    *
    * [[http://docs.mongodb.org/manual/reference/command/distinct/ Distinct]]
    * @param fieldName
    *   the field name
    * @tparam C
    *   the target type of the observable.
    * @return
    *   an IO emitting the sequence of distinct values
    */
  def distinct[C](fieldName: String)(implicit e: C DefaultsTo TResult, ct: ClassTag[C]): DistinctSubscription[C] =
    DistinctSubscription(wrapped.distinct(fieldName, ct.runtimeClass.asInstanceOf[Class[C]]))

  /**
    * Gets the distinct values of the specified field name.
    *
    * [[http://docs.mongodb.org/manual/reference/command/distinct/ Distinct]]
    * @param fieldName
    *   the field name
    * @param filter
    *   the query filter
    * @tparam C
    *   the target type of the observable.
    * @return
    *   an IO emitting the sequence of distinct values
    */
  def distinct[C](fieldName: String, filter: Bson)(implicit e: C DefaultsTo TResult, ct: ClassTag[C]): DistinctSubscription[C] =
    DistinctSubscription(wrapped.distinct(fieldName, filter, ct.runtimeClass.asInstanceOf[Class[C]]))

  /**
    * Gets the distinct values of the specified field name.
    *
    * [[http://docs.mongodb.org/manual/reference/command/distinct/ Distinct]]
    * @param clientSession
    *   the client session with which to associate this operation
    * @param fieldName
    *   the field name
    * @tparam C
    *   the target type of the observable.
    * @return
    *   an IO emitting the sequence of distinct values
    * @since 2.2
    * @note
    *   Requires MongoDB 3.6 or greater
    */
  def distinct[C](clientSession: ClientSession, fieldName: String)(implicit e: C DefaultsTo TResult, ct: ClassTag[C]): DistinctSubscription[C] =
    DistinctSubscription(wrapped.distinct(clientSession, fieldName, ct.runtimeClass.asInstanceOf[Class[C]]))

  /**
    * Gets the distinct values of the specified field name.
    *
    * [[http://docs.mongodb.org/manual/reference/command/distinct/ Distinct]]
    * @param clientSession
    *   the client session with which to associate this operation
    * @param fieldName
    *   the field name
    * @param filter
    *   the query filter
    * @tparam C
    *   the target type of the observable.
    * @return
    *   an IO emitting the sequence of distinct values
    * @since 2.2
    * @note
    *   Requires MongoDB 3.6 or greater
    */
  def distinct[C](clientSession: ClientSession, fieldName: String, filter: Bson)(implicit e: C DefaultsTo TResult, ct: ClassTag[C]): DistinctSubscription[C] =
    DistinctSubscription(wrapped.distinct(clientSession, fieldName, filter, ct.runtimeClass.asInstanceOf[Class[C]]))

  /**
    * Finds all documents in the collection.
    *
    * [[http://docs.mongodb.org/manual/tutorial/query-documents/ Find]]
    *
    * @tparam C
    *   the target document type of the observable.
    * @return
    *   the find Observable
    */
  def find[C]()(implicit e: C DefaultsTo TResult, ct: ClassTag[C]): FindSubscription[C] =
    FindSubscription(wrapped.find(ct.runtimeClass.asInstanceOf[Class[C]]))

  /**
    * Finds all documents in the collection.
    *
    * [[http://docs.mongodb.org/manual/tutorial/query-documents/ Find]]
    * @param filter
    *   the query filter
    * @tparam C
    *   the target document type of the observable.
    * @return
    *   the find Observable
    */
  def find[C](filter: Bson)(implicit e: C DefaultsTo TResult, ct: ClassTag[C]): FindSubscription[C] =
    FindSubscription(wrapped.find(filter, ct.runtimeClass.asInstanceOf[Class[C]]))

  /**
    * Finds all documents in the collection.
    *
    * [[http://docs.mongodb.org/manual/tutorial/query-documents/ Find]]
    *
    * @param clientSession
    *   the client session with which to associate this operation
    * @tparam C
    *   the target document type of the observable.
    * @return
    *   the find Observable
    * @since 2.2
    * @note
    *   Requires MongoDB 3.6 or greater
    */
  def find[C](clientSession: ClientSession)(implicit e: C DefaultsTo Document,  ct: ClassTag[C]): FindSubscription[C] =
    FindSubscription(wrapped.find[C](clientSession, ct.runtimeClass.asInstanceOf[Class[C]]))

  /**
    * Finds all documents in the collection.
    *
    * [[http://docs.mongodb.org/manual/tutorial/query-documents/ Find]]
    * @param clientSession
    *   the client session with which to associate this operation
    * @param filter
    *   the query filter
    * @tparam C
    *   the target document type of the observable.
    * @return
    *   the find Observable
    * @since 2.2
    * @note
    *   Requires MongoDB 3.6 or greater
    */
  def find[C](clientSession: ClientSession, filter: Bson)(implicit e: C DefaultsTo Document, ct: ClassTag[C]): FindSubscription[C] =
    FindSubscription(wrapped.find(clientSession, filter, ct.runtimeClass.asInstanceOf[Class[C]]))

  /**
    * Aggregates documents according to the specified aggregation pipeline.
    *
    * @param pipeline
    *   the aggregate pipeline
    * @return
    *   an IO containing the result of the aggregation operation
    *   [[http://docs.mongodb.org/manual/aggregation/Aggregation]]
    */
  def aggregate[C](pipeline: Seq[Bson])(implicit e: C DefaultsTo Document, ct: ClassTag[C]): AggregateSubscription[C] =
    AggregateSubscription(wrapped.aggregate(pipeline.asJava, ct.runtimeClass.asInstanceOf[Class[C]]))

  /**
    * Aggregates documents according to the specified aggregation pipeline.
    *
    * @param clientSession
    *   the client session with which to associate this operation
    * @param pipeline
    *   the aggregate pipeline
    * @return
    *   an IO containing the result of the aggregation operation
    *   [[http://docs.mongodb.org/manual/aggregation/Aggregation]]
    * @since 2.2
    * @note
    *   Requires MongoDB 3.6 or greater
    */
  def aggregate[C](clientSession: ClientSession, pipeline: Seq[Bson])(implicit e: C DefaultsTo Document, ct: ClassTag[C]): AggregateSubscription[C] =
    AggregateSubscription(wrapped.aggregate(clientSession, pipeline.asJava, ct.runtimeClass.asInstanceOf[Class[C]]))

  /**
    * Aggregates documents according to the specified map-reduce function.
    *
    * @param mapFunction
    *   A JavaScript function that associates or "maps" a value with a key and emits the key and value pair.
    * @param reduceFunction
    *   A JavaScript function that "reduces" to a single object all the values associated with a particular key.
    * @tparam C
    *   the target document type of the observable.
    * @return
    *   an IO containing the result of the map-reduce operation
    *   [[http://docs.mongodb.org/manual/reference/command/mapReduce/map-reduce]]
    */
  def mapReduce[C](mapFunction: String, reduceFunction: String)(implicit e: C DefaultsTo Document, ct: ClassTag[C]): MapReduceSubscription[C] =
    MapReduceSubscription(wrapped.mapReduce(mapFunction, reduceFunction, ct.runtimeClass.asInstanceOf[Class[C]]))

  /**
    * Aggregates documents according to the specified map-reduce function.
    *
    * @param clientSession
    *   the client session with which to associate this operation
    * @param mapFunction
    *   A JavaScript function that associates or "maps" a value with a key and emits the key and value pair.
    * @param reduceFunction
    *   A JavaScript function that "reduces" to a single object all the values associated with a particular key.
    * @tparam C
    *   the target document type of the observable.
    * @return
    *   an IO containing the result of the map-reduce operation
    *   [[http://docs.mongodb.org/manual/reference/command/mapReduce/ map-reduce]]
    * @since 2.2
    * @note
    *   Requires MongoDB 3.6 or greater
    */
  def mapReduce[C](clientSession: ClientSession, mapFunction: String, reduceFunction: String)(implicit e: C DefaultsTo Document, ct: ClassTag[C]): MapReduceSubscription[C] =
    MapReduceSubscription(wrapped.mapReduce(clientSession, mapFunction, reduceFunction, ct.runtimeClass.asInstanceOf[Class[C]]))

  /**
    * Executes a mix of inserts, updates, replaces, and deletes.
    *
    * @param requests
    *   the writes to execute
    * @return
    *   an IO with a single element the BulkWriteResult
    */
  def bulkWrite(requests: Seq[_ <: WriteModel[_ <: TResult]]): IO[Throwable, BulkWriteResult] =
    SingleItemSubscription(wrapped.bulkWrite(requests.asJava)).fetch

  /**
    * Executes a mix of inserts, updates, replaces, and deletes.
    *
    * @param requests
    *   the writes to execute
    * @param options
    *   the options to apply to the bulk write operation
    * @return
    *   an IO with a single element the BulkWriteResult
    */
  def bulkWrite(requests: Seq[_ <: WriteModel[_ <: TResult]], options: BulkWriteOptions): IO[Throwable, BulkWriteResult] =
    SingleItemSubscription(wrapped.bulkWrite(requests.asJava, options)).fetch

  /**
    * Executes a mix of inserts, updates, replaces, and deletes.
    *
    * @param clientSession
    *   the client session with which to associate this operation
    * @param requests
    *   the writes to execute
    * @return
    *   an IO with a single element the BulkWriteResult
    * @since 2.2
    * @note
    *   Requires MongoDB 3.6 or greater
    */
  def bulkWrite(
                 clientSession: ClientSession,
                 requests: Seq[_ <: WriteModel[_ <: TResult]]
               ): IO[Throwable, BulkWriteResult] =
    SingleItemSubscription(wrapped.bulkWrite(clientSession, requests.asJava)).fetch

  /**
    * Executes a mix of inserts, updates, replaces, and deletes.
    *
    * @param clientSession
    *   the client session with which to associate this operation
    * @param requests
    *   the writes to execute
    * @param options
    *   the options to apply to the bulk write operation
    * @return
    *   an IO with a single element the BulkWriteResult
    * @since 2.2
    * @note
    *   Requires MongoDB 3.6 or greater
    */
  def bulkWrite(
                 clientSession: ClientSession,
                 requests: Seq[_ <: WriteModel[_ <: TResult]],
                 options: BulkWriteOptions
               ): IO[Throwable, BulkWriteResult] =
    SingleItemSubscription(wrapped.bulkWrite(clientSession, requests.asJava, options)).fetch

  /**
    * Inserts the provided document. If the document is missing an identifier, the driver should generate one.
    *
    * @param document
    *   the document to insert
    * @return
    *   an IO with a single element indicating when the operation has completed or with either a
    *   com.mongodb.DuplicateKeyException or com.mongodb.MongoException
    */
  def insertOne(document: TResult): IO[Throwable, InsertOneResult] =
    SingleItemSubscription(wrapped.insertOne(document)).fetch.map(InsertOneResult)

  /**
    * Inserts the provided document. If the document is missing an identifier, the driver should generate one.
    *
    * @param document
    *   the document to insert
    * @param options
    *   the options to apply to the operation
    * @return
    *   an IO with a single element indicating when the operation has completed or with either a
    *   com.mongodb.DuplicateKeyException or com.mongodb.MongoException
    * @since 1.1
    */
  def insertOne(document: TResult, options: InsertOneOptions): IO[Throwable, InsertOneResult] =
    SingleItemSubscription(wrapped.insertOne(document, options)).fetch.map(InsertOneResult)

  /**
    * Inserts the provided document. If the document is missing an identifier, the driver should generate one.
    *
    * @param clientSession
    *   the client session with which to associate this operation
    * @param document
    *   the document to insert
    * @return
    *   an IO with a single element indicating when the operation has completed or with either a
    *   com.mongodb.DuplicateKeyException or com.mongodb.MongoException
    * @since 2.2
    * @note
    *   Requires MongoDB 3.6 or greater
    */
  def insertOne(clientSession: ClientSession, document: TResult): IO[Throwable, InsertOneResult] =
    SingleItemSubscription(wrapped.insertOne(clientSession, document)).fetch.map(InsertOneResult)

  /**
    * Inserts the provided document. If the document is missing an identifier, the driver should generate one.
    *
    * @param clientSession
    *   the client session with which to associate this operation
    * @param document
    *   the document to insert
    * @param options
    *   the options to apply to the operation
    * @return
    *   an IO with a single element indicating when the operation has completed or with either a
    *   com.mongodb.DuplicateKeyException or com.mongodb.MongoException
    * @since 2.2
    * @note
    *   Requires MongoDB 3.6 or greater
    */
  def insertOne(
                 clientSession: ClientSession,
                 document: TResult,
                 options: InsertOneOptions
               ): IO[Throwable, InsertOneResult] =
    SingleItemSubscription(wrapped.insertOne(clientSession, document, options)).fetch.map(InsertOneResult)

  /**
    * Inserts a batch of documents. The preferred way to perform bulk inserts is to use the BulkWrite API. However, when
    * talking with a server &lt; 2.6, using this method will be faster due to constraints in the bulk API related to
    * error handling.
    *
    * @param documents
    *   the documents to insert
    * @return
    *   an IO with a single element indicating when the operation has completed or with either a
    *   com.mongodb.DuplicateKeyException or com.mongodb.MongoException
    */
  def insertMany(documents: Seq[_ <: TResult]): IO[Throwable, InsertManyResult] =
    SingleItemSubscription(wrapped.insertMany(documents.asJava)).fetch.map(InsertManyResult)

  /**
    * Inserts a batch of documents. The preferred way to perform bulk inserts is to use the BulkWrite API. However, when
    * talking with a server &lt; 2.6, using this method will be faster due to constraints in the bulk API related to
    * error handling.
    *
    * @param documents
    *   the documents to insert
    * @param options
    *   the options to apply to the operation
    * @return
    *   an IO with a single element indicating when the operation has completed or with either a
    *   com.mongodb.DuplicateKeyException or com.mongodb.MongoException
    */
  def insertMany(documents: Seq[_ <: TResult], options: InsertManyOptions): IO[Throwable, InsertManyResult] =
    SingleItemSubscription(wrapped.insertMany(documents.asJava, options)).fetch.map(InsertManyResult)

  /**
    * Inserts a batch of documents. The preferred way to perform bulk inserts is to use the BulkWrite API.
    *
    * @param clientSession
    *   the client session with which to associate this operation
    * @param documents
    *   the documents to insert
    * @return
    *   an IO with a single element indicating when the operation has completed or with either a
    *   com.mongodb.DuplicateKeyException or com.mongodb.MongoException
    * @since 2.2
    * @note
    *   Requires MongoDB 3.6 or greater
    */
  def insertMany(clientSession: ClientSession, documents: Seq[_ <: TResult]): IO[Throwable, InsertManyResult] =
    SingleItemSubscription(wrapped.insertMany(clientSession, documents.asJava)).fetch.map(InsertManyResult)

  /**
    * Inserts a batch of documents. The preferred way to perform bulk inserts is to use the BulkWrite API.
    *
    * @param clientSession
    *   the client session with which to associate this operation
    * @param documents
    *   the documents to insert
    * @param options
    *   the options to apply to the operation
    * @return
    *   an IO with a single element indicating when the operation has completed or with either a
    *   com.mongodb.DuplicateKeyException or com.mongodb.MongoException
    * @since 2.2
    * @note
    *   Requires MongoDB 3.6 or greater
    */
  def insertMany(
                  clientSession: ClientSession,
                  documents: Seq[_ <: TResult],
                  options: InsertManyOptions
                ): IO[Throwable, InsertManyResult] =
    SingleItemSubscription(wrapped.insertMany(clientSession, documents.asJava, options)).fetch.map(InsertManyResult)

  /**
    * Removes at most one document from the collection that matches the given filter. If no documents match, the
    * collection is not modified.
    *
    * @param filter
    *   the query filter to apply the the delete operation
    * @return
    *   an IO with a single element the DeleteResult or with an com.mongodb.MongoException
    */
  def deleteOne(filter: Bson): IO[Throwable, DeleteResult] =
    SingleItemSubscription(wrapped.deleteOne(filter)).fetch.map(DeleteResult)

  /**
    * Removes at most one document from the collection that matches the given filter. If no documents match, the
    * collection is not modified.
    *
    * @param filter
    *   the query filter to apply the the delete operation
    * @param options
    *   the options to apply to the delete operation
    * @return
    *   an IO with a single element the DeleteResult or with an com.mongodb.MongoException
    * @since 1.2
    */
  def deleteOne(filter: Bson, options: DeleteOptions): IO[Throwable, DeleteResult] =
    SingleItemSubscription(wrapped.deleteOne(filter, options)).fetch.map(DeleteResult)

  /**
    * Removes at most one document from the collection that matches the given filter. If no documents match, the
    * collection is not modified.
    *
    * @param clientSession
    *   the client session with which to associate this operation
    * @param filter
    *   the query filter to apply the the delete operation
    * @return
    *   an IO with a single element the DeleteResult or with an com.mongodb.MongoException
    * @since 2.2
    * @note
    *   Requires MongoDB 3.6 or greater
    */
  def deleteOne(clientSession: ClientSession, filter: Bson): IO[Throwable, DeleteResult] =
    SingleItemSubscription(wrapped.deleteOne(clientSession, filter)).fetch.map(DeleteResult)

  /**
    * Removes at most one document from the collection that matches the given filter. If no documents match, the
    * collection is not modified.
    *
    * @param clientSession
    *   the client session with which to associate this operation
    * @param filter
    *   the query filter to apply the the delete operation
    * @param options
    *   the options to apply to the delete operation
    * @return
    *   an IO with a single element the DeleteResult or with an com.mongodb.MongoException
    * @since 2.2
    * @note
    *   Requires MongoDB 3.6 or greater
    */
  def deleteOne(clientSession: ClientSession, filter: Bson, options: DeleteOptions): IO[Throwable, DeleteResult] =
    SingleItemSubscription(wrapped.deleteOne(clientSession, filter, options)).fetch.map(DeleteResult)

  /**
    * Removes all documents from the collection that match the given query filter. If no documents match, the collection
    * is not modified.
    *
    * @param filter
    *   the query filter to apply the the delete operation
    * @return
    *   an IO with a single element the DeleteResult or with an com.mongodb.MongoException
    */
  def deleteMany(filter: Bson): IO[Throwable, DeleteResult] =
    SingleItemSubscription(wrapped.deleteMany(filter)).fetch.map(DeleteResult)

  /**
    * Removes all documents from the collection that match the given query filter. If no documents match, the collection
    * is not modified.
    *
    * @param filter
    *   the query filter to apply the the delete operation
    * @param options
    *   the options to apply to the delete operation
    * @return
    *   an IO with a single element the DeleteResult or with an com.mongodb.MongoException
    * @since 1.2
    */
  def deleteMany(filter: Bson, options: DeleteOptions): IO[Throwable, DeleteResult] =
    SingleItemSubscription(wrapped.deleteMany(filter, options)).fetch.map(DeleteResult)

  /**
    * Removes all documents from the collection that match the given query filter. If no documents match, the collection
    * is not modified.
    *
    * @param clientSession
    *   the client session with which to associate this operation
    * @param filter
    *   the query filter to apply the the delete operation
    * @return
    *   an IO with a single element the DeleteResult or with an com.mongodb.MongoException
    * @since 2.2
    * @note
    *   Requires MongoDB 3.6 or greater
    */
  def deleteMany(clientSession: ClientSession, filter: Bson): IO[Throwable, DeleteResult] =
    SingleItemSubscription(wrapped.deleteMany(clientSession, filter)).fetch.map(DeleteResult)

  /**
    * Removes all documents from the collection that match the given query filter. If no documents match, the collection
    * is not modified.
    *
    * @param clientSession
    *   the client session with which to associate this operation
    * @param filter
    *   the query filter to apply the the delete operation
    * @param options
    *   the options to apply to the delete operation
    * @return
    *   an IO with a single element the DeleteResult or with an com.mongodb.MongoException
    * @since 2.2
    * @note
    *   Requires MongoDB 3.6 or greater
    */
  def deleteMany(clientSession: ClientSession, filter: Bson, options: DeleteOptions): IO[Throwable, DeleteResult] =
    SingleItemSubscription(wrapped.deleteMany(clientSession, filter, options)).fetch.map(DeleteResult)

  /**
    * Replace a document in the collection according to the specified arguments.
    *
    * [[http://docs.mongodb.org/manual/tutorial/modify-documents/#replace-the-document Replace]]
    * @param filter
    *   the query filter to apply the the replace operation
    * @param replacement
    *   the replacement document
    * @return
    *   an IO with a single element the UpdateResult
    */
  def replaceOne(filter: Bson, replacement: TResult): IO[Throwable, client.result.UpdateResult] =
    SingleItemSubscription(wrapped.replaceOne(filter, replacement)).fetch

  /**
    * Replace a document in the collection according to the specified arguments.
    *
    * [[http://docs.mongodb.org/manual/tutorial/modify-documents/#replace-the-document Replace]]
    * @param clientSession
    *   the client session with which to associate this operation
    * @param filter
    *   the query filter to apply the the replace operation
    * @param replacement
    *   the replacement document
    * @return
    *   an IO with a single element the UpdateResult
    * @since 2.2
    * @note
    *   Requires MongoDB 3.6 or greater
    */
  def replaceOne(clientSession: ClientSession, filter: Bson, replacement: TResult): IO[Throwable, UpdateResult] =
    SingleItemSubscription(wrapped.replaceOne(clientSession, filter, replacement)).fetch.map(UpdateResult)


  /**
    * Replace a document in the collection according to the specified arguments.
    *
    * [[http://docs.mongodb.org/manual/tutorial/modify-documents/#replace-the-document Replace]]
    * @param filter
    *   the query filter to apply the the replace operation
    * @param replacement
    *   the replacement document
    * @param options
    *   the options to apply to the replace operation
    * @return
    *   an IO with a single element the UpdateResult
    */
  def replaceOne(filter: Bson, replacement: TResult, options: ReplaceOptions): IO[Throwable, UpdateResult] =
    SingleItemSubscription(wrapped.replaceOne(filter, replacement, options)).fetch.map(UpdateResult)


  /**
    * Replace a document in the collection according to the specified arguments.
    *
    * [[http://docs.mongodb.org/manual/tutorial/modify-documents/#replace-the-document Replace]]
    * @param clientSession
    *   the client session with which to associate this operation
    * @param filter
    *   the query filter to apply the the replace operation
    * @param replacement
    *   the replacement document
    * @param options
    *   the options to apply to the replace operation
    * @return
    *   an IO with a single element the UpdateResult
    * @since 2.2
    * @note
    *   Requires MongoDB 3.6 or greater
    */
  def replaceOne(clientSession: ClientSession, filter: Bson, replacement: TResult, options: ReplaceOptions): IO[Throwable, UpdateResult] =
    SingleItemSubscription(wrapped.replaceOne(clientSession, filter, replacement, options)).fetch.map(UpdateResult)


  /**
    * Update a single document in the collection according to the specified arguments.
    *
    * [[http://docs.mongodb.org/manual/tutorial/modify-documents/ Updates]]
    * [[http://docs.mongodb.org/manual/reference/operator/update/ Update Operators]]
    * @param filter
    *   a document describing the query filter, which may not be null. This can be of any type for which a `Codec` is
    *   registered
    * @param update
    *   a document describing the update, which may not be null. The update to apply must include only update operators.
    *   This can be of any type for which a `Codec` is registered
    * @return
    *   an IO with a single element the UpdateResult
    */
  def updateOne(filter: Bson, update: Bson): IO[Throwable, UpdateResult] =
    SingleItemSubscription(wrapped.updateOne(filter, update)).fetch.map(UpdateResult)


  /**
    * Update a single document in the collection according to the specified arguments.
    *
    * [[http://docs.mongodb.org/manual/tutorial/modify-documents/ Updates]]
    * [[http://docs.mongodb.org/manual/reference/operator/update/ Update Operators]]
    * @param filter
    *   a document describing the query filter, which may not be null. This can be of any type for which a `Codec` is
    *   registered
    * @param update
    *   a document describing the update, which may not be null. The update to apply must include only update operators.
    *   This can be of any type for which a `Codec` is registered
    * @param options
    *   the options to apply to the update operation
    * @return
    *   an IO with a single element the UpdateResult
    */
  def updateOne(filter: Bson, update: Bson, options: UpdateOptions): IO[Throwable, UpdateResult] =
    SingleItemSubscription(wrapped.updateOne(filter, update, options)).fetch.map(UpdateResult)


  /**
    * Update a single document in the collection according to the specified arguments.
    *
    * [[http://docs.mongodb.org/manual/tutorial/modify-documents/ Updates]]
    * [[http://docs.mongodb.org/manual/reference/operator/update/ Update Operators]]
    * @param clientSession
    *   the client session with which to associate this operation
    * @param filter
    *   a document describing the query filter, which may not be null. This can be of any type for which a `Codec` is
    *   registered
    * @param update
    *   a document describing the update, which may not be null. The update to apply must include only update operators.
    *   This can be of any type for which a `Codec` is registered
    * @return
    *   an IO with a single element the UpdateResult
    * @since 2.2
    * @note
    *   Requires MongoDB 3.6 or greater
    */
  def updateOne(clientSession: ClientSession, filter: Bson, update: Bson): IO[Throwable, UpdateResult] =
    SingleItemSubscription(wrapped.updateOne(clientSession, filter, update)).fetch.map(UpdateResult)


  /**
    * Update a single document in the collection according to the specified arguments.
    *
    * [[http://docs.mongodb.org/manual/tutorial/modify-documents/ Updates]]
    * [[http://docs.mongodb.org/manual/reference/operator/update/ Update Operators]]
    * @param clientSession
    *   the client session with which to associate this operation
    * @param filter
    *   a document describing the query filter, which may not be null. This can be of any type for which a `Codec` is
    *   registered
    * @param update
    *   a document describing the update, which may not be null. The update to apply must include only update operators.
    *   This can be of any type for which a `Codec` is registered
    * @param options
    *   the options to apply to the update operation
    * @return
    *   an IO with a single element the UpdateResult
    * @since 2.2
    * @note
    *   Requires MongoDB 3.6 or greater
    */
  def updateOne(clientSession: ClientSession, filter: Bson, update: Bson, options: UpdateOptions): IO[Throwable, UpdateResult] =
    SingleItemSubscription(wrapped.updateOne(clientSession, filter, update, options)).fetch.map(UpdateResult)


  /**
    * Update a single document in the collection according to the specified arguments.
    *
    * [[http://docs.mongodb.org/manual/tutorial/modify-documents/ Updates]]
    * [[http://docs.mongodb.org/manual/reference/operator/update/ Update Operators]]
    * @param filter
    *   a document describing the query filter, which may not be null. This can be of any type for which a `Codec` is
    *   registered
    * @param update
    *   a pipeline describing the update.
    * @return
    *   an IO with a single element the UpdateResult
    * @since 2.7
    * @note
    *   Requires MongoDB 4.2 or greater
    */
  def updateOne(filter: Bson, update: Seq[Bson]): IO[Throwable, UpdateResult] =
    SingleItemSubscription(wrapped.updateOne(filter, update.asJava)).fetch.map(UpdateResult)


  /**
    * Update a single document in the collection according to the specified arguments.
    *
    * [[http://docs.mongodb.org/manual/tutorial/modify-documents/ Updates]]
    * [[http://docs.mongodb.org/manual/reference/operator/update/ Update Operators]]
    * @param filter
    *   a document describing the query filter, which may not be null. This can be of any type for which a `Codec` is
    *   registered
    * @param update
    *   a pipeline describing the update.
    * @param options
    *   the options to apply to the update operation
    * @return
    *   an IO with a single element the UpdateResult
    * @since 2.7
    * @note
    *   Requires MongoDB 4.2 or greater
    */
  def updateOne(filter: Bson, update: Seq[Bson], options: UpdateOptions): IO[Throwable, UpdateResult] =
    SingleItemSubscription(wrapped.updateOne(filter, update.asJava, options)).fetch.map(UpdateResult)


  /**
    * Update a single document in the collection according to the specified arguments.
    *
    * [[http://docs.mongodb.org/manual/tutorial/modify-documents/ Updates]]
    * [[http://docs.mongodb.org/manual/reference/operator/update/ Update Operators]]
    * @param clientSession
    *   the client session with which to associate this operation
    * @param filter
    *   a document describing the query filter, which may not be null. This can be of any type for which a `Codec` is
    *   registered
    * @param update
    *   a pipeline describing the update.
    * @return
    *   an IO with a single element the UpdateResult
    * @since 2.7
    * @note
    *   Requires MongoDB 4.2 or greater
    */
  def updateOne(clientSession: ClientSession, filter: Bson, update: Seq[Bson]): IO[Throwable, UpdateResult] =
    SingleItemSubscription(wrapped.updateOne(clientSession, filter, update.asJava)).fetch.map(UpdateResult)


  /**
    * Update a single document in the collection according to the specified arguments.
    *
    * [[http://docs.mongodb.org/manual/tutorial/modify-documents/ Updates]]
    * [[http://docs.mongodb.org/manual/reference/operator/update/ Update Operators]]
    * @param clientSession
    *   the client session with which to associate this operation
    * @param filter
    *   a document describing the query filter, which may not be null. This can be of any type for which a `Codec` is
    *   registered
    * @param update
    *   a pipeline describing the update.
    * @param options
    *   the options to apply to the update operation
    * @return
    *   an IO with a single element the UpdateResult
    * @since 2.7
    * @note
    *   Requires MongoDB 4.2 or greater
    */
  def updateOne(clientSession: ClientSession, filter: Bson, update: Seq[Bson], options: UpdateOptions): IO[Throwable, UpdateResult] =
    SingleItemSubscription(wrapped.updateOne(clientSession, filter, update.asJava, options)).fetch.map(UpdateResult)

  /**
    * Update a single document in the collection according to the specified arguments.
    *
    * [[http://docs.mongodb.org/manual/tutorial/modify-documents/ Updates]]
    * [[http://docs.mongodb.org/manual/reference/operator/update/ Update Operators]]
    * @param filter
    *   a document describing the query filter, which may not be null. This can be of any type for which a `Codec` is
    *   registered
    * @param update
    *   a document describing the update, which may not be null. The update to apply must include only update operators.
    *   This can be of any type for which a `Codec` is registered
    * @return
    *   an IO with a single element the UpdateResult
    */
  def updateMany(filter: Bson, update: Bson): IO[Throwable, UpdateResult] =
    SingleItemSubscription(wrapped.updateMany(filter, update)).fetch.map(UpdateResult)

  /**
    * Update a single document in the collection according to the specified arguments.
    *
    * [[http://docs.mongodb.org/manual/tutorial/modify-documents/ Updates]]
    * [[http://docs.mongodb.org/manual/reference/operator/update/ Update Operators]]
    * @param filter
    *   a document describing the query filter, which may not be null. This can be of any type for which a `Codec` is
    *   registered
    * @param update
    *   a document describing the update, which may not be null. The update to apply must include only update operators.
    *   This can be of any type for which a `Codec` is registered
    * @param options
    *   the options to apply to the update operation
    * @return
    *   an IO with a single element the UpdateResult
    */
  def updateMany(filter: Bson, update: Bson, options: UpdateOptions): IO[Throwable, UpdateResult] =
    SingleItemSubscription(wrapped.updateMany(filter, update, options)).fetch.map(UpdateResult)

  /**
    * Update a single document in the collection according to the specified arguments.
    *
    * [[http://docs.mongodb.org/manual/tutorial/modify-documents/ Updates]]
    * [[http://docs.mongodb.org/manual/reference/operator/update/ Update Operators]]
    * @param clientSession
    *   the client session with which to associate this operation
    * @param filter
    *   a document describing the query filter, which may not be null. This can be of any type for which a `Codec` is
    *   registered
    * @param update
    *   a document describing the update, which may not be null. The update to apply must include only update operators.
    *   This can be of any type for which a `Codec` is registered
    * @return
    *   an IO with a single element the UpdateResult
    * @since 2.2
    * @note
    *   Requires MongoDB 3.6 or greater
    */
  def updateMany(clientSession: ClientSession, filter: Bson, update: Bson): IO[Throwable, UpdateResult] =
    SingleItemSubscription(wrapped.updateMany(clientSession, filter, update)).fetch.map(UpdateResult)

  /**
    * Update a single document in the collection according to the specified arguments.
    *
    * [[http://docs.mongodb.org/manual/tutorial/modify-documents/ Updates]]
    * [[http://docs.mongodb.org/manual/reference/operator/update/ Update Operators]]
    * @param clientSession
    *   the client session with which to associate this operation
    * @param filter
    *   a document describing the query filter, which may not be null. This can be of any type for which a `Codec` is
    *   registered
    * @param update
    *   a document describing the update, which may not be null. The update to apply must include only update operators.
    *   This can be of any type for which a `Codec` is registered
    * @param options
    *   the options to apply to the update operation
    * @return
    *   an IO with a single element the UpdateResult
    * @since 2.2
    * @note
    *   Requires MongoDB 3.6 or greater
    */
  def updateMany(
                  clientSession: ClientSession,
                  filter: Bson,
                  update: Bson,
                  options: UpdateOptions
                ): IO[Throwable, UpdateResult] =
    SingleItemSubscription(wrapped.updateMany(clientSession, filter, update, options)).fetch.map(UpdateResult)

  /**
    * Update a single document in the collection according to the specified arguments.
    *
    * [[http://docs.mongodb.org/manual/tutorial/modify-documents/ Updates]]
    * [[http://docs.mongodb.org/manual/reference/operator/update/ Update Operators]]
    * @param filter
    *   a document describing the query filter, which may not be null. This can be of any type for which a `Codec` is
    *   registered
    * @param update
    *   a pipeline describing the update.
    * @return
    *   an IO with a single element the UpdateResult
    * @since 2.7
    * @note
    *   Requires MongoDB 4.2 or greater
    */
  def updateMany(filter: Bson, update: Seq[Bson]): IO[Throwable, UpdateResult] =
    SingleItemSubscription(wrapped.updateMany(filter, update.asJava)).fetch.map(UpdateResult)

  /**
    * Update a single document in the collection according to the specified arguments.
    *
    * [[http://docs.mongodb.org/manual/tutorial/modify-documents/ Updates]]
    * [[http://docs.mongodb.org/manual/reference/operator/update/ Update Operators]]
    * @param filter
    *   a document describing the query filter, which may not be null. This can be of any type for which a `Codec` is
    *   registered
    * @param update
    *   a pipeline describing the update.
    * @param options
    *   the options to apply to the update operation
    * @return
    *   an IO with a single element the UpdateResult
    * @since 2.7
    * @note
    *   Requires MongoDB 4.2 or greater
    */
  def updateMany(filter: Bson, update: Seq[Bson], options: UpdateOptions): IO[Throwable, UpdateResult] =
    SingleItemSubscription(wrapped.updateMany(filter, update.asJava, options)).fetch.map(UpdateResult)

  /**
    * Update a single document in the collection according to the specified arguments.
    *
    * [[http://docs.mongodb.org/manual/tutorial/modify-documents/ Updates]]
    * [[http://docs.mongodb.org/manual/reference/operator/update/ Update Operators]]
    * @param clientSession
    *   the client session with which to associate this operation
    * @param filter
    *   a document describing the query filter, which may not be null. This can be of any type for which a `Codec` is
    *   registered
    * @param update
    *   a pipeline describing the update.
    * @return
    *   an IO with a single element the UpdateResult
    * @since 2.7
    * @note
    *   Requires MongoDB 4.2 or greater
    */
  def updateMany(clientSession: ClientSession, filter: Bson, update: Seq[Bson]): IO[Throwable, UpdateResult] =
    SingleItemSubscription(wrapped.updateMany(clientSession, filter, update.asJava)).fetch.map(UpdateResult)

  /**
    * Update a single document in the collection according to the specified arguments.
    *
    * [[http://docs.mongodb.org/manual/tutorial/modify-documents/ Updates]]
    * [[http://docs.mongodb.org/manual/reference/operator/update/ Update Operators]]
    * @param clientSession
    *   the client session with which to associate this operation
    * @param filter
    *   a document describing the query filter, which may not be null. This can be of any type for which a `Codec` is
    *   registered
    * @param update
    *   a pipeline describing the update.
    * @param options
    *   the options to apply to the update operation
    * @return
    *   an IO with a single element the UpdateResult
    * @since 2.7
    * @note
    *   Requires MongoDB 4.2 or greater
    */
  def updateMany(
                  clientSession: ClientSession,
                  filter: Bson,
                  update: Seq[Bson],
                  options: UpdateOptions
                ): IO[Throwable, UpdateResult] =
    SingleItemSubscription(wrapped.updateMany(clientSession, filter, update.asJava, options)).fetch.map(UpdateResult)

  /**
    * Atomically find a document and remove it.
    *
    * @param filter
    *   the query filter to find the document with
    * @return
    *   an IO with a single element the document that was removed. If no documents matched the query filter, then
    *   null will be returned
    */
  def findOneAndDelete(filter: Bson): IO[Throwable, TResult] =
    SingleItemSubscription(wrapped.findOneAndDelete(filter)).fetch

  /**
    * Atomically find a document and remove it.
    *
    * @param filter
    *   the query filter to find the document with
    * @param options
    *   the options to apply to the operation
    * @return
    *   an IO with a single element the document that was removed. If no documents matched the query filter, then
    *   null will be returned
    */
  def findOneAndDelete(filter: Bson, options: FindOneAndDeleteOptions): IO[Throwable, TResult] =
    SingleItemSubscription(wrapped.findOneAndDelete(filter, options)).fetch

  /**
    * Atomically find a document and remove it.
    *
    * @param clientSession
    *   the client session with which to associate this operation
    * @param filter
    *   the query filter to find the document with
    * @return
    *   an IO with a single element the document that was removed. If no documents matched the query filter, then
    *   null will be returned
    * @since 2.2
    * @note
    *   Requires MongoDB 3.6 or greater
    */
  def findOneAndDelete(clientSession: ClientSession, filter: Bson): IO[Throwable, TResult] =
    SingleItemSubscription(wrapped.findOneAndDelete(clientSession, filter)).fetch

  /**
    * Atomically find a document and remove it.
    *
    * @param clientSession
    *   the client session with which to associate this operation
    * @param filter
    *   the query filter to find the document with
    * @param options
    *   the options to apply to the operation
    * @return
    *   an IO with a single element the document that was removed. If no documents matched the query filter, then
    *   null will be returned
    * @since 2.2
    * @note
    *   Requires MongoDB 3.6 or greater
    */
  def findOneAndDelete(
                        clientSession: ClientSession,
                        filter: Bson,
                        options: FindOneAndDeleteOptions
                      ): IO[Throwable, TResult] =
    SingleItemSubscription(wrapped.findOneAndDelete(clientSession, filter, options)).fetch

  /**
    * Atomically find a document and replace it.
    *
    * @param filter
    *   the query filter to apply the the replace operation
    * @param replacement
    *   the replacement document
    * @return
    *   an IO with a single element the document that was replaced. Depending on the value of the
    *   `returnOriginal` property, this will either be the document as it was before the update or as it is after the
    *   update. If no documents matched the query filter, then null will be returned
    */
  def findOneAndReplace(filter: Bson, replacement: TResult): IO[Throwable, TResult] =
    SingleItemSubscription(wrapped.findOneAndReplace(filter, replacement)).fetch

  /**
    * Atomically find a document and replace it.
    *
    * @param filter
    *   the query filter to apply the the replace operation
    * @param replacement
    *   the replacement document
    * @param options
    *   the options to apply to the operation
    * @return
    *   an IO with a single element the document that was replaced. Depending on the value of the
    *   `returnOriginal` property, this will either be the document as it was before the update or as it is after the
    *   update. If no documents matched the query filter, then null will be returned
    */
  def findOneAndReplace(filter: Bson, replacement: TResult, options: FindOneAndReplaceOptions): IO[Throwable, TResult] =
    SingleItemSubscription(wrapped.findOneAndReplace(filter, replacement, options)).fetch

  /**
    * Atomically find a document and replace it.
    *
    * @param clientSession
    *   the client session with which to associate this operation
    * @param filter
    *   the query filter to apply the the replace operation
    * @param replacement
    *   the replacement document
    * @return
    *   an IO with a single element the document that was replaced. Depending on the value of the
    *   `returnOriginal` property, this will either be the document as it was before the update or as it is after the
    *   update. If no documents matched the query filter, then null will be returned
    * @since 2.2
    * @note
    *   Requires MongoDB 3.6 or greater
    */
  def findOneAndReplace(clientSession: ClientSession, filter: Bson, replacement: TResult): IO[Throwable, TResult] =
    SingleItemSubscription(wrapped.findOneAndReplace(clientSession, filter, replacement)).fetch

  /**
    * Atomically find a document and replace it.
    *
    * @param clientSession
    *   the client session with which to associate this operation
    * @param filter
    *   the query filter to apply the the replace operation
    * @param replacement
    *   the replacement document
    * @param options
    *   the options to apply to the operation
    * @return
    *   an IO with a single element the document that was replaced. Depending on the value of the
    *   `returnOriginal` property, this will either be the document as it was before the update or as it is after the
    *   update. If no documents matched the query filter, then null will be returned
    * @since 2.2
    * @note
    *   Requires MongoDB 3.6 or greater
    */
  def findOneAndReplace(
                         clientSession: ClientSession,
                         filter: Bson,
                         replacement: TResult,
                         options: FindOneAndReplaceOptions
                       ): IO[Throwable, TResult] =
    SingleItemSubscription(wrapped.findOneAndReplace(clientSession, filter, replacement, options)).fetch

  /**
    * Atomically find a document and update it.
    *
    * @param filter
    *   a document describing the query filter, which may not be null. This can be of any type for which a `Codec` is
    *   registered
    * @param update
    *   a document describing the update, which may not be null. The update to apply must include only update operators.
    *   This can be of any type for which a `Codec` is registered
    * @return
    *   an IO with a single element the document that was updated. Depending on the value of the `returnOriginal`
    *   property, this will either be the document as it was before the update or as it is after the update. If no
    *   documents matched the query filter, then null will be returned
    */
  def findOneAndUpdate(filter: Bson, update: Bson): IO[Throwable, TResult] =
    SingleItemSubscription(wrapped.findOneAndUpdate(filter, update)).fetch

  /**
    * Atomically find a document and update it.
    *
    * @param filter
    *   a document describing the query filter, which may not be null. This can be of any type for which a `Codec` is
    *   registered
    * @param update
    *   a document describing the update, which may not be null. The update to apply must include only update operators.
    *   This can be of any type for which a `Codec` is registered
    * @param options
    *   the options to apply to the operation
    * @return
    *   an IO with a single element the document that was updated. Depending on the value of the `returnOriginal`
    *   property, this will either be the document as it was before the update or as it is after the update. If no
    *   documents matched the query filter, then null will be returned
    */
  def findOneAndUpdate(filter: Bson, update: Bson, options: FindOneAndUpdateOptions): IO[Throwable, TResult] =
    SingleItemSubscription(wrapped.findOneAndUpdate(filter, update, options)).fetch

  /**
    * Atomically find a document and update it.
    *
    * @param clientSession
    *   the client session with which to associate this operation
    * @param filter
    *   a document describing the query filter, which may not be null. This can be of any type for which a `Codec` is
    *   registered
    * @param update
    *   a document describing the update, which may not be null. The update to apply must include only update operators.
    *   This can be of any type for which a `Codec` is registered
    * @return
    *   an IO with a single element the document that was updated. Depending on the value of the `returnOriginal`
    *   property, this will either be the document as it was before the update or as it is after the update. If no
    *   documents matched the query filter, then null will be returned
    * @since 2.2
    * @note
    *   Requires MongoDB 3.6 or greater
    */
  def findOneAndUpdate(clientSession: ClientSession, filter: Bson, update: Bson): IO[Throwable, TResult] =
    SingleItemSubscription(wrapped.findOneAndUpdate(clientSession, filter, update)).fetch

  /**
    * Atomically find a document and update it.
    *
    * @param clientSession
    *   the client session with which to associate this operation
    * @param filter
    *   a document describing the query filter, which may not be null. This can be of any type for which a `Codec` is
    *   registered
    * @param update
    *   a document describing the update, which may not be null. The update to apply must include only update operators.
    *   This can be of any type for which a `Codec` is registered
    * @param options
    *   the options to apply to the operation
    * @return
    *   an IO with a single element the document that was updated. Depending on the value of the `returnOriginal`
    *   property, this will either be the document as it was before the update or as it is after the update. If no
    *   documents matched the query filter, then null will be returned
    * @since 2.2
    * @note
    *   Requires MongoDB 3.6 or greater
    */
  def findOneAndUpdate(
                        clientSession: ClientSession,
                        filter: Bson,
                        update: Bson,
                        options: FindOneAndUpdateOptions
                      ): IO[Throwable, TResult] =
    SingleItemSubscription(wrapped.findOneAndUpdate(clientSession, filter, update, options)).fetch

  /**
    * Atomically find a document and update it.
    *
    * @param filter
    *   a document describing the query filter, which may not be null. This can be of any type for which a `Codec` is
    *   registered
    * @param update
    *   a pipeline describing the update.
    * @return
    *   an IO with a single element the document that was updated. Depending on the value of the `returnOriginal`
    *   property, this will either be the document as it was before the update or as it is after the update. If no
    *   documents matched the query filter, then null will be returned
    * @since 2.7
    * @note
    *   Requires MongoDB 4.2 or greater
    */
  def findOneAndUpdate(filter: Bson, update: Seq[Bson]): IO[Throwable, TResult] =
    SingleItemSubscription(wrapped.findOneAndUpdate(filter, update.asJava)).fetch

  /**
    * Atomically find a document and update it.
    *
    * @param filter
    *   a document describing the query filter, which may not be null. This can be of any type for which a `Codec` is
    *   registered
    * @param update
    *   a pipeline describing the update.
    * @param options
    *   the options to apply to the operation
    * @return
    *   an IO with a single element the document that was updated. Depending on the value of the `returnOriginal`
    *   property, this will either be the document as it was before the update or as it is after the update. If no
    *   documents matched the query filter, then null will be returned
    * @since 2.7
    * @note
    *   Requires MongoDB 4.2 or greater
    */
  def findOneAndUpdate(filter: Bson, update: Seq[Bson], options: FindOneAndUpdateOptions): IO[Throwable, TResult] =
    SingleItemSubscription(wrapped.findOneAndUpdate(filter, update.asJava, options)).fetch

  /**
    * Atomically find a document and update it.
    *
    * @param clientSession
    *   the client session with which to associate this operation
    * @param filter
    *   a document describing the query filter, which may not be null. This can be of any type for which a `Codec` is
    *   registered
    * @param update
    *   a pipeline describing the update.
    * @return
    *   an IO with a single element the document that was updated. Depending on the value of the `returnOriginal`
    *   property, this will either be the document as it was before the update or as it is after the update. If no
    *   documents matched the query filter, then null will be returned
    * @since 2.7
    * @note
    *   Requires MongoDB 4.2 or greater
    */
  def findOneAndUpdate(clientSession: ClientSession, filter: Bson, update: Seq[Bson]): IO[Throwable, TResult] =
    SingleItemSubscription(wrapped.findOneAndUpdate(clientSession, filter, update.asJava)).fetch

  /**
    * Atomically find a document and update it.
    *
    * @param clientSession
    *   the client session with which to associate this operation
    * @param filter
    *   a document describing the query filter, which may not be null. This can be of any type for which a `Codec` is
    *   registered
    * @param update
    *   a pipeline describing the update.
    * @param options
    *   the options to apply to the operation
    * @return
    *   an IO with a single element the document that was updated. Depending on the value of the `returnOriginal`
    *   property, this will either be the document as it was before the update or as it is after the update. If no
    *   documents matched the query filter, then null will be returned
    * @since 2.7
    * @note
    *   Requires MongoDB 4.2 or greater
    */
  def findOneAndUpdate(
                        clientSession: ClientSession,
                        filter: Bson,
                        update: Seq[Bson],
                        options: FindOneAndUpdateOptions
                      ): IO[Throwable, TResult] =
    SingleItemSubscription(wrapped.findOneAndUpdate(clientSession, filter, update.asJava, options)).fetch
  /**
    * Drops this collection from the Database.
    *
    * @return
    *   an IO with a single element indicating when the operation has completed
    *   [[http://docs.mongodb.org/manual/reference/command/drop/ Drop Collection]]
    */
  def drop(): IO[Throwable, Completed] = CompletedSubscription(wrapped.drop()).fetch

  /**
    * Drops this collection from the Database.
    *
    * @param clientSession
    *   the client session with which to associate this operation
    * @return
    *   an IO with a single element indicating when the operation has completed
    *   [[http://docs.mongodb.org/manual/reference/command/drop/ Drop Collection]]
    * @since 2.2
    * @note
    *   Requires MongoDB 3.6 or greater
    */
  def drop(clientSession: ClientSession): IO[Throwable, Completed] = CompletedSubscription(wrapped.drop(clientSession)).fetch

  /**
    * [[http://docs.mongodb.org/manual/reference/command/createIndexes Create Index]]
    * @param key
    *   an object describing the index key(s), which may not be null. This can be of any type for which a `Codec` is
    *   registered
    * @return
    *   an IO with a single element indicating when the operation has completed
    */
  def createIndex(key: Bson): IO[Throwable, String] =
    SingleItemSubscription(wrapped.createIndex(key)).fetch

  /**
    * [[http://docs.mongodb.org/manual/reference/command/createIndexes Create Index]]
    * @param key
    *   an object describing the index key(s), which may not be null. This can be of any type for which a `Codec` is
    *   registered
    * @param options
    *   the options for the index
    * @return
    *   an IO with a single element indicating when the operation has completed
    */
  def createIndex(key: Bson, options: IndexOptions): IO[Throwable, String] =
    SingleItemSubscription(wrapped.createIndex(key, options)).fetch

  /**
    * [[http://docs.mongodb.org/manual/reference/command/createIndexes Create Index]]
    * @param clientSession
    *   the client session with which to associate this operation
    * @param key
    *   an object describing the index key(s), which may not be null. This can be of any type for which a `Codec` is
    *   registered
    * @return
    *   an IO with a single element indicating when the operation has completed
    * @since 2.2
    * @note
    *   Requires MongoDB 3.6 or greater
    */
  def createIndex(clientSession: ClientSession, key: Bson): IO[Throwable, String] =
    SingleItemSubscription(wrapped.createIndex(clientSession, key)).fetch

  /**
    * [[http://docs.mongodb.org/manual/reference/command/createIndexes Create Index]]
    * @param clientSession
    *   the client session with which to associate this operation
    * @param key
    *   an object describing the index key(s), which may not be null. This can be of any type for which a `Codec` is
    *   registered
    * @param options
    *   the options for the index
    * @return
    *   an IO with a single element indicating when the operation has completed
    * @since 2.2
    * @note
    *   Requires MongoDB 3.6 or greater
    */
  def createIndex(clientSession: ClientSession, key: Bson, options: IndexOptions): IO[Throwable, String] =
    SingleItemSubscription(wrapped.createIndex(clientSession, key, options)).fetch

  /**
    * Create multiple indexes.
    *
    * [[http://docs.mongodb.org/manual/reference/command/createIndexes Create Index]]
    * @param models
    *   the list of indexes to create
    * @return
    *   an IO with the names of the indexes
    */
  def createIndexes(models: Seq[IndexModel]): IO[Throwable, String] =
    SingleItemSubscription(wrapped.createIndexes(models.asJava)).fetch

  /**
    * Create multiple indexes.
    *
    * [[http://docs.mongodb.org/manual/reference/command/createIndexes Create Index]]
    * @param models
    *   the list of indexes to create
    * @param createIndexOptions
    *   options to use when creating indexes
    * @return
    *   an IO with the names of the indexes
    * @since 2.2
    */
  def createIndexes(models: Seq[IndexModel], createIndexOptions: CreateIndexOptions): IO[Throwable, String] =
    SingleItemSubscription(wrapped.createIndexes(models.asJava, createIndexOptions)).fetch

  /**
    * Create multiple indexes.
    *
    * [[http://docs.mongodb.org/manual/reference/command/createIndexes Create Index]]
    * @param clientSession
    *   the client session with which to associate this operation
    * @param models
    *   the list of indexes to create
    * @return
    *   an IO with the names of the indexes
    * @since 2.2
    * @note
    *   Requires MongoDB 3.6 or greater
    */
  def createIndexes(clientSession: ClientSession, models: Seq[IndexModel]): IO[Throwable, String] =
    SingleItemSubscription(wrapped.createIndexes(clientSession, models.asJava)).fetch

  /**
    * Create multiple indexes.
    *
    * [[http://docs.mongodb.org/manual/reference/command/createIndexes Create Index]]
    * @param clientSession
    *   the client session with which to associate this operation
    * @param models
    *   the list of indexes to create
    * @param createIndexOptions
    *   options to use when creating indexes
    * @return
    *   an IO with the names of the indexes
    * @since 2.2
    * @note
    *   Requires MongoDB 3.6 or greater
    */
  def createIndexes(clientSession: ClientSession, models: Seq[IndexModel], createIndexOptions: CreateIndexOptions): IO[Throwable, String] =
    SingleItemSubscription(wrapped.createIndexes(clientSession, models.asJava, createIndexOptions)).fetch

  /**
    * Get all the indexes in this collection.
    *
    * [[http://docs.mongodb.org/manual/reference/command/listIndexes/ listIndexes]]
    * @tparam C
    *   the target document type of the observable.
    * @return
    *   the fluent list indexes interface
    */
  def listIndexes[C]()(implicit e: C DefaultsTo Document, ct: ClassTag[C]): ListIndexesSubscription[C] =
    ListIndexesSubscription(wrapped.listIndexes(ct.runtimeClass.asInstanceOf[Class[C]]))

  /**
    * Get all the indexes in this collection.
    *
    * [[http://docs.mongodb.org/manual/reference/command/listIndexes/ listIndexes]]
    * @param clientSession
    *   the client session with which to associate this operation
    * @tparam C
    *   the target document type of the observable.
    * @return
    *   the fluent list indexes interface
    * @since 2.2
    * @note
    *   Requires MongoDB 3.6 or greater
    */
  def listIndexes[C](clientSession: ClientSession)(implicit e: C DefaultsTo Document, ct: ClassTag[C]): ListIndexesSubscription[C] =
    ListIndexesSubscription(wrapped.listIndexes(clientSession, ct.runtimeClass.asInstanceOf[Class[C]]))

  /**
    * Drops the given index.
    *
    * [[http://docs.mongodb.org/manual/reference/command/dropIndexes/ Drop Indexes]]
    * @param indexName
    *   the name of the index to remove
    * @return
    *   an IO with a single element indicating when the operation has completed
    */
  def dropIndex(indexName: String): IO[Throwable, Completed] =
    CompletedSubscription(wrapped.dropIndex(indexName)).fetch

  /**
    * Drops the given index.
    *
    * [[http://docs.mongodb.org/manual/reference/command/dropIndexes/ Drop Indexes]]
    * @param indexName
    *   the name of the index to remove
    * @param dropIndexOptions
    *   options to use when dropping indexes
    * @return
    *   an IO with a single element indicating when the operation has completed
    * @since 2.2
    */
  def dropIndex(indexName: String, dropIndexOptions: DropIndexOptions): IO[Throwable, Completed] =
    CompletedSubscription(wrapped.dropIndex(indexName, dropIndexOptions)).fetch

  /**
    * Drops the index given the keys used to create it.
    *
    * @param keys
    *   the keys of the index to remove
    * @return
    *   an IO with a single element indicating when the operation has completed
    */
  def dropIndex(keys: Bson): IO[Throwable, Completed] =
    CompletedSubscription(wrapped.dropIndex(keys)).fetch

  /**
    * Drops the index given the keys used to create it.
    *
    * @param keys
    *   the keys of the index to remove
    * @param dropIndexOptions
    *   options to use when dropping indexes
    * @return
    *   an IO with a single element indicating when the operation has completed
    * @since 2.2
    */
  def dropIndex(keys: Bson, dropIndexOptions: DropIndexOptions): IO[Throwable, Completed] =
    CompletedSubscription(wrapped.dropIndex(keys, dropIndexOptions)).fetch

  /**
    * Drops the given index.
    *
    * [[http://docs.mongodb.org/manual/reference/command/dropIndexes/ Drop Indexes]]
    * @param clientSession
    *   the client session with which to associate this operation
    * @param indexName
    *   the name of the index to remove
    * @return
    *   an IO with a single element indicating when the operation has completed
    * @since 2.2
    * @note
    *   Requires MongoDB 3.6 or greater
    */
  def dropIndex(clientSession: ClientSession, indexName: String) =
    SingleItemSubscription(wrapped.dropIndex(clientSession, indexName)).fetch

  /**
    * Drops the given index.
    *
    * [[http://docs.mongodb.org/manual/reference/command/dropIndexes/ Drop Indexes]]
    * @param clientSession
    *   the client session with which to associate this operation
    * @param indexName
    *   the name of the index to remove
    * @param dropIndexOptions
    *   options to use when dropping indexes
    * @return
    *   an IO with a single element indicating when the operation has completed
    * @since 2.2
    * @note
    *   Requires MongoDB 3.6 or greater
    */
  def dropIndex(clientSession: ClientSession, indexName: String, dropIndexOptions: DropIndexOptions): IO[Throwable, Completed] =
    CompletedSubscription(wrapped.dropIndex(clientSession, indexName, dropIndexOptions)).fetch

  /**
    * Drops the index given the keys used to create it.
    *
    * @param clientSession
    *   the client session with which to associate this operation
    * @param keys
    *   the keys of the index to remove
    * @return
    *   an IO with a single element indicating when the operation has completed
    * @since 2.2
    * @note
    *   Requires MongoDB 3.6 or greater
    */
  def dropIndex(clientSession: ClientSession, keys: Bson): IO[Throwable, Completed] =
    CompletedSubscription(wrapped.dropIndex(clientSession, keys)).fetch

  /**
    * Drops the index given the keys used to create it.
    *
    * @param clientSession
    *   the client session with which to associate this operation
    * @param keys
    *   the keys of the index to remove
    * @param dropIndexOptions
    *   options to use when dropping indexes
    * @return
    *   an IO with a single element indicating when the operation has completed
    * @since 2.2
    * @note
    *   Requires MongoDB 3.6 or greater
    */
  def dropIndex(
                 clientSession: ClientSession,
                 keys: Bson,
                 dropIndexOptions: DropIndexOptions
               ): IO[Throwable, Completed] =
    CompletedSubscription(wrapped.dropIndex(clientSession, keys, dropIndexOptions)).fetch

  /**
    * Drop all the indexes on this collection, except for the default on _id.
    *
    * [[http://docs.mongodb.org/manual/reference/command/dropIndexes/ Drop Indexes]]
    * @return
    *   an IO with a single element indicating when the operation has completed
    */
  def dropIndexes(): IO[Throwable, Completed] =
    CompletedSubscription(wrapped.dropIndexes()).fetch

  /**
    * Drop all the indexes on this collection, except for the default on _id.
    *
    * [[http://docs.mongodb.org/manual/reference/command/dropIndexes/ Drop Indexes]]
    * @param dropIndexOptions
    *   options to use when dropping indexes
    * @return
    *   an IO with a single element indicating when the operation has completed
    * @since 2.2
    */
  def dropIndexes(dropIndexOptions: DropIndexOptions): IO[Throwable, Completed] =
    CompletedSubscription(wrapped.dropIndexes(dropIndexOptions)).fetch

  /**
    * Drop all the indexes on this collection, except for the default on _id.
    *
    * [[http://docs.mongodb.org/manual/reference/command/dropIndexes/ Drop Indexes]]
    * @param clientSession
    *   the client session with which to associate this operation
    * @return
    *   an IO with a single element indicating when the operation has completed
    * @since 2.2
    * @note
    *   Requires MongoDB 3.6 or greater
    */
  def dropIndexes(clientSession: ClientSession): IO[Throwable, Completed] =
    CompletedSubscription(wrapped.dropIndexes(clientSession)).fetch

  /**
    * Drop all the indexes on this collection, except for the default on _id.
    *
    * [[http://docs.mongodb.org/manual/reference/command/dropIndexes/ Drop Indexes]]
    * @param clientSession
    *   the client session with which to associate this operation
    * @param dropIndexOptions
    *   options to use when dropping indexes
    * @return
    *   an IO with a single element indicating when the operation has completed
    * @since 2.2
    * @note
    *   Requires MongoDB 3.6 or greater
    */
  def dropIndexes(clientSession: ClientSession, dropIndexOptions: DropIndexOptions): IO[Throwable, Completed] =
    CompletedSubscription(wrapped.dropIndexes(clientSession, dropIndexOptions)).fetch

  /**
    * Rename the collection with oldCollectionName to the newCollectionName.
    *
    * [[http://docs.mongodb.org/manual/reference/commands/renameCollection Rename collection]]
    * @param newCollectionNamespace
    *   the name the collection will be renamed to
    * @return
    *   an IO with a single element indicating when the operation has completed
    */
  def renameCollection(newCollectionNamespace: MongoNamespace): IO[Throwable, Completed] =
    CompletedSubscription(wrapped.renameCollection(newCollectionNamespace)).fetch

  /**
    * Rename the collection with oldCollectionName to the newCollectionName.
    *
    * [[http://docs.mongodb.org/manual/reference/commands/renameCollection Rename collection]]
    * @param newCollectionNamespace
    *   the name the collection will be renamed to
    * @param options
    *   the options for renaming a collection
    * @return
    *   an IO with a single element indicating when the operation has completed
    */
  def renameCollection(
                        newCollectionNamespace: MongoNamespace,
                        options: RenameCollectionOptions
                      ): IO[Throwable, Completed] =
    CompletedSubscription(wrapped.renameCollection(newCollectionNamespace, options)).fetch

  /**
    * Rename the collection with oldCollectionName to the newCollectionName.
    *
    * [[http://docs.mongodb.org/manual/reference/commands/renameCollection Rename collection]]
    * @param clientSession
    *   the client session with which to associate this operation
    * @param newCollectionNamespace
    *   the name the collection will be renamed to
    * @return
    *   an IO with a single element indicating when the operation has completed
    * @since 2.2
    * @note
    *   Requires MongoDB 3.6 or greater
    */
  def renameCollection(clientSession: ClientSession, newCollectionNamespace: MongoNamespace): IO[Throwable, Completed] =
    CompletedSubscription(wrapped.renameCollection(clientSession, newCollectionNamespace)).fetch

  /**
    * Rename the collection with oldCollectionName to the newCollectionName.
    *
    * [[http://docs.mongodb.org/manual/reference/commands/renameCollection Rename collection]]
    * @param clientSession
    *   the client session with which to associate this operation
    * @param newCollectionNamespace
    *   the name the collection will be renamed to
    * @param options
    *   the options for renaming a collection
    * @return
    *   an IO with a single element indicating when the operation has completed
    * @since 2.2
    * @note
    *   Requires MongoDB 3.6 or greater
    */
  def renameCollection(
                        clientSession: ClientSession,
                        newCollectionNamespace: MongoNamespace,
                        options: RenameCollectionOptions
                      ): IO[Throwable, Completed] =
    CompletedSubscription(wrapped.renameCollection(clientSession, newCollectionNamespace, options)).fetch

  /**
    * Creates a change stream for this collection.
    *
    * @tparam C
    *   the target document type of the observable.
    * @return
    *   the change stream observable
    * @since 2.2
    * @note
    *   Requires MongoDB 3.6 or greater
    */
  def watch(): ChangeStreamSubscription[bson.Document] =
    ChangeStreamSubscription(wrapped.watch())

  /**
    * Creates a change stream for this collection.
    *
    * @param pipeline
    *   the aggregation pipeline to apply to the change stream
    * @tparam C
    *   the target document type of the observable.
    * @return
    *   the change stream observable
    * @since 2.2
    * @note
    *   Requires MongoDB 3.6 or greater
    */
  def watch(pipeline: Seq[Bson]): ChangeStreamSubscription[bson.Document] =
    ChangeStreamSubscription(wrapped.watch(pipeline.asJava))

  /**
    * Creates a change stream for this collection.
    *
    * @param clientSession
    *   the client session with which to associate this operation
    * @tparam C
    *   the target document type of the observable.
    * @return
    *   the change stream observable
    * @since 2.2
    * @note
    *   Requires MongoDB 3.6 or greater
    */
  def watch(clientSession: ClientSession): ChangeStreamSubscription[bson.Document] =
    ChangeStreamSubscription(wrapped.watch(clientSession))

  /**
    * Creates a change stream for this collection.
    *
    * @param clientSession
    *   the client session with which to associate this operation
    * @param pipeline
    *   the aggregation pipeline to apply to the change stream
    * @tparam C
    *   the target document type of the observable.
    * @return
    *   the change stream observable
    * @since 2.2
    * @note
    *   Requires MongoDB 3.6 or greater
    */
  def watch(clientSession: ClientSession, pipeline: Seq[Bson]): ChangeStreamSubscription[bson.Document] =
    ChangeStreamSubscription(wrapped.watch(clientSession, pipeline.asJava))

}
