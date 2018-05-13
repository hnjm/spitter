name := "Spitter"

version := "0.6.9"

scalaVersion := "2.12.6"

lazy val akkaVersion = "2.5.12"
lazy val akkaHTTPVersion = "10.1.1"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-stream" % akkaVersion,
  "com.typesafe.akka" %% "akka-http" % akkaHTTPVersion,
  "commons-codec" % "commons-codec" % "1.11"
)
