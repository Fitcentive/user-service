name := "user"

version := "1.0"

lazy val `notification` = (project in file("."))
  .enablePlugins(PlayScala)
  .disablePlugins(PlayLogback)

resolvers += "Akka Snapshot Repository" at "https://repo.akka.io/snapshots/"

scalaVersion := "2.13.5"

libraryDependencies ++= Seq(
  jdbc,
  ehcache,
  ws,
  evolutions,
  // Anorm
  "org.playframework.anorm" %% "anorm"          % "2.6.10",
  "org.playframework.anorm" %% "anorm-postgres" % "2.6.10",
  //Cats
  "org.typelevel" %% "cats-core"   % "2.7.0",
  "org.typelevel" %% "cats-effect" % "3.3.4",
  // App sdk
  "io.fitcentive" %% "app-sdk"          % "1.0.0",
  "io.fitcentive" %% "message-registry" % "1.0.0",
  // SMTP
  "javax.mail"   % "javax.mail-api" % "1.6.2",
  "com.sun.mail" % "javax.mail"     % "1.6.2",
  //Logging
  "ch.qos.logback"       % "logback-classic"          % "1.3.0-alpha10",
  "net.logstash.logback" % "logstash-logback-encoder" % "7.0.1",
  // Image support
  "org.apache.xmlgraphics" % "batik-transcoder" % "1.14",
  "org.apache.xmlgraphics" % "batik-codec"      % "1.14",
  specs2                   % Test,
  guice
)

dependencyOverrides ++= Seq(
  "com.fasterxml.jackson.core" % "jackson-core"        % "2.11.4",
  "com.fasterxml.jackson.core" % "jackson-annotations" % "2.11.4",
  "com.fasterxml.jackson.core" % "jackson-databind"    % "2.11.4",
)

Universal / javaOptions ++= Seq("-Dpidfile.path=/dev/null")
