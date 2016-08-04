name := "redislib"

organization:= "com.turdwaffle"

version := "1.0-SNAPSHOT"

scalaVersion := "2.11.8"

libraryDependencies ++= Seq(
  "com.google.inject" % "guice" % "4.0",
  "redis.clients" % "jedis" % "2.8.1"
)
