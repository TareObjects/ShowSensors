name := """ShowSensors"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.11.1"

libraryDependencies ++= Seq(
  javaJdbc,
  javaEbean,
  cache,
  javaWs,
  "com.fasterxml.jackson.core" % "jackson-databind" % "2.4.4",
  "com.fasterxml.jackson.core" % "jackson-annotations" % "2.4.4"
)
