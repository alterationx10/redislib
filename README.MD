This is an example project of how to set up a library for quick work 
with Redis servers.

Modify it up for servers you currently work with, use sbt publishLocal,
and then you can add

"com.turdwaffle" %% "redislib" % "1.0"

(insert correct version)

as a dependency in your (local sbt) projects build.sbt file and start 
hget-ing!.

# What's the point of this?

A wonderful thing about Scala, is that you can use any of the wonderful Java libraries out there _at no additional cost_. In Java, I've used the [Jedis](https://github.com/xetorthio/jedis) library quite a lot. A lot of back end infrastruce I use is built on Redis, and I like to have some CLI apps that can hit the backend in a similar fashion to how the production code is. To that extent, this project is meant to be a small example of porting over your own wrapper/code to a scala libary that you can use as a versioned/managed dependency for production and CLI code. The basic sctructure can lend itself to a Database library as well.

## Dependencies
This project only has 2 dependencies:
1. Jedis
2. Guice

### The Code

The core of the library resides in a single file: [RedisProvider.scala](https://github.com/alterationx10/redislib/blob/master/src/main/scala/com/turdwaffle/redis/RedisProvider.scala)

This file contains 4 main components:
1. The **RedisPool** trait. This will set up some basic connection pooling parameters.
2. A **RedisHost** class that extends **RedisPool**. This will allow us to set a hostname, and a password if any, for our connection (2 examples provided).
3. A **RedisProvider** class extends guices **AbstractModule**. This will bind a named instance of our **RedisHost** that we can access.
4. A **RedisProvider** object. We use this to access our guice injector and get isntances of our **RedisHosts**s

The code is heavily commented, so please check there for how the parts work.

The file [Main.scala](https://github.com/alterationx10/redislib/blob/master/src/main/scala/Main.scala) shows a quick usage of the implementation.

From there, you have a good skeleton to insert you own often-used code!
