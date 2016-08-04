package com.turdwaffle.redis

import com.google.inject.name.Names
import com.google.inject.{AbstractModule, Guice, Key, Singleton}
import redis.clients.jedis.{Jedis, JedisPool, JedisPoolConfig}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Future, Promise}
import scala.util.Try

/**
  * Trait containing the default connection pool settings we will use for
  * redis instances.
  */
trait RedisPool {

  /**
    * IP/hostname of the redis server
    */
  val hostName: String

  /**
    * Password for the redis server.
    */
  val password: Option[String]

  /**
    * The redis connection pool to get resources from.
    * Lazily instantiated.
    * This is cleanly closed on JVM shutdown via sys.addShutdownHook
    */
  lazy val jedisPool: JedisPool = password match {
    case Some(p) => new JedisPool(jedisPoolConfig, hostName, port, timeOut, p)
    case None => new JedisPool(jedisPoolConfig, hostName, port, timeOut)
  }
  /**
    * The port redis is running on. 6379 unless overridden
    */
  val port: Int = 6379

  /**
    * The redis timeout. 2000 unless overridden
    */
  val timeOut: Int = 2000

  /**
    * The redis connection pool. Settings are:
    *   25 minimum connections in the pool.
    *   250 max connections in the pool.
    *   Unlimited connections on burst, if needed.
    *   Test connection on return to the pool.
    *   Test connections while idle.
    *   Evict members of the connection pool that have been idle for 1 hour.
    */
  val jedisPoolConfig: JedisPoolConfig = new JedisPoolConfig()
  jedisPoolConfig.setMinIdle(25)
  jedisPoolConfig.setMaxIdle(250)
  jedisPoolConfig.setMaxTotal(-1)
  jedisPoolConfig.setTestOnReturn(true)
  jedisPoolConfig.setTestWhileIdle(true)
  jedisPoolConfig.setSoftMinEvictableIdleTimeMillis(3600000)


  /**
    * Create a way to reduce the boilerplate of calling redis actions
    * i.e.
    * def quick: Future[String] = redisAction { client =>
    *   client.hget(key, value)
    * }
    * Note the future is always successful.
    * @param thunk
    * @tparam T
    * @return
    */
  def redisAction[T](thunk: Jedis => T): Future[T] = {
    val p = Promise[T]()
    Future {
      val jedis = jedisPool.getResource
      val result = thunk(jedis)
      try {
        jedis.close()
      } finally {
        p.success(result)
      }
    }
    p.future
  }

  /**
    * Same as redisAction, but wrapping in a Try()
    * Note the future is always successful.
    * @param thunk
    * @tparam T
    * @return
    */
  def redisTryAction[T](thunk: Jedis => T): Future[Try[T]] = {
    val p = Promise[Try[T]]()
    Future {
      val jedis = jedisPool.getResource
      val result = Try(thunk(jedis))
      try {
        jedis.close()
      } finally {
        p.success(result)
      }
    }
    p.future
  }

  /**
    * Add a shutdown hook to close the jedisPool.
    */
  sys.addShutdownHook {
    jedisPool.close()
  }

}

/**
  * A connection to a redis server on localhost
  */
@Singleton
class RedisLocalHost extends RedisPool {
  override val password: Option[String] = None
  override val hostName: String = "localhost"
}

/**
  * A connection to a redis server on 127.0.0.1
  * This is obviously the same as localhost, but just done as a 'for example'
  * of how to add another IP/host
  */
@Singleton
class Redis127 extends RedisPool {
  override val hostName: String = "127.0.0.1"
  override val password: Option[String] = None
}

/**
  * Guice module to configure our various redis configurations
  */
class RedisProvider extends AbstractModule {
  override def configure(): Unit = {

    // Bind an instance to localhost
    bind(classOf[RedisPool])
      .annotatedWith(Names.named("localhost"))
      .to(classOf[RedisLocalHost])

    // Bind an instance to 127.0.0.1
    bind(classOf[RedisPool])
      .annotatedWith(Names.named("127"))
      .to(classOf[Redis127])

  }
}

/**
  * Helper object to retrieve instances of our various redis configurations
  */
object RedisProvider {

  /**
    * Guice injector for RedisProvider
    */
  lazy val redisInjector = Guice.createInjector(new RedisProvider)

  /**
    * Connection pool to localhost
    * @return
    */
  def getRedisLocalHost: RedisLocalHost = redisInjector.getInstance(
    Key.get(classOf[RedisPool], Names.named("localhost"))
  ).asInstanceOf[RedisLocalHost]

  /**
    * Connection pool to 127.0.0.1
    * @return
    */
  def getRedis127: Redis127 = redisInjector.getInstance(
    Key.get(classOf[RedisPool], Names.named("127"))
  ).asInstanceOf[Redis127]


}