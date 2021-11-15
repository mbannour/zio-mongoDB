package io.github.mbannour

import com.mongodb.bulk.BulkWriteResult
import com.mongodb.client.model._
import com.mongodb.{MongoNamespace, ReadConcern, ReadPreference, WriteConcern, client}
import com.mongodb.reactivestreams.client.ClientSession
import io.github.mbannour.DefaultHelper.MapTo
import io.github.mbannour.result.{Completed, DeleteResult, InsertManyResult, InsertOneResult, UpdateResult}
import io.github.mbannour.subscriptions._
import org.bson

import scala.jdk.CollectionConverters._
import scala.reflect.ClassTag
import org.bson.codecs.configuration.CodecRegistry
import org.bson.conversions.Bson
import org.mongodb.scala.bson.collection.immutable.Document
import zio.{IO, ZIO}


case class MongoZioCollection[T](private val wrapped: JavaMongoCollection[T]) {

  /**
    * Gets the namespace of this collection.
    */
  lazy val namespace: MongoNamespace = wrapped.getNamespace

  /**
    * Get the default class to cast any documents returned from the database into.
    */
  lazy val documentClass: Class[T] = wrapped.getDocumentClass

  /**
    * Get the codec registry for the MongoDatabase.
    */
  lazy val codecRegistry: CodecRegistry = wrapped.getCodecRegistry

  /**
    * Get the read preference for the MongoDatabase.
    */
  lazy val readPreference: ReadPreference = wrapped.getReadPreference

  /**
    * Get the write concern for the MongoDatabase.
    */
  lazy val writeConcern: WriteConcern = wrapped.getWriteConcern

  /**
    * Get the read concern for the MongoDatabase.
    */
  lazy val readConcern: ReadConcern = wrapped.getReadConcern

  /**
    * Create a new MongoZioCollection instance with a different default class to cast any documents returned from the
    * database into..
    */
  def withDocumentClass[C]()(implicit e: C MapTo Document, ct: ClassTag[C]): MongoZioCollection[C] =
    MongoZioCollection(wrapped.withDocumentClass(clazz(ct)))

  /**
    * Create a new MongoZioCollection instance with a different codec registry.
    */
  def withCodecRegistry(codecRegistry: CodecRegistry): MongoZioCollection[T] =
    MongoZioCollection(wrapped.withCodecRegistry(codecRegistry))

  /**
    * Create a new MongoZioCollection instance with a different read preference.
    */
  def withReadPreference(readPreference: ReadPreference): MongoZioCollection[T] =
    MongoZioCollection(wrapped.withReadPreference(readPreference))

  /**
    * Create a new MongoZioCollection instance with a different write concern.
    */
  def withWriteConcern(writeConcern: WriteConcern): MongoZioCollection[T] =
    MongoZioCollection(wrapped.withWriteConcern(writeConcern))

  /**
    * Create a new MongoZioCollection instance with a different read concern.
    */
  def withReadConcern(readConcern: ReadConcern): MongoZioCollection[T] =
    MongoZioCollection(wrapped.withReadConcern(readConcern))

  /**
    * Gets an estimate of the count of documents in a collection using collection metadata.
    */
  def estimatedDocumentCount(): IO[Throwable, Long] =
    SingleItemSubscription(wrapped.estimatedDocumentCount()).fetch.map(long2Long(_))

  /**
    * Gets an estimate of the count of documents in a collection using collection metadata.
    */
  def estimatedDocumentCount(options: EstimatedDocumentCountOptions): IO[Throwable, Long] =
    SingleItemSubscription(wrapped.estimatedDocumentCount(options)).fetch.map(long2Long(_))

  /**
    * Counts the number of documents in the collection.
    */
  def countDocuments(): ZIO[Any, Throwable, Long] =
    SingleItemSubscription(wrapped.countDocuments()).fetch.map(long2Long(_))

  /**
    * Counts the number of documents in the collection according to the given options.
    */
  def countDocuments(filter: Bson): ZIO[Any, Throwable, Long] =
    SingleItemSubscription(wrapped.countDocuments(filter)).fetch.map(long2Long(_))

  /**
    * Counts the number of documents in the collection according to the given options.
    */
  def countDocuments(filter: Bson, options: CountOptions): IO[Throwable, Long] =
    SingleItemSubscription(wrapped.countDocuments(filter, options)).fetch.map(long2Long(_))

  /**
    * Counts the number of documents in the collection.
    *
    */
  def countDocuments(clientSession: ClientSession): ZIO[Any, Throwable, Long] =
    SingleItemSubscription(wrapped.countDocuments(clientSession)).fetch.map(long2Long(_))

  /**
    * Counts the number of documents in the collection according to the given options.
    */
  def countDocuments(clientSession: ClientSession, filter: Bson): ZIO[Any, Throwable, Long] =
    SingleItemSubscription(wrapped.countDocuments(clientSession, filter)).fetch.map(long2Long(_))

  /**
    * Counts the number of documents in the collection according to the given options.
    */
  def countDocuments(clientSession: ClientSession, filter: Bson, options: CountOptions): ZIO[Any, Throwable, Long] =
    SingleItemSubscription(wrapped.countDocuments(clientSession, filter, options)).fetch.map(long2Long(_))

  /**
    * Gets the distinct values of the specified field name.
    */
  def distinct[C](fieldName: String)(implicit e: C MapTo T, ct: ClassTag[C]): DistinctSubscription[C] =
    DistinctSubscription(wrapped.distinct(fieldName, clazz(ct)))

  /**
    * Gets the distinct values of the specified field name.
    */
  def distinct[C](fieldName: String, filter: Bson)(implicit e: C MapTo T, ct: ClassTag[C]): DistinctSubscription[C] =
    DistinctSubscription(wrapped.distinct(fieldName, filter, clazz(ct)))

  /**
    * Gets the distinct values of the specified field name.
    */
  def distinct[C](clientSession: ClientSession, fieldName: String)(implicit e: C MapTo T, ct: ClassTag[C]): DistinctSubscription[C] =
    DistinctSubscription(wrapped.distinct(clientSession, fieldName, clazz(ct)))

  /**
    * Gets the distinct values of the specified field name.
    */
  def distinct[C](clientSession: ClientSession, fieldName: String, filter: Bson)(implicit e: C MapTo T, ct: ClassTag[C]): DistinctSubscription[C] =
    DistinctSubscription(wrapped.distinct(clientSession, fieldName, filter, clazz(ct)))

  /**
    * Finds all documents in the collection.
    */
  def find[C]()(implicit e: C MapTo T, ct: ClassTag[C]): FindSubscription[C] =
    FindSubscription(wrapped.find(clazz(ct)))

  /**
    * Finds all documents in the collection.
    */
  def find[C](filter: Bson)(implicit e: C MapTo T, ct: ClassTag[C]): FindSubscription[C] =
    FindSubscription(wrapped.find(filter, clazz(ct)))

  /**
    * Finds all documents in the collection.
    */
  def find[C](clientSession: ClientSession)(implicit e: C MapTo Document, ct: ClassTag[C]): FindSubscription[C] =
    FindSubscription(wrapped.find[C](clientSession, clazz(ct)))

  /**
    * Finds all documents in the collection.
    */
  def find[C](clientSession: ClientSession, filter: Bson)(implicit e: C MapTo Document, ct: ClassTag[C]): FindSubscription[C] =
    FindSubscription(wrapped.find(clientSession, filter, clazz(ct)))

  /**
    * Aggregates documents according to the specified aggregation pipeline.
    */
  def aggregate[C](pipeline: Seq[Bson])(implicit e: C MapTo Document, ct: ClassTag[C]): AggregateSubscription[C] =
    AggregateSubscription(wrapped.aggregate(pipeline.asJava, clazz(ct)))

  /**
    * Aggregates documents according to the specified aggregation pipeline.
    */
  def aggregate[C](clientSession: ClientSession, pipeline: Seq[Bson])(implicit e: C MapTo Document, ct: ClassTag[C]): AggregateSubscription[C] =
    AggregateSubscription(wrapped.aggregate(clientSession, pipeline.asJava, clazz(ct)))

  /**
    * Aggregates documents according to the specified map-reduce function.
    */
  def mapReduce[C](mapFunction: String, reduceFunction: String)(implicit e: C MapTo Document, ct: ClassTag[C]): MapReduceSubscription[C] =
    MapReduceSubscription(wrapped.mapReduce(mapFunction, reduceFunction, clazz(ct)))

  /**
    * Aggregates documents according to the specified map-reduce function.
    *
    */
  def mapReduce[C](clientSession: ClientSession, mapFunction: String, reduceFunction: String)(implicit e: C MapTo Document, ct: ClassTag[C]): MapReduceSubscription[C] =
    MapReduceSubscription(wrapped.mapReduce(clientSession, mapFunction, reduceFunction, clazz(ct)))

  /**
    * Executes a mix of inserts, updates, replaces, and deletes.
    *
    */
  def bulkWrite(requests: Seq[_ <: WriteModel[_ <: T]]): IO[Throwable, BulkWriteResult] =
    SingleItemSubscription(wrapped.bulkWrite(requests.asJava)).fetch

  /**
    * Executes a mix of inserts, updates, replaces, and deletes.
    *
    */
  def bulkWrite(requests: Seq[_ <: WriteModel[_ <: T]], options: BulkWriteOptions): IO[Throwable, BulkWriteResult] =
    SingleItemSubscription(wrapped.bulkWrite(requests.asJava, options)).fetch

  /**
    * Executes a mix of inserts, updates, replaces, and deletes.
    *
    */
  def bulkWrite(
                 clientSession: ClientSession,
                 requests: Seq[_ <: WriteModel[_ <: T]]
               ): IO[Throwable, BulkWriteResult] =
    SingleItemSubscription(wrapped.bulkWrite(clientSession, requests.asJava)).fetch

  /**
    * Executes a mix of inserts, updates, replaces, and deletes.
    *
    */
  def bulkWrite(
                 clientSession: ClientSession,
                 requests: Seq[_ <: WriteModel[_ <: T]],
                 options: BulkWriteOptions
               ): IO[Throwable, BulkWriteResult] =
    SingleItemSubscription(wrapped.bulkWrite(clientSession, requests.asJava, options)).fetch

  /**
    * Inserts the provided document. If the document is missing an identifier, the driver should generate one.
    *
    */
  def insertOne(document: T): IO[Throwable, InsertOneResult] =
    SingleItemSubscription(wrapped.insertOne(document)).fetch.map(InsertOneResult)

  /**
    * Inserts the provided document. If the document is missing an identifier, the driver should generate one.
    *
    */
  def insertOne(document: T, options: InsertOneOptions): IO[Throwable, InsertOneResult] =
    SingleItemSubscription(wrapped.insertOne(document, options)).fetch.map(InsertOneResult)

  /**
    * Inserts the provided document. If the document is missing an identifier, the driver should generate one.
    *
    */
  def insertOne(clientSession: ClientSession, document: T): IO[Throwable, InsertOneResult] =
    SingleItemSubscription(wrapped.insertOne(clientSession, document)).fetch.map(InsertOneResult)

  /**
    * Inserts the provided document. If the document is missing an identifier, the driver should generate one.
    *
    */
  def insertOne(clientSession: ClientSession, document: T, options: InsertOneOptions): IO[Throwable, InsertOneResult] =
    SingleItemSubscription(wrapped.insertOne(clientSession, document, options)).fetch.map(InsertOneResult)

  /**
    * Inserts a batch of documents. The preferred way to perform bulk inserts is to use the BulkWrite API. However, when
    * talking with a server &lt; 2.6, using this method will be faster due to constraints in the bulk API related to
    * error handling.
    */
  def insertMany(documents: Seq[_ <: T]): IO[Throwable, InsertManyResult] =
    SingleItemSubscription(wrapped.insertMany(documents.asJava)).fetch.map(InsertManyResult)

  /**
    * Inserts a batch of documents. The preferred way to perform bulk inserts is to use the BulkWrite API. However, when
    * talking with a server &lt; 2.6, using this method will be faster due to constraints in the bulk API related to
    * error handling.
    *
    */
  def insertMany(documents: Seq[_ <: T], options: InsertManyOptions): IO[Throwable, InsertManyResult] =
    SingleItemSubscription(wrapped.insertMany(documents.asJava, options)).fetch.map(InsertManyResult)

  /**
    * Inserts a batch of documents. The preferred way to perform bulk inserts is to use the BulkWrite API.
    *
    */
  def insertMany(clientSession: ClientSession, documents: Seq[_ <: T]): IO[Throwable, InsertManyResult] =
    SingleItemSubscription(wrapped.insertMany(clientSession, documents.asJava)).fetch.map(InsertManyResult)

  /**
    * Inserts a batch of documents. The preferred way to perform bulk inserts is to use the BulkWrite API.
    *
    */
  def insertMany(
                  clientSession: ClientSession,
                  documents: Seq[_ <: T],
                  options: InsertManyOptions
                ): IO[Throwable, InsertManyResult] =
    SingleItemSubscription(wrapped.insertMany(clientSession, documents.asJava, options)).fetch.map(InsertManyResult)

  /**
    * Removes at most one document from the collection that matches the given filter. If no documents match, the
    * collection is not modified.
    *
    */
  def deleteOne(filter: Bson): IO[Throwable, DeleteResult] =
    SingleItemSubscription(wrapped.deleteOne(filter)).fetch.map(DeleteResult)

  /**
    * Removes at most one document from the collection that matches the given filter. If no documents match, the
    * collection is not modified.
    *
    */
  def deleteOne(filter: Bson, options: DeleteOptions): IO[Throwable, DeleteResult] =
    SingleItemSubscription(wrapped.deleteOne(filter, options)).fetch.map(DeleteResult)

  /**
    * Removes at most one document from the collection that matches the given filter. If no documents match, the
    * collection is not modified.
    *
    */
  def deleteOne(clientSession: ClientSession, filter: Bson): IO[Throwable, DeleteResult] =
    SingleItemSubscription(wrapped.deleteOne(clientSession, filter)).fetch.map(DeleteResult)

  /**
    * Removes at most one document from the collection that matches the given filter. If no documents match, the
    * collection is not modified.
    *
    */
  def deleteOne(clientSession: ClientSession, filter: Bson, options: DeleteOptions): IO[Throwable, DeleteResult] =
    SingleItemSubscription(wrapped.deleteOne(clientSession, filter, options)).fetch.map(DeleteResult)

  /**
    * Removes all documents from the collection that match the given query filter. If no documents match, the collection
    * is not modified.
    */
  def deleteMany(filter: Bson): IO[Throwable, DeleteResult] =
    SingleItemSubscription(wrapped.deleteMany(filter)).fetch.map(DeleteResult)

  /**
    * Removes all documents from the collection that match the given query filter. If no documents match, the collection
    * is not modified.
    *
    */
  def deleteMany(filter: Bson, options: DeleteOptions): IO[Throwable, DeleteResult] =
    SingleItemSubscription(wrapped.deleteMany(filter, options)).fetch.map(DeleteResult)

  /**
    * Removes all documents from the collection that match the given query filter. If no documents match, the collection
    * is not modified.
    *
    */
  def deleteMany(clientSession: ClientSession, filter: Bson): IO[Throwable, DeleteResult] =
    SingleItemSubscription(wrapped.deleteMany(clientSession, filter)).fetch.map(DeleteResult)

  /**
    * Removes all documents from the collection that match the given query filter. If no documents match, the collection
    * is not modified.
    *
    */
  def deleteMany(clientSession: ClientSession, filter: Bson, options: DeleteOptions): IO[Throwable, DeleteResult] =
    SingleItemSubscription(wrapped.deleteMany(clientSession, filter, options)).fetch.map(DeleteResult)

  /**
    * Replace a document in the collection according to the specified arguments.
    *
    */
  def replaceOne(filter: Bson, replacement: T): IO[Throwable, client.result.UpdateResult] =
    SingleItemSubscription(wrapped.replaceOne(filter, replacement)).fetch

  /**
    * Replace a document in the collection according to the specified arguments.
    *
    */
  def replaceOne(clientSession: ClientSession, filter: Bson, replacement: T): IO[Throwable, UpdateResult] =
    SingleItemSubscription(wrapped.replaceOne(clientSession, filter, replacement)).fetch.map(UpdateResult)


  /**
    * Replace a document in the collection according to the specified arguments.
    *
    */
  def replaceOne(filter: Bson, replacement: T, options: ReplaceOptions): IO[Throwable, UpdateResult] =
    SingleItemSubscription(wrapped.replaceOne(filter, replacement, options)).fetch.map(UpdateResult)


  /**
    * Replace a document in the collection according to the specified arguments.
    *
    */
  def replaceOne(clientSession: ClientSession, filter: Bson, replacement: T, options: ReplaceOptions): IO[Throwable, UpdateResult] =
    SingleItemSubscription(wrapped.replaceOne(clientSession, filter, replacement, options)).fetch.map(UpdateResult)


  /**
    * Update a single document in the collection according to the specified arguments.
    *
    */
  def updateOne(filter: Bson, update: Bson): IO[Throwable, UpdateResult] =
    SingleItemSubscription(wrapped.updateOne(filter, update)).fetch.map(UpdateResult)


  /**
    * Update a single document in the collection according to the specified arguments.
    *
    */
  def updateOne(filter: Bson, update: Bson, options: UpdateOptions): IO[Throwable, UpdateResult] =
    SingleItemSubscription(wrapped.updateOne(filter, update, options)).fetch.map(UpdateResult)


  /**
    * Update a single document in the collection according to the specified arguments.
    *
    */
  def updateOne(clientSession: ClientSession, filter: Bson, update: Bson): IO[Throwable, UpdateResult] =
    SingleItemSubscription(wrapped.updateOne(clientSession, filter, update)).fetch.map(UpdateResult)


  /**
    * Update a single document in the collection according to the specified arguments.
    *
    */
  def updateOne(clientSession: ClientSession, filter: Bson, update: Bson, options: UpdateOptions): IO[Throwable, UpdateResult] =
    SingleItemSubscription(wrapped.updateOne(clientSession, filter, update, options)).fetch.map(UpdateResult)


  /**
    * Update a single document in the collection according to the specified arguments.
    *
    */
  def updateOne(filter: Bson, update: Seq[Bson]): IO[Throwable, UpdateResult] =
    SingleItemSubscription(wrapped.updateOne(filter, update.asJava)).fetch.map(UpdateResult)


  /**
    * Update a single document in the collection according to the specified arguments.
    *
    */
  def updateOne(filter: Bson, update: Seq[Bson], options: UpdateOptions): IO[Throwable, UpdateResult] =
    SingleItemSubscription(wrapped.updateOne(filter, update.asJava, options)).fetch.map(UpdateResult)


  /**
    * Update a single document in the collection according to the specified arguments.
    *
    */
  def updateOne(clientSession: ClientSession, filter: Bson, update: Seq[Bson]): IO[Throwable, UpdateResult] =
    SingleItemSubscription(wrapped.updateOne(clientSession, filter, update.asJava)).fetch.map(UpdateResult)


  /**
    * Update a single document in the collection according to the specified arguments.
    *
    */
  def updateOne(clientSession: ClientSession, filter: Bson, update: Seq[Bson], options: UpdateOptions): IO[Throwable, UpdateResult] =
    SingleItemSubscription(wrapped.updateOne(clientSession, filter, update.asJava, options)).fetch.map(UpdateResult)

  /**
    * Update a single document in the collection according to the specified arguments.
    *
    */
  def updateMany(filter: Bson, update: Bson): IO[Throwable, UpdateResult] =
    SingleItemSubscription(wrapped.updateMany(filter, update)).fetch.map(UpdateResult)

  /**
    * Update a single document in the collection according to the specified arguments.
    *
    */
  def updateMany(filter: Bson, update: Bson, options: UpdateOptions): IO[Throwable, UpdateResult] =
    SingleItemSubscription(wrapped.updateMany(filter, update, options)).fetch.map(UpdateResult)

  /**
    * Update a single document in the collection according to the specified arguments.
    *
    */
  def updateMany(clientSession: ClientSession, filter: Bson, update: Bson): IO[Throwable, UpdateResult] =
    SingleItemSubscription(wrapped.updateMany(clientSession, filter, update)).fetch.map(UpdateResult)

  /**
    * Update a single document in the collection according to the specified arguments.
    *
    */
  def updateMany(clientSession: ClientSession, filter: Bson, update: Bson, options: UpdateOptions): IO[Throwable, UpdateResult] =
    SingleItemSubscription(wrapped.updateMany(clientSession, filter, update, options)).fetch.map(UpdateResult)

  /**
    * Update a single document in the collection according to the specified arguments.
    *
    */
  def updateMany(filter: Bson, update: Seq[Bson]): IO[Throwable, UpdateResult] =
    SingleItemSubscription(wrapped.updateMany(filter, update.asJava)).fetch.map(UpdateResult)

  /**
    * Update a single document in the collection according to the specified arguments.
    *
    */
  def updateMany(filter: Bson, update: Seq[Bson], options: UpdateOptions): IO[Throwable, UpdateResult] =
    SingleItemSubscription(wrapped.updateMany(filter, update.asJava, options)).fetch.map(UpdateResult)

  /**
    * Update a single document in the collection according to the specified arguments.
    *
    */
  def updateMany(clientSession: ClientSession, filter: Bson, update: Seq[Bson]): IO[Throwable, UpdateResult] =
    SingleItemSubscription(wrapped.updateMany(clientSession, filter, update.asJava)).fetch.map(UpdateResult)

  /**
    * Update a single document in the collection according to the specified arguments.
    *
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
    */
  def findOneAndDelete(filter: Bson): IO[Throwable, T] =
    SingleItemSubscription(wrapped.findOneAndDelete(filter)).fetch

  /**
    * Atomically find a document and remove it.
    *
    */
  def findOneAndDelete(filter: Bson, options: FindOneAndDeleteOptions): IO[Throwable, T] =
    SingleItemSubscription(wrapped.findOneAndDelete(filter, options)).fetch

  /**
    * Atomically find a document and remove it.
    *
    */
  def findOneAndDelete(clientSession: ClientSession, filter: Bson): IO[Throwable, T] =
    SingleItemSubscription(wrapped.findOneAndDelete(clientSession, filter)).fetch

  /**
    * Atomically find a document and remove it.
    *
    */
  def findOneAndDelete(
                        clientSession: ClientSession,
                        filter: Bson,
                        options: FindOneAndDeleteOptions
                      ): IO[Throwable, T] =
    SingleItemSubscription(wrapped.findOneAndDelete(clientSession, filter, options)).fetch

  /**
    * Atomically find a document and replace it.
    *
    */
  def findOneAndReplace(filter: Bson, replacement: T): IO[Throwable, T] =
    SingleItemSubscription(wrapped.findOneAndReplace(filter, replacement)).fetch

  /**
    * Atomically find a document and replace it.
    *
    */
  def findOneAndReplace(filter: Bson, replacement: T, options: FindOneAndReplaceOptions): IO[Throwable, T] =
    SingleItemSubscription(wrapped.findOneAndReplace(filter, replacement, options)).fetch

  /**
    * Atomically find a document and replace it.
    *
    */
  def findOneAndReplace(clientSession: ClientSession, filter: Bson, replacement: T): IO[Throwable, T] =
    SingleItemSubscription(wrapped.findOneAndReplace(clientSession, filter, replacement)).fetch

  /**
    * Atomically find a document and replace it.
    *
    */
  def findOneAndReplace(
                         clientSession: ClientSession,
                         filter: Bson,
                         replacement: T,
                         options: FindOneAndReplaceOptions
                       ): IO[Throwable, T] =
    SingleItemSubscription(wrapped.findOneAndReplace(clientSession, filter, replacement, options)).fetch

  /**
    * Atomically find a document and update it.
    *
    */
  def findOneAndUpdate(filter: Bson, update: Bson): IO[Throwable, T] =
    SingleItemSubscription(wrapped.findOneAndUpdate(filter, update)).fetch

  /**
    * Atomically find a document and update it.
    *
    */
  def findOneAndUpdate(filter: Bson, update: Bson, options: FindOneAndUpdateOptions): IO[Throwable, T] =
    SingleItemSubscription(wrapped.findOneAndUpdate(filter, update, options)).fetch

  /**
    * Atomically find a document and update it.
    *
    */
  def findOneAndUpdate(clientSession: ClientSession, filter: Bson, update: Bson): IO[Throwable, T] =
    SingleItemSubscription(wrapped.findOneAndUpdate(clientSession, filter, update)).fetch

  /**
    * Atomically find a document and update it.
    *
    */
  def findOneAndUpdate(clientSession: ClientSession, filter: Bson, update: Bson, options: FindOneAndUpdateOptions): IO[Throwable, T] =
    SingleItemSubscription(wrapped.findOneAndUpdate(clientSession, filter, update, options)).fetch

  /**
    * Atomically find a document and update it.
    *
    */
  def findOneAndUpdate(filter: Bson, update: Seq[Bson]): IO[Throwable, T] =
    SingleItemSubscription(wrapped.findOneAndUpdate(filter, update.asJava)).fetch

  /**
    * Atomically find a document and update it.
    *
    */
  def findOneAndUpdate(filter: Bson, update: Seq[Bson], options: FindOneAndUpdateOptions): IO[Throwable, T] =
    SingleItemSubscription(wrapped.findOneAndUpdate(filter, update.asJava, options)).fetch

  /**
    * Atomically find a document and update it.
    *
    */
  def findOneAndUpdate(clientSession: ClientSession, filter: Bson, update: Seq[Bson]): IO[Throwable, T] =
    SingleItemSubscription(wrapped.findOneAndUpdate(clientSession, filter, update.asJava)).fetch

  /**
    * Atomically find a document and update it.
    *
    */
  def findOneAndUpdate(
                        clientSession: ClientSession,
                        filter: Bson,
                        update: Seq[Bson],
                        options: FindOneAndUpdateOptions
                      ): IO[Throwable, T] =
    SingleItemSubscription(wrapped.findOneAndUpdate(clientSession, filter, update.asJava, options)).fetch
  /**
    * Drops this collection from the Database.
    */
  def drop(): IO[Throwable, Completed] = CompletedSubscription(wrapped.drop()).fetch

  /**
    * Drops this collection from the Database.
    *
    */
  def drop(clientSession: ClientSession): IO[Throwable, Completed] = CompletedSubscription(wrapped.drop(clientSession)).fetch

  /**
   * Creates an index
    */
  def createIndex(key: Bson): IO[Throwable, String] =
    SingleItemSubscription(wrapped.createIndex(key)).fetch

  /**
   *  Creates an index
    */
  def createIndex(key: Bson, options: IndexOptions): IO[Throwable, String] =
    SingleItemSubscription(wrapped.createIndex(key, options)).fetch

  /**
    *  Creates an index
    */
  def createIndex(clientSession: ClientSession, key: Bson): IO[Throwable, String] =
    SingleItemSubscription(wrapped.createIndex(clientSession, key)).fetch

  /**
    *  Creates an index
    */
  def createIndex(clientSession: ClientSession, key: Bson, options: IndexOptions): IO[Throwable, String] =
    SingleItemSubscription(wrapped.createIndex(clientSession, key, options)).fetch

  /**
    *  Creates an index
    */
  def createIndexes(models: Seq[IndexModel]): IO[Throwable, String] =
    SingleItemSubscription(wrapped.createIndexes(models.asJava)).fetch

  /**
    * Create multiple indexes.
    *
    */
  def createIndexes(models: Seq[IndexModel], createIndexOptions: CreateIndexOptions): IO[Throwable, String] =
    SingleItemSubscription(wrapped.createIndexes(models.asJava, createIndexOptions)).fetch

  /**
    * Create multiple indexes.
    *
    */
  def createIndexes(clientSession: ClientSession, models: Seq[IndexModel]): IO[Throwable, String] =
    SingleItemSubscription(wrapped.createIndexes(clientSession, models.asJava)).fetch

  /**
    * Create multiple indexes.
    *
    */
  def createIndexes(clientSession: ClientSession, models: Seq[IndexModel], createIndexOptions: CreateIndexOptions): IO[Throwable, String] =
    SingleItemSubscription(wrapped.createIndexes(clientSession, models.asJava, createIndexOptions)).fetch

  /**
    * Get all the indexes in this collection.
    *
    */
  def listIndexes[C]()(implicit e: C MapTo Document, ct: ClassTag[C]): ListIndexesSubscription[C] =
    ListIndexesSubscription(wrapped.listIndexes(clazz(ct)))

  /**
    * Get all the indexes in this collection.
    *
    */
  def listIndexes[C](clientSession: ClientSession)(implicit e: C MapTo Document, ct: ClassTag[C]): ListIndexesSubscription[C] =
    ListIndexesSubscription(wrapped.listIndexes(clientSession, clazz(ct)))

  /**
    * Drops the given index.
    *
    */
  def dropIndex(indexName: String): IO[Throwable, Completed] =
    CompletedSubscription(wrapped.dropIndex(indexName)).fetch

  /**
    * Drops the given index.
    */
  def dropIndex(indexName: String, dropIndexOptions: DropIndexOptions): IO[Throwable, Completed] =
    CompletedSubscription(wrapped.dropIndex(indexName, dropIndexOptions)).fetch

  /**
    * Drops the index given the keys used to create it.
    */
  def dropIndex(keys: Bson): IO[Throwable, Completed] =
    CompletedSubscription(wrapped.dropIndex(keys)).fetch

  /**
    * Drops the index given the keys used to create it.
    *
    */
  def dropIndex(keys: Bson, dropIndexOptions: DropIndexOptions): IO[Throwable, Completed] =
    CompletedSubscription(wrapped.dropIndex(keys, dropIndexOptions)).fetch

  /**
    * Drops the given index.
    *
    */
  def dropIndex(clientSession: ClientSession, indexName: String) =
    SingleItemSubscription(wrapped.dropIndex(clientSession, indexName)).fetch

  /**
    * Drops the given index.
    *
    */
  def dropIndex(clientSession: ClientSession, indexName: String, dropIndexOptions: DropIndexOptions): IO[Throwable, Completed] =
    CompletedSubscription(wrapped.dropIndex(clientSession, indexName, dropIndexOptions)).fetch

  /**
    * Drops the index given the keys used to create it.
    *
    */
  def dropIndex(clientSession: ClientSession, keys: Bson): IO[Throwable, Completed] =
    CompletedSubscription(wrapped.dropIndex(clientSession, keys)).fetch

  /**
    * Drops the index given the keys used to create it.
    *
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
    */
  def dropIndexes(): IO[Throwable, Completed] =
    CompletedSubscription(wrapped.dropIndexes()).fetch

  /**
    * Drop all the indexes on this collection, except for the default on _id.
    *
    */
  def dropIndexes(dropIndexOptions: DropIndexOptions): IO[Throwable, Completed] =
    CompletedSubscription(wrapped.dropIndexes(dropIndexOptions)).fetch

  /**
    * Drop all the indexes on this collection, except for the default on _id.
    *
    */
  def dropIndexes(clientSession: ClientSession): IO[Throwable, Completed] =
    CompletedSubscription(wrapped.dropIndexes(clientSession)).fetch

  /**
    * Drop all the indexes on this collection, except for the default on _id.
    *
    */
  def dropIndexes(clientSession: ClientSession, dropIndexOptions: DropIndexOptions): IO[Throwable, Completed] =
    CompletedSubscription(wrapped.dropIndexes(clientSession, dropIndexOptions)).fetch

  /**
    * Rename the collection with oldCollectionName to the newCollectionName.
    *
    */
  def renameCollection(newCollectionNamespace: MongoNamespace): IO[Throwable, Completed] =
    CompletedSubscription(wrapped.renameCollection(newCollectionNamespace)).fetch

  /**
    * Rename the collection with oldCollectionName to the newCollectionName.
    *
    */
  def renameCollection(newCollectionNamespace: MongoNamespace, options: RenameCollectionOptions): IO[Throwable, Completed] =
    CompletedSubscription(wrapped.renameCollection(newCollectionNamespace, options)).fetch

  /**
    * Rename the collection with oldCollectionName to the newCollectionName.
    *
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
