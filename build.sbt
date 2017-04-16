name := "spotify-playlist-pools"

organization := "net.soundmining"

version := "1.0-SNAPSHOT"

scalaVersion := "2.12.1"

testOptions in Test := Seq(Tests.Filter(s => s.endsWith("Spec")))

libraryDependencies += "se.michaelthelin.spotify" % "spotify-web-api-java" % "1.5.0"

libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.1" % "test"

libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.1.2"

libraryDependencies += "org.rogach" %% "scallop" % "2.1.1"

assemblyMergeStrategy in assembly := {
  case PathList("META-INF", xs @ _*) => MergeStrategy.discard
  case x => MergeStrategy.first
}

