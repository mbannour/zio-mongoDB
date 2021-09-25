import sbt._

object Dependencies {

  lazy val mongoVersion = "4.3.1"
  lazy val zioVersion = "2.0.0-M2"
  lazy val scalaTestVersion = "3.2.8"
  lazy val scalaMongoVersion = "2.9.0"

  lazy val mongoScala    = "org.mongodb.scala" %% "mongo-scala-driver" % scalaMongoVersion
  lazy val mongodbDriverStreams = "org.mongodb" % "mongodb-driver-reactivestreams" % mongoVersion
  lazy val zio = "dev.zio" %% "zio" % zioVersion
  lazy val zioStreams = "dev.zio" %% "zio-streams" % zioVersion

  //Test
  lazy val scalaTest = "org.scalatest" %% "scalatest" % scalaTestVersion

}
