package io.github.mbannour

import com.mongodb.client.model.changestream.ChangeStreamDocument
import com.mongodb.client.model.{CreateCollectionOptions, CreateViewOptions}
import com.mongodb.{ReadConcern, ReadPreference, WriteConcern}
import com.mongodb.reactivestreams.client.ClientSession
import io.github.mbannour.DefaultHelper.MapTo
import io.github.mbannour.result.Completed
import io.github.mbannour.subscriptions.{AggregateSubscription, ChangeStreamSubscription, CompletedSubscription, ListCollectionsSubscription, SingleItemSubscription}
import org.bson

import scala.jdk.CollectionConverters._
import org.bson.codecs.configuration.CodecRegistry
import org.bson.conversions.Bson
import org.mongodb.scala.bson.collection.immutable.Document
import zio.{IO, Task, ZIO}

import scala.reflect.ClassTag

case class MongoZioDatabase(private val javaMongoDatabase: JavaMongoDatabase) {

  /**
    * Gets the name of the database.
    *
    */
  lazy val name: String = javaMongoDatabase.getName

  /**
    * Get the codec registry for the MongoDatabase.
    *
    */
  lazy val codecRegistry: CodecRegistry = javaMongoDatabase.getCodecRegistry

  /**
    * Get the read preference for the MongoDatabase.
    *
    */
  lazy val readPreference: ReadPreference = javaMongoDatabase.getReadPreference

  /**
    * Get the write concern for the MongoDatabase.
    *
    */
  lazy val writeConcern: WriteConcern = javaMongoDatabase.getWriteConcern

  /**
    * Get the read concern for the MongoDatabase.
    *
    */
  lazy val readConcern: ReadConcern = javaMongoDatabase.getReadConcern

  /**
    * Create a new MongoZioDatabase instance with a different codec registry.
    *
    */
  def withCodecRegistry(codecRegistry: CodecRegistry): MongoZioDatabase =
    MongoZioDatabase(javaMongoDatabase.withCodecRegistry(codecRegistry))

  /**
    * Create a new MongoZioDatabase instance with a different read preference.
    *
    */
  def withReadPreference(readPreference: ReadPreference): MongoZioDatabase =
    MongoZioDatabase(javaMongoDatabase.withReadPreference(readPreference))

  /**
    * Create a new MongoZioDatabase instance with a different write concern.
    */
  def withWriteConcern(writeConcern: WriteConcern): MongoZioDatabase =
    MongoZioDatabase(javaMongoDatabase.withWriteConcern(writeConcern))

  /**
    * Create a new MongoZioDatabase instance with a different read concern.
    *
    */
  def withReadConcern(readConcern: ReadConcern): MongoZioDatabase =
    MongoZioDatabase(javaMongoDatabase.withReadConcern(readConcern))

  /**
    * Gets a MongoZioCollection, with a specific default document class.
    *
    */
  def getCollection[TResult](collectionName: String)(implicit e: TResult MapTo Document, ct: ClassTag[TResult]): Task[MongoZioCollection[TResult]] =
    ZIO.attempt(MongoZioCollection(javaMongoDatabase.getCollection(collectionName, clazz(ct))))

  /**
    * Executes command in the context of the current database.
    */
  def runCommand[TResult](command: Bson)(implicit e: TResult MapTo Document, ct: ClassTag[TResult]): IO[Throwable, TResult] =
    SingleItemSubscription(javaMongoDatabase.runCommand[TResult](command, clazz(ct))).fetch

  /**
    * Executes command in the context of the current database.
    */
  def runCommand[TResult](command: Bson, readPreference: ReadPreference)(implicit e: TResult MapTo Document, ct: ClassTag[TResult]): IO[Throwable, TResult] =
    SingleItemSubscription(javaMongoDatabase.runCommand(command, readPreference, clazz(ct))).fetch

  /**
    * Executes command in the context of the current database.
    */
  def runCommand[TResult](clientSession: ClientSession, command: Bson)(implicit e: TResult MapTo Document, ct: ClassTag[TResult]): IO[Throwable, TResult] =
    SingleItemSubscription(javaMongoDatabase.runCommand[TResult](clientSession, command, clazz(ct))).fetch

  /**
    * Executes command in the context of the current database.
    */
  def runCommand[TResult](clientSession: ClientSession, command: Bson, readPreference: ReadPreference)(implicit e: TResult MapTo Document, ct: ClassTag[TResult]): IO[Throwable, TResult] =
    SingleItemSubscription(javaMongoDatabase.runCommand(clientSession, command, readPreference, clazz(ct))).fetch

  /**
    * Drops this database.
    *
    */
  def drop(): IO[Throwable, Completed] = CompletedSubscription(javaMongoDatabase.drop()).fetch

  /**
    * Drops this database.
    */
  def drop(clientSession: ClientSession): IO[Throwable, Completed] =
    CompletedSubscription(javaMongoDatabase.drop(clientSession)).fetch

  /**
    * Gets the names of all the collections in this database.
    */
  def listCollectionNames(): IO[Throwable, String] = SingleItemSubscription(javaMongoDatabase.listCollectionNames()).fetch

  /**
    * Finds all the collections in this database.
    *
    */
  def listCollections[TResult]()(implicit e: TResult MapTo Document, ct: ClassTag[TResult]): IO[Throwable, Iterable[TResult]] =
    ListCollectionsSubscription(javaMongoDatabase.listCollections(clazz(ct))).fetch

  /**
    * Gets the names of all the collections in this database.
    *
    */
  def listCollectionNames(clientSession: ClientSession): IO[Throwable, String] = SingleItemSubscription(javaMongoDatabase.listCollectionNames(clientSession)).fetch

  /**
    * Finds all the collections in this database.
    *
    */
  def listCollections[TResult](clientSession: ClientSession)(implicit e: TResult MapTo Document, ct: ClassTag[TResult]): IO[Throwable, Iterable[TResult]] =
    ListCollectionsSubscription(javaMongoDatabase.listCollections(clientSession, clazz(ct))).fetch

  /**
    * Create a new collection with the given name.
    */
  def createCollection(collectionName: String): IO[Throwable, Completed] =
    CompletedSubscription(javaMongoDatabase.createCollection(collectionName)).fetch

  /**
    * Create a new collection with the selected options
    *
    */
  def createCollection(collectionName: String, options: CreateCollectionOptions): IO[Throwable, Completed] =
    CompletedSubscription(javaMongoDatabase.createCollection(collectionName, options)).fetch

  /**
    * Create a new collection with the given name.
    *
    */
  def createCollection(clientSession: ClientSession, collectionName: String): IO[Throwable, Completed] =
    CompletedSubscription(javaMongoDatabase.createCollection(clientSession, collectionName)).fetch

  /**
    * Create a new collection with the selected options
    *
    */
  def createCollection(clientSession: ClientSession, collectionName: String, options: CreateCollectionOptions): IO[Throwable, Completed] =
    CompletedSubscription(javaMongoDatabase.createCollection(clientSession, collectionName, options)).fetch

  /**
    * Creates a view with the given name, backing collection/view name, and aggregation pipeline that defines the view.
    */
  def createView(viewName: String, viewOn: String, pipeline: Seq[Bson]): IO[Throwable, Completed] =
    CompletedSubscription(javaMongoDatabase.createView(viewName, viewOn, pipeline.asJava)).fetch

  /**
    * Creates a view with the given name, backing collection/view name, aggregation pipeline, and options that defines the view.
    */
  def createView(viewName: String, viewOn: String, pipeline: Seq[Bson], createViewOptions: CreateViewOptions): IO[Throwable, Completed] =
    CompletedSubscription(javaMongoDatabase.createView(viewName, viewOn, pipeline.asJava, createViewOptions)).fetch

  /**
    * Creates a view with the given name, backing collection/view name, and aggregation pipeline that defines the view.
    */
  def createView(clientSession: ClientSession, viewName: String, viewOn: String, pipeline: Seq[Bson]): IO[Throwable, Completed] =
    CompletedSubscription(javaMongoDatabase.createView(clientSession, viewName, viewOn, pipeline.asJava)).fetch

  /**
    * Creates a view with the given name, backing collection/view name, aggregation pipeline, and options that defines the view.
    *
    */
  def createView(clientSession: ClientSession, viewName: String, viewOn: String, pipeline: Seq[Bson], createViewOptions: CreateViewOptions): IO[Throwable, Completed] =
    CompletedSubscription(javaMongoDatabase.createView(clientSession, viewName, viewOn, pipeline.asJava, createViewOptions)).fetch

  /**
    * Creates a change stream for this collection.
    */
  def watch(): IO[Throwable, ChangeStreamDocument[bson.Document]] =
    ChangeStreamSubscription(javaMongoDatabase.watch()).fetch

  /**
    * Creates a change stream for this collection.
    *
    */
  def watch(pipeline: Seq[Bson]): IO[Throwable, ChangeStreamDocument[bson.Document]] =
    ChangeStreamSubscription(javaMongoDatabase.watch(pipeline.asJava)).fetch

  /**
    * Creates a change stream for this collection.
    *
    */
  def watch(clientSession: ClientSession): IO[Throwable, ChangeStreamDocument[bson.Document]] =
    ChangeStreamSubscription(javaMongoDatabase.watch(clientSession)).fetch

  /**
    * Creates a change stream for this collection.
    *
    */
  def watch(clientSession: ClientSession, pipeline: Seq[Bson]): IO[Throwable, ChangeStreamDocument[bson.Document]] =
    ChangeStreamSubscription(javaMongoDatabase.watch(clientSession, pipeline.asJava)).fetch

  /**
    * Aggregates documents according to the specified aggregation pipeline.
    *
    */
  def aggregate[C](pipeline: Seq[Bson])(ct: ClassTag[C]): IO[Throwable, Iterable[C]] =
    AggregateSubscription(javaMongoDatabase.aggregate[C](pipeline.asJava,  ct.runtimeClass.asInstanceOf[Class[C]])).fetch

  /**
    * Aggregates documents according to the specified aggregation pipeline.
    *
    */
  def aggregate[C](clientSession: ClientSession, pipeline: Seq[Bson])(ct: ClassTag[C]): IO[Throwable, Iterable[C]] =
    AggregateSubscription(javaMongoDatabase.aggregate(clientSession, pipeline.asJava, ct.runtimeClass.asInstanceOf[Class[C]])).fetch
}
