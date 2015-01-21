name := "pipeline"

version := "1.0"

resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"

scalaVersion := "2.11.1"

libraryDependencies ++= Seq(
  "org.scalatest" % "scalatest_2.11" % "2.2.1" % "test",
  "com.typesafe" % "config" % "1.2.1",
  "com.typesafe.akka" %% "akka-actor" % "2.3.7",
  "com.typesafe.akka" %% "akka-testkit" % "2.3.7",
  "com.typesafe.akka" %% "akka-stream-experimental" % "1.0-M2",
  "com.sksamuel.elastic4s" %% "elastic4s" % "1.3.2",
  "net.sf.opencsv" % "opencsv" % "2.3",
  "com.typesafe.play" %% "play-json" % "2.3.6"
)