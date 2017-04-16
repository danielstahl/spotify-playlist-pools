name := "spotify-playlist-pools"

organization := "net.soundmining"

version := "1.0-SNAPSHOT"

scalaVersion := "2.11.8"

testOptions in Test := Seq(Tests.Filter(s => s.endsWith("Spec")))

libraryDependencies += "se.michaelthelin.spotify" % "spotify-web-api-java" % "1.5.0"

libraryDependencies += "org.scalatest" % "scalatest_2.11" % "2.2.0" % "test"

libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.1.2"

libraryDependencies +=  "org.scalaj" %% "scalaj-http" % "2.3.0"

