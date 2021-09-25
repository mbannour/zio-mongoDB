package io.github

import org.mongodb.scala.bson

package object mbannour {

  type JavaMongoDatabase = com.mongodb.reactivestreams.client.MongoDatabase

  type Document = bson.Document

}
