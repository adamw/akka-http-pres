organization  := "com.softwaremill"

name := "akka-http-pres"

version := "0.1-SNAPSHOT"

scalaVersion := "2.11.8"

libraryDependencies ++= Seq(
  "com.softwaremill.akka-http-session" %% "core" % "0.2.7",
  "com.softwaremill.akka-http-session" %% "jwt"  % "0.2.7"
)
