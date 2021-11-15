package io.github.mbannour.subscriptions

import io.github.mbannour.MongoTestClient.mongoTestClient
import io.github.mbannour.Person
import org.mongodb.scala.bson.codecs.Macros._
import org.bson.codecs.configuration.CodecRegistries.{fromProviders, fromRegistries}
import org.mongodb.scala.MongoClient.DEFAULT_CODEC_REGISTRY
import org.mongodb.scala.model.Filters
import zio.{Duration, ExecutionStrategy}
import zio.test.Assertion.equalTo
import zio.test.environment.TestEnvironment
import zio.test.{DefaultRunnableSpec, TestAspect, ZSpec, assertM}

object DistinctSubscriptionSpec extends DefaultRunnableSpec {

  val mongoClient = mongoTestClient()

  val codecRegistry = fromRegistries(fromProviders(classOf[Person]), DEFAULT_CODEC_REGISTRY)

  val database = mongoClient.getDatabase("mydb").map(_.withCodecRegistry(codecRegistry))

  val collection = database.flatMap(_.getCollection[Person]("test"))

  override def aspects: List[TestAspect[Nothing, TestEnvironment, Nothing, Any]] =
    List(TestAspect.executionStrategy(ExecutionStrategy.Sequential), TestAspect.timeout(Duration.fromMillis(30000)))

  override def spec: ZSpec[TestEnvironment, Any] = suite("DistinctSubscriptionSpec")(
    distinctDocuments(),
    distinctFirstDocuments(),
    filterDistinctDocuments(),
    close()
  )

  def distinctDocuments() = {
    val names = for {
      col <- collection
      _ <- col.insertMany(
        Seq(
          Person("John", 20),
          Person("Carmen", 40),
          Person("John", 15),
          Person("Yasmin", 30)
        )
      )
      doc <- col.distinct[String]("name").fetch
    } yield doc

    test("Get distinct Persons by name") {
      assertM(names)(equalTo(Seq("John", "Carmen", "Yasmin")))
    }
  }

  def distinctFirstDocuments() = {
    val names = for {
      col <- collection
      doc <- col.distinct[String]("name").first().fetch
    } yield doc

    test("Get first person Persons by name") {
      assertM(names)(equalTo("John"))
    }
  }

  def filterDistinctDocuments() = {
    val names = for {
      col <- collection
      doc <- col.distinct[String]("name").filter(Filters.gt("age", 30)).fetch
    } yield doc

    test("Get filtered persons with age greater than 30") {
      assertM(names)(equalTo(Seq("Carmen")))
    }
  }

  def close() = {
    test("Close database and clean") {
      val close =    for {
        col <- collection
        _ <- col.drop()
        _ <- mongoClient.pureClose()

      } yield ()
      assertM(close)(equalTo(()))
    }
  }

}
