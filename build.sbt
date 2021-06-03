name := """sklep_play"""
organization := "com.wladyga"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.12.3"

libraryDependencies += guice
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "5.0.0" % Test
libraryDependencies += "com.typesafe.play" %% "play-slick" % "4.0.0"
libraryDependencies += "com.typesafe.play" %% "play-slick-evolutions" % "4.0.0"
libraryDependencies += "org.xerial" % "sqlite-jdbc" % "3.30.1"
libraryDependencies ++= Seq(
  "com.iheart" %% "ficus" % "1.4.7",
  "com.mohiva" %% "play-silhouette" % "7.0.0",
  "com.mohiva" %% "play-silhouette-password-bcrypt" % "7.0.0",
  "com.mohiva" %% "play-silhouette-persistence" % "7.0.0",
  "com.mohiva" %% "play-silhouette-crypto-jca" % "7.0.0",
  "com.mohiva" %% "play-silhouette-totp" % "7.0.0",
  "net.codingwell" %% "scala-guice" % "5.0.1"
)
libraryDependencies += ehcache
//libraryDependencies += "com.github.karelcemus" %% "play-redis" % "2.0.1"

resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"
resolvers += "Akka Snapshot Repository" at "https://repo.akka.io/snapshots/"
resolvers += "Atlassian's Maven Public Repository" at "https://packages.atlassian.com/maven-public/"