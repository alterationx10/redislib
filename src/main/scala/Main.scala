import com.turdwaffle.redis.RedisProvider
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.util.{Failure, Success, Try}

/**
  * A simple demonstration of how to start using the library
  */
object Main extends App {

  // Let's make a redis hash key so we don't mis-type things
  val redisKey: String = "com:turdwaffle:test"

  // We can use our helper object to set up the connection pool, and get a reference
  val localRedis = RedisProvider.getRedisLocalHost

  // Now we can get a jedis object, and start reading/writing values
  val jedis = localRedis.jedisPool.getResource
  jedis.hset(redisKey, "enabled", "1")
  jedis.hset(redisKey, "secret", "bacon")
  jedis.hset(redisKey, "zero", "0")
  // Don't forget to close resources when you're done
  jedis.close()

  // Because we wrote a sweet redisAction function,
  // we can do this without the open/close parts:

  def enabledStatus: Future[Boolean] = localRedis.redisAction { client =>
    client.hget(redisKey, "enabled") match {
      case "1" => true
      case _ => false
    }
  }

  // Let's see what we got

  Await.result(enabledStatus, 5 seconds)

  enabledStatus.map{
    case true => println("We are enabled!")
    case false => println("We are disabled!")
  }

  // If we have a risky operation, we can use our redisTryAction
  def uhOh: Future[Try[Int]] = localRedis.redisTryAction { client =>
    client.hget(redisKey, "secret").toInt
  }

  // Let's see what we got
  Await.result(uhOh, 5 seconds)

  uhOh.map {
    case Success(int) => println(s"We got an int: $int")
    case Failure(e) => println(s"UhOh! We failed: ${e.toString}")
  }

  // redisActions/redisTryActions can also useful for composition
  val multiTask = for {
    one <- localRedis.redisAction(client => client.hget(redisKey, "enabled").toInt)
    zero <- localRedis.redisTryAction(client => client.hget(redisKey, "zero").toInt)
    password <- localRedis.redisAction(client => client.hget(redisKey, "secret"))
  } yield {
    if ("bacon".equals(password)) {
      zero.map(z => z / one)
    } else {
      Failure(new Exception("Missing Bacon Exception"))
    }
  }

  Await.result(multiTask, 5 seconds)

  multiTask.map {
    case Success(int) => println(s"We got an int: $int")
    case Failure(e) => println(s"UhOh! We failed: ${e.toString}")
  }

  // Because we have used a shutdown hook, our redis pool will cleanly close itself

}
