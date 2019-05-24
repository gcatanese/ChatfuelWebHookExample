organization := "com.kata"
name := "ChatfuelWebHookExample"
version := "0.1-SNAPSHOT"

scalaVersion := "2.12.8"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.5.19",
  "com.typesafe.akka" %% "akka-stream" % "2.5.19",
  "com.typesafe.akka" %% "akka-http" % "10.1.8",
  "com.typesafe.akka" %% "akka-http-spray-json" % "10.1.8",
  "org.scalatest" %% "scalatest" % "3.0.5" % "test",
  "com.typesafe.akka" %% "akka-stream-testkit" % "2.5.19" % "test",
  "com.typesafe.akka" %% "akka-http-testkit" % "10.1.8" % "test",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2",
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "com.kata" %% "chatfuelwebhook" % "0.1-SNAPSHOT"
)

enablePlugins(JavaAppPackaging)



