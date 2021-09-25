package io.github.mbannour

import com.mongodb.client.model.changestream.ChangeStreamDocument
import com.mongodb.{ClientSessionOptions, ConnectionString, MongoClientSettings, MongoDriverInformation}
import com.mongodb.reactivestreams.client.{ClientSession, MongoClients, MongoClient => JMongoClient}
import io.github.mbannour.subscriptions.{ChangeStreamSubscription, ListDatabasesSubscription, ListSubscription, SingleItemSubscription}
import org.bson
import org.bson.codecs.configuration.CodecRegistries.fromRegistries
import org.bson.codecs.configuration.CodecRegistry
import org.bson.conversions.Bson
import zio.{IO, ZIO}

import scala.jdk.CollectionConverters._
import java.io.Closeable


object MongoClient {

  /**
   * Create a default MongoClient at localhost:27017
   *
   * @return MongoClient
   */
  def apply(): MongoClient = apply("mongodb://localhost:27017")

  /**
   * Create a MongoClient instance from a connection string uri
   *
   * @param uri the connection string
   * @return MongoClient
   */
  def apply(uri: String): MongoClient = MongoClient(uri, None)

  /**
   * Create a MongoClient instance from a connection string uri
   *
   * @param uri the connection string
   * @param mongoDriverInformation any driver information to associate with the MongoClient
   * @return MongoClient
   * @note the `mongoDriverInformation` is intended for driver and library authors to associate extra driver metadata with the connections.
   */
  def apply(uri: String, mongoDriverInformation: Option[MongoDriverInformation]): MongoClient = {
    apply(MongoClientSettings.builder().applyConnectionString(new ConnectionString(uri))
      .codecRegistry(DEFAULT_CODEC_REGISTRY).build(), mongoDriverInformation)
  }



  /**
   * Create a MongoClient instance from the MongoClientSettings
   *
   * @param clientSettings MongoClientSettings to use for the MongoClient
   * @return MongoClient
   * @since 2.3
   */
  def apply(clientSettings: MongoClientSettings): MongoClient = MongoClient(clientSettings, None)

  /**
   * Create a MongoClient instance from the MongoClientSettings
   *
   * @param clientSettings MongoClientSettings to use for the MongoClient
   * @param mongoDriverInformation any driver information to associate with the MongoClient
   * @return MongoClient
   * @note the `mongoDriverInformation` is intended for driver and library authors to associate extra driver metadata with the connections.
   * @since 2.3
   */
  def apply(clientSettings:MongoClientSettings, mongoDriverInformation: Option[MongoDriverInformation]): MongoClient =
  {
    val builder = mongoDriverInformation match {
      case Some(info) => MongoDriverInformation.builder(info)
      case None       => MongoDriverInformation.builder()
    }
    MongoClient(MongoClients.create(clientSettings, builder.build()))
  }

  val DEFAULT_CODEC_REGISTRY: CodecRegistry = fromRegistries(MongoClients.getDefaultCodecRegistry
  )
}

