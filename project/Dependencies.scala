import sbt._

object Dependencies {

  lazy val mongoVersion     = "4.9.0"
  lazy val zioVersion       = "2.0.10"
  lazy val scalaTestVersion = "3.2.8"
  lazy val scalaMockVersion = "4.3.0"
  lazy val logbackVersion   = "1.1.3"

  lazy val mongoScala = "org.mongodb.scala" %% "mongo-scala-driver" % mongoVersion
  lazy val zio        = "dev.zio"           %% "zio"                % zioVersion
  lazy val zioStreams = "dev.zio"           %% "zio-streams"        % zioVersion
  lazy val logback    = "ch.qos.logback"     % "logback-classic"    % logbackVersion % "test"
  lazy val slf4j       = "org.slf4j"         % "slf4j-simple"       % "1.7.32"

  //Test
  lazy val scalaTest       = "org.scalatest" %% "scalatest"                   % scalaTestVersion
  lazy val zioTest         = "dev.zio"       %% "zio-test"                    % zioVersion
  lazy val zioTestSbt      = "dev.zio"       %% "zio-test-sbt"                % zioVersion
  lazy val zioMagnoliaTest = "dev.zio"       %% "zio-test-magnolia"           % zioVersion
  lazy val zioIntStream    = "dev.zio"       %% "zio-interop-reactivestreams" % "2.0.1"

}
