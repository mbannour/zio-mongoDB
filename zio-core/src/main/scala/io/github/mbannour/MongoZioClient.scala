package io.github.mbannour

import com.mongodb.{ClientSessionOptions, ConnectionString, MongoClientSettings, MongoDriverInformation}
import com.mongodb.reactivestreams.client.{ClientSession, MongoClients, MongoClient => JMongoClient}
import io.github.mbannour.subscriptions.{ChangeStreamSubscription, ListDatabasesSubscription, ListSubscription, SingleItemSubscription}
import org.bson
import org.bson.codecs.configuration.CodecRegistries.fromRegistries
import org.bson.codecs.configuration.CodecRegistry
import org.bson.conversions.Bson
import zio.{IO, Task, ZIO, ZManaged}

import scala.collection.JavaConverters._
import java.io.Closeable


object MongoZioClient {

  /**
    * Create a default MongoZioClient at localhost:27017
    *
    * @return a Task of MongoZioClient
    */
  def apply(): Task[MongoZioClient] = apply("mongodb://localhost:27017")

  /**
    * Create a MongoZioClient instance from a connection string uri
    *
    * @param uri the connection string
    * @return a Task of MongoZioClient
    */
  def apply(uri: String): Task[MongoZioClient] = MongoZioClient(uri, None)


  /**
    * Create an auto closable MongoZioClient instance from a connection string uri
    *
    * @param uri the connection string
    * @return a ZManaged of MongoZioClient
    */
  def autoCloseableClient(uri: String): ZManaged[Any, Throwable, MongoZioClient] = ZManaged.fromAutoCloseable(apply(uri))

  /**
    * Create a MongoZioClient instance from a connection string uri
    *
    * @param uri the connection string
    * @param mongoDriverInformation any driver information to associate with the MongoZioClient
    * @return a Task of MongoZioClient
    */
  def apply(uri: String, mongoDriverInformation: Option[MongoDriverInformation]): Task[MongoZioClient] = {
    apply(MongoClientSettings.builder().applyConnectionString(new ConnectionString(uri))
      .codecRegistry(DEFAULT_CODEC_REGISTRY).build(), mongoDriverInformation)
  }

  /**
    * Create a MongoZioClient instance from the MongoClientSettings
    *
    * @param clientSettings MongoClientSettings to use for the MongoClientSettings
    * @return a Task of MongoZioClient
    */
  def apply(clientSettings: MongoClientSettings): Task[MongoZioClient] = MongoZioClient(clientSettings, None)

  /**
    * Create a MongoZioClient instance from the MongoClientSettings
    *
    * @param clientSettings MongoClientSettings to use for the MongoZioClient
    * @param mongoDriverInformation any driver information to associate with the MongoZioClient
    * @return zio tak MongoZioClient
    */
  def apply(clientSettings:MongoClientSettings, mongoDriverInformation: Option[MongoDriverInformation]): Task[MongoZioClient] =
    ZIO.effect(createMongoClient(clientSettings, mongoDriverInformation))


  private[mbannour] def createMongoClient(clientSettings:MongoClientSettings, mongoDriverInformation: Option[MongoDriverInformation]) = {
    val builder = mongoDriverInformation match {
      case Some(info) => MongoDriverInformation.builder(info)
      case None => MongoDriverInformation.builder()
    }
    MongoZioClient(MongoClients.create(clientSettings, builder.build()))
  }

  val DEFAULT_CODEC_REGISTRY: CodecRegistry = fromRegistries(MongoClients.getDefaultCodecRegistry

  )
}

case class MongoZioClient(private val wrapped: JMongoClient) extends Closeable {
  /**
    * Creates a client session.
    *
    * '''Note:''' A ClientSession instance can not be used concurrently in multiple asynchronous operations.
    *
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
  def getDatabase(name: String): Task[MongoZioDatabase] = ZIO.effect(MongoZioDatabase(wrapped.getDatabase(name)))

  /**
    * Close the client, which will close all underlying cached resources, including, for example,
    * sockets and background monitoring threads.
    */
  def close(): Unit = wrapped.close()


  /**
    * Close the client , which will close all underlying cached resources, including, for example,
    * sockets and background monitoring threads.
    */
  def pureClose(): Task[Unit] = ZIO.effect(close())

  /**
    * Get a list of the database names
    *
    * [[http://docs.mongodb.org/manual/reference/commands/listDatabases List Databases]]
    * @return an iterable containing all the names of all the databases
    */
  def listDatabaseNames(): IO[Throwable, Iterable[String]] = ListSubscription(wrapped.listDatabaseNames()).fetch

  /**
    * Get a list of the database names
    *
    * [[http://docs.mongodb.org/manual/reference/commands/listDatabases List Databases]]
    *
    * @param clientSession the client session with which to associate this operation
    * @return an iterable containing all the names of all the databases
    * @note Requires MongoDB 3.6 or greater
    */
  def listDatabaseNames(clientSession: ClientSession): IO[Throwable, Iterable[String]] =
    ListSubscription(wrapped.listDatabaseNames(clientSession)).fetch

  /**
    * Get a list of the database names
    *
    * [[http://docs.mongodb.org/manual/reference/commands/listDatabases List Databases]]
    *
    * @return an iterable containing all the names of all the databases
    * @note Requires MongoDB 3.6 or greater
    */
  def listDatabases(): ListDatabasesSubscription[bson.Document] =
    ListDatabasesSubscription(wrapped.listDatabases())

  /**
    * Gets the list of databases
    *
    * @param clientSession the client session with which to associate this operation
    * @tparam TResult the type of the class to use instead of `Document`.
    * @return the fluent list databases interface
    * @note Requires MongoDB 3.6 or greater
    */
  def listDatabases[TResult](clientSession: ClientSession): ListDatabasesSubscription[bson.Document] =
    ListDatabasesSubscription(wrapped.listDatabases(clientSession))

  /**
    * Creates a change stream for this client.
    * @return the ChangeStreamSubscription
    * @mongodb.driver.dochub core/changestreams Change Streams
    * @mongodb.server.release 4.0
    */
  def watch(): ChangeStreamSubscription[bson.Document] = ChangeStreamSubscription(wrapped.watch())

  /**
    * Creates a change stream for this collection.
    *
    * @param pipeline the aggregation pipeline to apply to the change stream
    * @return the change stream iterable
    * @note Requires MongoDB 4.0 or greater
    *
    */
  def watch(pipeline: Seq[Bson]): ChangeStreamSubscription[bson.Document] =
    ChangeStreamSubscription(wrapped.watch(pipeline.asJava))

  /**
    * Creates a change stream for this collection.
    *
    * @param clientSession the client session with which to associate this operation
    * @return the change stream iterable
    * @note Requires MongoDB 4.0 or greater
    */
  def watch(clientSession: ClientSession): ChangeStreamSubscription[bson.Document] =
    ChangeStreamSubscription(wrapped.watch(clientSession))

  /**
    * Creates a change stream for this collection.
    *
    * @param clientSession the client session with which to associate this operation
    * @param pipeline the aggregation pipeline to apply to the change stream
    * @return the change stream iterable
    * @note Requires MongoDB 4.0 or greater
    */
  def watch[TResult](clientSession: ClientSession, pipeline: Seq[Bson]): ChangeStreamSubscription[bson.Document] =
    ChangeStreamSubscription(wrapped.watch(clientSession, pipeline.asJava))

}

