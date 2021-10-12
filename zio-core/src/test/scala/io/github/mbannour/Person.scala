package io.github.mbannour

import org.mongodb.scala.bson.ObjectId

object Person {
  def apply(name: String, age: Int): Person = Person(new ObjectId(), name, age)
}

case class Person(_id: ObjectId, name: String, age: Int)