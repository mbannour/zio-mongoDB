package io.github.mbannour

import com.mongodb.{ConnectionString, MongoClientSettings}
import zio._

object MongoTestClient {

  lazy val urlConfig: MongoClientSettings =
    MongoClientSettings.builder().applyConnectionString(new ConnectionString("mongodb://localhost:27017")).build()

  def mongoTestClient(): Task[MongoZioClient] = MongoZioClient(urlConfig)
}
