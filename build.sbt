import Dependencies._
import sbt.{Test, ThisBuild}

lazy val scala212 = "2.12.14"
lazy val scala213 = "2.13.6"
lazy val supportedScalaVersions = List(scala212, scala213)

ThisBuild / scalaVersion     := "2.13.6"
ThisBuild / version          := "0.0.2"
ThisBuild / organization     := "io.github.mbannour"
ThisBuild / organizationName := "mbannour"
ThisBuild / description      := "ZIO wrapper for MongoDB Reactive Streams Java Driver"
ThisBuild / scmInfo          := Some(ScmInfo(url("https://github.com/mbannour/zio-mongo"), "https://github.com/mbannour/zio-mongo.git"))
ThisBuild / developers       := List(Developer("", "medali", "med.ali.bennour@gmail.com", url("https://github.com/mbannour")))
ThisBuild / licenses         := List("Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0"))
ThisBuild / homepage         := Some(url("https://github.com/mbannour/zio-mongo"))
ThisBuild / versionScheme    := Some("early-semver")

lazy val scalaOptions =
  Seq(
  "-encoding", "utf-8",
  "-deprecation",
  "-explaintypes",
  "-unchecked",
  "-feature",
  "-language:existentials",
  "-language:higherKinds",
  "-language:implicitConversions",
  "-Xcheckinit"
  )

lazy val ziomongo = (project in file("."))
  .settings(
    crossScalaVersions := Nil,
    publish / skip := true
  )
  .aggregate(zioCore, examples)



lazy val zioCore = (project in file("zio-core"))
  .settings(
    name := "ziomongo",
    ThisBuild / publishTo := {
      val nexus = "https://s01.oss.sonatype.org/"
      if (isSnapshot.value) Some("snapshots" at nexus + "content/repositories/snapshots")
      else Some("releases" at nexus + "service/local/staging/deploy/maven2")
    },
    ThisBuild / publishMavenStyle := true,
    scalacOptions ++= scalaOptions,
    crossScalaVersions := supportedScalaVersions,
    credentials += Credentials(Path.userHome / ".sbt" / "sonatype_credentials"),
    libraryDependencies ++= Seq(
        mongoScala,
        mongodbDriverStreams,
        logback,
        zio,
        zioStreams,
        zioMagnoliaTest % Test,
        zioTestSbt % Test,
        zioTest % Test,
        scalaTest % Test
      ),
    testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework"),
    Test / testOptions  ++= Seq(Tests.Setup(() => MongoEmbedded.start), Tests.Cleanup(() => MongoEmbedded.stop)),
    Test / parallelExecution := false
  )

lazy val examples = (project in file("zio-examples"))
  .settings(
    crossScalaVersions := Nil,
    publish / skip := true
  )
  .dependsOn(zioCore)