case class MongoClient(private val wrapped: JMongoClient) extends Closeable {
  /**
   * Creates a client session.
   *
   * '''Note:''' A ClientSession instance can not be used concurrently in multiple asynchronous operations.
   *
   * @since 2.4
   * @note Requires MongoDB 3.6 or greater
   */
  def startSession(): SingleItemSubscription[ClientSession] =
    SingleItemSubscription(wrapped.startSession())

  /**
   * Creates a client session.
   *
   * '''Note:''' A ClientSession instance can not be used concurrently in multiple asynchronous operations.
   *
   * @param options  the options for the client session
   * @since 2.2
   * @note Requires MongoDB 3.6 or greater
   */
  def startSession(options: ClientSessionOptions): SingleItemSubscription[ClientSession] =
    SingleItemSubscription(wrapped.startSession(options))

  /**
   * Gets the database with the given name.
   *
   * @param name the name of the database
   * @return the database
   */
  def getDatabase(name: String): MongoDatabase = MongoDatabase(wrapped.getDatabase(name))

  /**
   * Close the client, which will close all underlying cached resources, including, for example,
   * sockets and background monitoring threads.
   */
  def close(): Unit = wrapped.close()

  /**
   * Get a list of the database names
   *
   * [[http://docs.mongodb.org/manual/reference/commands/listDatabases List Databases]]
   * @return an iterable containing all the names of all the databases
   */
  def listDatabaseNames(): IO[Throwable, Iterable[String]] = ListSubscription(wrapped.listDatabaseNames()).subscribe

  /**
   * Get a list of the database names
   *
   * [[http://docs.mongodb.org/manual/reference/commands/listDatabases List Databases]]
   *
   * @param clientSession the client session with which to associate this operation
   * @return an iterable containing all the names of all the databases
   * @since 2.2
   * @note Requires MongoDB 3.6 or greater
   */
  def listDatabaseNames(clientSession: ClientSession): IO[Throwable, Iterable[String]] =
    ListSubscription(wrapped.listDatabaseNames(clientSession)).subscribe



  def listDatabases(): IO[Throwable, Iterable[bson.Document]] =
    ListDatabasesSubscription(wrapped.listDatabases()).subscribe



  def listJsonDatabases(): ZIO[Any, Throwable, Iterable[String]] =
    ListDatabasesSubscription(wrapped.listDatabases()).subscribe.map(_.map(_.toJson))

  /**
   * Gets the list of databases
   *
   * @param clientSession the client session with which to associate this operation
   * @tparam TResult the type of the class to use instead of `Document`.
   * @return the fluent list databases interface
   * @since 2.2
   * @note Requires MongoDB 3.6 or greater
   */
  def listDatabases[TResult](clientSession: ClientSession): IO[Throwable, Iterable[bson.Document]] =
    ListDatabasesSubscription(wrapped.listDatabases(clientSession)).subscribe

  /**
   * Creates a change stream for this client.
   *
   * @param resultClass the class to decode each document into
   * @param <TResult>   the target document type of the iterable.
   * @return the change stream iterable
   * @mongodb.driver.dochub core/changestreams Change Streams
   * @since 1.9
   * @mongodb.server.release 4.0
   */
    def watch(): IO[Throwable, ChangeStreamDocument[bson.Document]] = ChangeStreamSubscription(wrapped.watch()).subscribe

    /**
     * Creates a change stream for this collection.
     *
     * @param pipeline the aggregation pipeline to apply to the change stream
     * @tparam C   the target document type of the observable.
     * @return ???
     * @since 2.4
     * @note Requires MongoDB 4.0 or greater
     *
     */
    def watch(pipeline: Seq[Bson]): IO[Throwable, ChangeStreamDocument[bson.Document]] =
      ChangeStreamSubscription(wrapped.watch(pipeline.asJava)).subscribe

    /**
     * Creates a change stream for this collection.
     *
     * @param clientSession the client session with which to associate this operation
     * @tparam C   the target document type of the observable.
     * @return ???
     * @since 2.4
     * @note Requires MongoDB 4.0 or greater
     */
    def watch(clientSession: ClientSession): IO[Throwable, ChangeStreamDocument[bson.Document]] =
      ChangeStreamSubscription(wrapped.watch(clientSession)).subscribe

    /**
     * Creates a change stream for this collection.
     *
     * @param clientSession the client session with which to associate this operation
     * @param pipeline the aggregation pipeline to apply to the change stream
     * @tparam C   the target document type of the observable.
     * @return ???
     * @since 2.4
     * @note Requires MongoDB 4.0 or greater
     */
    def watch[TResult](clientSession: ClientSession, pipeline: Seq[Bson]): IO[Throwable, ChangeStreamDocument[bson.Document]] =
      ChangeStreamSubscription(wrapped.watch(clientSession, pipeline.asJava)).subscribe

}

