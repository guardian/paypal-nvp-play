import Dependencies._

name := "Paypal NVP Scala"
version := "1.0"
scalaVersion := "2.11.8"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

libraryDependencies += okHttp3
libraryDependencies += scalaUri
