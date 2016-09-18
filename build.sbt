name := """play-getting-started"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  jdbc,
  "com.typesafe.play" %% "play-slick" % "2.0.0",
  "org.postgresql" % "postgresql" % "9.4-1201-jdbc41",
  "net.ruippeixotog" %% "scala-scraper" % "1.0.0"
  //"org.postgresql" % "postgresql" % "9.4-1201-jdbc41",
  //"com.typesafe.play" %% "play-slick" % "2.0.0"
)

libraryDependencies <+= scalaVersion("org.scala-lang" % "scala-compiler" % _ )



