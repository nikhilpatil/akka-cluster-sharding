organization := "com.typesafe.akka.samples"
name := "akka-sample-persistence-scala"

scalaVersion := "2.12.8"
val akkaVersion = "2.5.22"

libraryDependencies ++= Seq(
  "com.typesafe.akka"           %% "akka-persistence" % akkaVersion,
  "com.typesafe.akka"           %% "akka-cluster" % akkaVersion,
  "com.typesafe.akka"           %% "akka-cluster-sharding" % akkaVersion,
  "com.typesafe.akka"           %% "akka-cluster-tools" % akkaVersion,
  "com.typesafe.akka"           %% "akka-persistence-cassandra" % "0.98",
  "org.iq80.leveldb"            % "leveldb"          % "0.7",
  "org.fusesource.leveldbjni"   % "leveldbjni-all"   % "1.8")