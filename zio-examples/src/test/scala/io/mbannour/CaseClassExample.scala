package io.mbannour

import io.github.mbannour.MongoZioClient
import zio._
import org.mongodb.scala.bson.codecs.Macros._
import org.bson.codecs.configuration.CodecRegistries.{fromProviders, fromRegistries}
import org.mongodb.scala.MongoClient.DEFAULT_CODEC_REGISTRY
import org.mongodb.scala.bson.ObjectId
import org.mongodb.scala.model.Filters.equal
import org.mongodb.scala.model.Updates.set

object CaseClassExample extends zio.ZIOAppDefault {

  case class Person(_id: ObjectId, name: String, lastName: String, age: Int)

  val persons : Seq[Person] = Seq(
    Person(new ObjectId(), "Charles", "Babbage", 34),
    Person(new ObjectId(), "George", "Boole", 19),
    Person(new ObjectId(), "Gertrude", "Blanch", 74),
    Person(new ObjectId(), "Grace", "Hopper", 14),
    Person(new ObjectId(), "Ida", "Rhodes", 30),
    Person(new ObjectId(), "Jean", "Bartik", 22),
    Person(new ObjectId(), "John", "Backus", 56),
    Person(new ObjectId(), "Lucy", "Sanders", 51),
    Person(new ObjectId(), "Tim", "Berners Lee", 46),
    Person(new ObjectId(), "Zaphod", "Beeblebrox", 15)
  )

  val codecRegistry = fromRegistries(fromProviders(classOf[Person]), DEFAULT_CODEC_REGISTRY)
  val clientResource = MongoZioClient.autoCloseableClient("mongodb://localhost:27017")

  val app = ZIO.scoped {
    for {
      client <- clientResource
      database <- client.getDatabase("mydb").map(_.withCodecRegistry(codecRegistry))
      col <- database.getCollection[Person]("test")
      _ <- col.insertMany(persons)
      _ <- col.find().first().fetch
      _ <- col.find(equal("name", "Ida")).first().fetch
      _ <- col.updateOne(equal("name", "Jean"), set("lastName", "Bannour"))
      _ <- col.deleteOne(equal("name", "Zaphod"))
      count <- col.countDocuments()
      person <- col.find(equal("name", "Jean")).first().headOption
      _  <- ZIO.attempt(println(s"Persons count: $count"))
      _  <- ZIO.attempt(println( s"The updated person with name Jean is: $person"))
    } yield ()
  }

  override def run: URIO[Any, ExitCode] = app.exitCode

}
