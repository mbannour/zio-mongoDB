import Dependencies._

ThisBuild / scalaVersion     := "2.13.6"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "io.github.mbannour"
ThisBuild / organizationName := "mbannour"

lazy val root = (project in file("."))
  .settings(
    name := "ziomongo",
    scalacOptions ++= Seq(
      "-Ymacro-annotations",
      "-encoding", "utf-8",
      "-deprecation",
      "-explaintypes",
      "-unchecked",
      "-feature",
      "-language:existentials",
      "-language:higherKinds",
      "-language:implicitConversions",
      "-Xcheckinit",
    ),
      libraryDependencies ++= Seq(
        mongoScala,
      mongodbDriverStreams,
      zio,
      zioStreams,
      scalaTest % Test
    )
  )

