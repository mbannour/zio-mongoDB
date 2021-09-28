package io.github.mbannour

import com.mongodb.client.model.changestream.ChangeStreamDocument
import com.mongodb.client.model.{CreateCollectionOptions, CreateViewOptions}
import com.mongodb.{ReadConcern, ReadPreference, WriteConcern}
import com.mongodb.reactivestreams.client.ClientSession
import io.github.mbannour.result.Completed
import io.github.mbannour.subscriptions.{AggregateSubscription, ChangeStreamSubscription, CompletedSubscription, ListCollectionsSubscription, SingleItemSubscription}
import org.bson

import scala.jdk.CollectionConverters._
import org.bson.codecs.configuration.CodecRegistry
import org.bson.conversions.Bson
import zio.{IO, UIO, ZIO}

import scala.reflect.ClassTag

case class MongoDatabase(private val javaMongoDatabase: JavaMongoDatabase) {

  /**
   * Gets the name of the database.
   *
   * @return the database name
   */
  lazy val name: String = javaMongoDatabase.getName

  /**
   * Get the codec registry for the MongoDatabase.
   *
   * @return the {@link org.bson.codecs.configuration.CodecRegistry}
   */
  lazy val codecRegistry: CodecRegistry = javaMongoDatabase.getCodecRegistry

  /**
   * Get the read preference for the MongoDatabase.
   *
   * @return the {@link com.mongodb.ReadPreference}
   */
  lazy val readPreference: ReadPreference = javaMongoDatabase.getReadPreference

  /**
   * Get the write concern for the MongoDatabase.
   *
   * @return the {@link com.mongodb.WriteConcern}
   */
  lazy val writeConcern: WriteConcern = javaMongoDatabase.getWriteConcern

  /**
   * Get the read concern for the MongoDatabase.
   *
   * @return the [[ReadConcern]]
   * @since 1.1
   */
  lazy val readConcern: ReadConcern = javaMongoDatabase.getReadConcern

  /**
   * Create a new MongoDatabase instance with a different codec registry.
   *
   * @param codecRegistry the new { @link org.bson.codecs.configuration.CodecRegistry} for the collection
   * @return a new MongoDatabase instance with the different codec registry
   */
  def withCodecRegistry(codecRegistry: CodecRegistry): MongoDatabase =
   MongoDatabase(javaMongoDatabase.withCodecRegistry(codecRegistry))

  /**
   * Create a new MongoDatabase instance with a different read preference.
   *
   * @param readPreference the new { @link com.mongodb.ReadPreference} for the collection
   * @return a new MongoDatabase instance with the different readPreference
   */
  def withReadPreference(readPreference: ReadPreference): MongoDatabase =
    MongoDatabase(javaMongoDatabase.withReadPreference(readPreference))

  /**
   * Create a new MongoDatabase instance with a different write concern.
   *
   * @param writeConcern the new { @link com.mongodb.WriteConcern} for the collection
   * @return a new MongoDatabase instance with the different writeConcern
   */
  def withWriteConcern(writeConcern: WriteConcern): MongoDatabase =
    MongoDatabase(javaMongoDatabase.withWriteConcern(writeConcern))

  /**
   * Create a new MongoDatabase instance with a different read concern.
   *
   * @param readConcern the new [[ReadConcern]] for the collection
   * @return a new MongoDatabase instance with the different ReadConcern
   * @since 1.1
   */
  def withReadConcern(readConcern: ReadConcern): MongoDatabase =
    MongoDatabase(javaMongoDatabase.withReadConcern(readConcern))

  /**
   * Gets a collection, with a specific default document class.
   *
   * @param collectionName the name of the collection to return
   * @tparam TResult       the type of the class to use instead of [[Document]].
   * @return the collection
   */
   def getCollection[TResult](collectionName: String)(implicit ct: ClassTag[TResult]): UIO[MongoCollection[TResult]] =
    ZIO.succeed(MongoCollection(javaMongoDatabase.getCollection(collectionName, ct.runtimeClass.asInstanceOf[Class[TResult]])))

  /**
     * Executes command in the context of the current database using the primary server.
   *
   * @param command  the command to be run
   * @tparam TResult the type of the class to use instead of [[Document]].
   * @return a Observable containing the command result
   */
    def runCommand[TResult](command: Bson)(implicit ct: ClassTag[TResult]): IO[Throwable, TResult] =
      SingleItemSubscription(javaMongoDatabase.runCommand[TResult](command, ct.runtimeClass.asInstanceOf[Class[TResult]])).fetch

    /**
     * Executes command in the context of the current database.
     *
     * @param command        the command to be run
     * @param readPreference the [[ReadPreference]] to be used when executing the command
     * @tparam TResult       the type of the class to use instead of [[Document]].
     * @return a Observable containing the command result
     */
    def runCommand[TResult](command: Bson, readPreference: ReadPreference)(implicit ct: ClassTag[TResult]): IO[Throwable, TResult] =
      SingleItemSubscription(javaMongoDatabase.runCommand(command, readPreference, ct.runtimeClass.asInstanceOf[Class[TResult]])).fetch

    /**
     * Executes command in the context of the current database using the primary server.
     *
     * @param clientSession the client session with which to associate this operation
     * @param command  the command to be run
     * @tparam TResult the type of the class to use instead of [[Document]].
     * @return a Observable containing the command result
     * @since 2.2
     * @note Requires MongoDB 3.6 or greater
     */
    def runCommand[TResult](clientSession: ClientSession, command: Bson)(implicit ct: ClassTag[TResult]): IO[Throwable, TResult] =
      SingleItemSubscription(javaMongoDatabase.runCommand[TResult](clientSession, command, ct.runtimeClass.asInstanceOf[Class[TResult]])).fetch

    /**
     * Executes command in the context of the current database.
     *
     * @param command        the command to be run
     * @param readPreference the [[ReadPreference]] to be used when executing the command
     * @tparam TResult       the type of the class to use instead of [[Document]].
     * @return a Observable containing the command result
     * @since 2.2
     * @note Requires MongoDB 3.6 or greater
     */
    def runCommand[TResult](clientSession: ClientSession, command: Bson, readPreference: ReadPreference)(implicit ct: ClassTag[TResult]) =
      SingleItemSubscription(javaMongoDatabase.runCommand(clientSession, command, readPreference, ct.runtimeClass.asInstanceOf[Class[TResult]])).fetch

    /**
     * Drops this database.
     *
     * [[http://docs.mongodb.org/manual/reference/commands/dropDatabase/#dbcmd.dropDatabase Drop database]]
     * @return a Observable identifying when the database has been dropped
     */
    def drop(): IO[Throwable, Completed] = CompletedSubscription(javaMongoDatabase.drop()).fetch

    /**
     * Drops this database.
     *
     * [[http://docs.mongodb.org/manual/reference/commands/dropDatabase/#dbcmd.dropDatabase Drop database]]
     * @param clientSession the client session with which to associate this operation
     * @return a Observable identifying when the database has been dropped
     * @since 2.2
     * @note Requires MongoDB 3.6 or greater
     */
    def drop(clientSession: ClientSession): IO[Throwable, Completed] =
      CompletedSubscription(javaMongoDatabase.drop(clientSession)).fetch

    /**
     * Gets the names of all the collections in this database.
     *
     * @return a Observable with all the names of all the collections in this database
     */
    def listCollectionNames(): IO[Throwable, String] = SingleItemSubscription(javaMongoDatabase.listCollectionNames()).fetch

    /**
     * Finds all the collections in this database.
     *
     * [[http://docs.mongodb.org/manual/reference/command/listCollections listCollections]]
     * @tparam TResult the target document type of the iterable.
     * @return the fluent list collections interface
     */
    def listCollections[TResult]()(implicit ct: ClassTag[TResult]): IO[Throwable, Iterable[TResult]] =
      ListCollectionsSubscription(javaMongoDatabase.listCollections(ct.runtimeClass.asInstanceOf[Class[TResult]])).fetch

    /**
     * Gets the names of all the collections in this database.
     *
     * @param clientSession the client session with which to associate this operation
     * @return a Observable with all the names of all the collections in this database
     * @since 2.2
     * @note Requires MongoDB 3.6 or greater
     */
    def listCollectionNames(clientSession: ClientSession): IO[Throwable, String] = SingleItemSubscription(javaMongoDatabase.listCollectionNames(clientSession)).fetch

    /**
     * Finds all the collections in this database.
     *
     * [[http://docs.mongodb.org/manual/reference/command/listCollections listCollections]]
     * @param clientSession the client session with which to associate this operation
     * @tparam TResult the target document type of the iterable.
     * @return the fluent list collections interface
     * @since 2.2
     * @note Requires MongoDB 3.6 or greater
     */
    def listCollections[TResult](clientSession: ClientSession)(implicit ct: ClassTag[TResult]
    ): IO[Throwable, Iterable[TResult]] =
      ListCollectionsSubscription(javaMongoDatabase.listCollections(clientSession, ct.runtimeClass.asInstanceOf[Class[TResult]])).fetch

    /**
     * Create a new collection with the given name.
     *
     * [[http://docs.mongodb.org/manual/reference/commands/create Create Command]]
     * @param collectionName the name for the new collection to create
     * @return a Observable identifying when the collection has been created
     */
    def createCollection(collectionName: String): IO[Throwable, Completed] =
      CompletedSubscription(javaMongoDatabase.createCollection(collectionName)).fetch

    /**
     * Create a new collection with the selected options
     *
     * [[http://docs.mongodb.org/manual/reference/commands/create Create Command]]
     * @param collectionName the name for the new collection to create
     * @param options        various options for creating the collection
     * @return a Observable identifying when the collection has been created
     */
    def createCollection(collectionName: String, options: CreateCollectionOptions): IO[Throwable, Completed] =
      CompletedSubscription(javaMongoDatabase.createCollection(collectionName, options)).fetch

    /**
     * Create a new collection with the given name.
     *
     * [[http://docs.mongodb.org/manual/reference/commands/create Create Command]]
     * @param clientSession the client session with which to associate this operation
     * @param collectionName the name for the new collection to create
     * @return a Observable identifying when the collection has been created
     * @since 2.2
     * @note Requires MongoDB 3.6 or greater
     */
    def createCollection(clientSession: ClientSession, collectionName: String): IO[Throwable, Completed] =
      CompletedSubscription(javaMongoDatabase.createCollection(clientSession, collectionName)).fetch

    /**
     * Create a new collection with the selected options
     *
     * [[http://docs.mongodb.org/manual/reference/commands/create Create Command]]
     * @param clientSession the client session with which to associate this operation
     * @param collectionName the name for the new collection to create
     * @param options        various options for creating the collection
     * @return a Observable identifying when the collection has been created
     * @since 2.2
     * @note Requires MongoDB 3.6 or greater
     */
    def createCollection(clientSession: ClientSession, collectionName: String, options: CreateCollectionOptions): IO[Throwable, Completed] =
      CompletedSubscription(javaMongoDatabase.createCollection(clientSession, collectionName, options)).fetch

    /**
     * Creates a view with the given name, backing collection/view name, and aggregation pipeline that defines the view.
     *
     * [[http://docs.mongodb.org/manual/reference/commands/create Create Command]]
     * @param viewName the name of the view to create
     * @param viewOn   the backing collection/view for the view
     * @param pipeline the pipeline that defines the view
     * @since 1.2
     * @note Requires MongoDB 3.4 or greater
     */
    def createView(viewName: String, viewOn: String, pipeline: Seq[Bson]): IO[Throwable, Completed] =
      CompletedSubscription(javaMongoDatabase.createView(viewName, viewOn, pipeline.asJava)).fetch

    /**
     * Creates a view with the given name, backing collection/view name, aggregation pipeline, and options that defines the view.
     *
     * [[http://docs.mongodb.org/manual/reference/commands/create Create Command]]
     * @param viewName          the name of the view to create
     * @param viewOn            the backing collection/view for the view
     * @param pipeline          the pipeline that defines the view
     * @param createViewOptions various options for creating the view
     * @since 1.2
     * @note Requires MongoDB 3.4 or greater
     */
    def createView(viewName: String, viewOn: String, pipeline: Seq[Bson], createViewOptions: CreateViewOptions): IO[Throwable, Completed] =
      CompletedSubscription(javaMongoDatabase.createView(viewName, viewOn, pipeline.asJava, createViewOptions)).fetch

    /**
     * Creates a view with the given name, backing collection/view name, and aggregation pipeline that defines the view.
     *
     * [[http://docs.mongodb.org/manual/reference/commands/create Create Command]]
     * @param clientSession the client session with which to associate this operation
     * @param viewName the name of the view to create
     * @param viewOn   the backing collection/view for the view
     * @param pipeline the pipeline that defines the view
     * @since 2.2
     * @note Requires MongoDB 3.6 or greater
     */
    def createView(clientSession: ClientSession, viewName: String, viewOn: String, pipeline: Seq[Bson]): IO[Throwable, Completed] =
      CompletedSubscription(javaMongoDatabase.createView(clientSession, viewName, viewOn, pipeline.asJava)).fetch

    /**
     * Creates a view with the given name, backing collection/view name, aggregation pipeline, and options that defines the view.
     *
     * [[http://docs.mongodb.org/manual/reference/commands/create Create Command]]
     * @param clientSession the client session with which to associate this operation
     * @param viewName          the name of the view to create
     * @param viewOn            the backing collection/view for the view
     * @param pipeline          the pipeline that defines the view
     * @param createViewOptions various options for creating the view
     * @since 2.2
     * @note Requires MongoDB 3.6 or greater
     */
    def createView(clientSession: ClientSession, viewName: String, viewOn: String, pipeline: Seq[Bson],
                   createViewOptions: CreateViewOptions): IO[Throwable, Completed] =
      CompletedSubscription(javaMongoDatabase.createView(clientSession, viewName, viewOn, pipeline.asJava, createViewOptions)).fetch

    /**
     * Creates a change stream for this collection.
     *
     * @tparam C   the target document type of the observable.
     * @return the change stream observable
     * @since 2.4
     * @note Requires MongoDB 4.0 or greater
     */
    def watch(): IO[Throwable, ChangeStreamDocument[bson.Document]] =
      ChangeStreamSubscription(javaMongoDatabase.watch()).fetch

    /**
     * Creates a change stream for this collection.
     *
     * @param pipeline the aggregation pipeline to apply to the change stream
     * @tparam C   the target document type of the observable.
     * @return the change stream observable
     * @since 2.4
     * @note Requires MongoDB 4.0 or greater
     */
    def watch(pipeline: Seq[Bson]): IO[Throwable, ChangeStreamDocument[bson.Document]] =
      ChangeStreamSubscription(javaMongoDatabase.watch(pipeline.asJava)).fetch

    /**
     * Creates a change stream for this collection.
     *
     * @param clientSession the client session with which to associate this operation
     * @tparam C   the target document type of the observable.
     * @return the change stream observable
     * @since 2.4
     * @note Requires MongoDB 4.0 or greater
     */
    def watch(clientSession: ClientSession): IO[Throwable, ChangeStreamDocument[bson.Document]] =
      ChangeStreamSubscription(javaMongoDatabase.watch(clientSession)).fetch

    /**
     * Creates a change stream for this collection.
     *
     * @param clientSession the client session with which to associate this operation
     * @param pipeline the aggregation pipeline to apply to the change stream
     * @tparam C   the target document type of the observable.
     * @return the change stream observable
     * @since 2.4
     * @note Requires MongoDB 4.0 or greater
     */
    def watch(clientSession: ClientSession, pipeline: Seq[Bson]): IO[Throwable, ChangeStreamDocument[bson.Document]] =
      ChangeStreamSubscription(javaMongoDatabase.watch(clientSession, pipeline.asJava)).fetch

    /**
     * Aggregates documents according to the specified aggregation pipeline.
     *
     * @param pipeline the aggregate pipeline
     * @return a Observable containing the result of the aggregation operation
     *         [[http://docs.mongodb.org/manual/aggregation/ Aggregation]]
     * @since 2.6
     * @note Requires MongoDB 3.6 or greater
     */
    def aggregate[C](pipeline: Seq[Bson])(ct: ClassTag[C]): IO[Throwable, Iterable[C]] =
      AggregateSubscription(javaMongoDatabase.aggregate[C](pipeline.asJava,  ct.runtimeClass.asInstanceOf[Class[C]])).fetch

    /**
     * Aggregates documents according to the specified aggregation pipeline.
     *
     * @param clientSession the client session with which to associate this operation
     * @param pipeline the aggregate pipeline
     * @return a Observable containing the result of the aggregation operation
     *         [[http://docs.mongodb.org/manual/aggregation/ Aggregation]]
     * @since 2.6
     * @note Requires MongoDB 3.6 or greater
     */
    def aggregate[C](clientSession: ClientSession, pipeline: Seq[Bson])(ct: ClassTag[C]): IO[Throwable, Iterable[C]] =
      AggregateSubscription(javaMongoDatabase.aggregate(clientSession, pipeline.asJava, ct.runtimeClass.asInstanceOf[Class[C]])).fetch
}


