package io.mbannour

import com.mongodb.reactivestreams.client.MongoClients
import io.github.mbannour.reactivestreams.ZMongoSource
import zio._
import org.mongodb.scala.bson.codecs.Macros._
import org.bson.codecs.configuration.CodecRegistries.{ fromProviders, fromRegistries }
import org.mongodb.scala.MongoClient.DEFAULT_CODEC_REGISTRY
import org.mongodb.scala.bson.ObjectId
import org.mongodb.scala.model.Filters.equal
import org.mongodb.scala.model.Updates.set
import zio.stream.{ ZSink, ZStream }

object ZIOSourceStreamExample extends zio.ZIOAppDefault {

  case class Person(_id: ObjectId, name: String, lastName: String, age: Int)

  val persons: Seq[Person] = Seq(
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

  val db = ZIO.fromAutoCloseable(ZIO.attempt(MongoClients.create("mongodb://localhost:27017"))).map { client =>
    client.getDatabase("mydb").withCodecRegistry(codecRegistry)
  }

  val zioCollection = ZStream.fromZIO(db.map(database => database.getCollection("test", classOf[Person])))

  //The bufferSize used as internal buffer. If possible, set to a power of 2 value for best performance.
  val bufferSize = 16

  val app = ZIO.scoped {
    (for {
      col         <- zioCollection
      _           <- ZMongoSource.insertMany(col, persons)
      firstPerson <- ZMongoSource(col.find().first(), bufferSize = 16)
      _           <- ZStream.fromZIO(ZIO.attempt(println(s"First saved person: $firstPerson")))
      _           <- ZMongoSource.default(col.updateOne(equal("name", "Jean"), set("lastName", "Bannour")))
      _           <- ZMongoSource.deleteOne(col, equal("name", "Zaphod"))
      count       <- ZMongoSource(col.countDocuments(), bufferSize = 16)
      person      <- ZMongoSource.default(col.find(equal("name", "Jean")).first())
      _           <- ZStream.fromZIO(ZIO.attempt(println(s"Persons count: $count")))
      _           <- ZStream.fromZIO(ZIO.attempt(println(s"The updated person with name Jean is: $person")))
    } yield ()).run(ZSink.head)
  }

  override def run: URIO[Any, ExitCode] = app.exitCode

}
