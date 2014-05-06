import sbt._
import Keys._

object ApplicationBuild extends Build {
  lazy val projectId = "evently-project"

  lazy val defaultSettings = Defaults.defaultSettings ++ Seq(
    version := "1.0",
    scalaVersion := "2.10.4",
    // scalac -help
    // scalac -X
    scalacOptions := Seq(
      "-deprecation",
      "-encoding", "UTF8",
      "–explaintypes",
      "-feature",
      "–optimise",
      "-unchecked",
      "–Xcheck-null",
      "–Xcheckinit",
      "–Xlog-implicits",
      "–Xlint"
    ),
    organization := "me.moschops"
  )

  val dependencies = Seq(
    "net.databinder.dispatch" %% "dispatch-core" % "0.11.0",
    "net.liftweb" %% "lift-json" % "2.5.1",
    "com.typesafe" % "config" % "1.0.2"
  )

  lazy val root = Project(id = projectId,
    base = file(".")).settings(libraryDependencies ++= dependencies)
}
