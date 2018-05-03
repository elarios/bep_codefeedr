package org.codefeedr.keymanager.redis

import java.net.URI
import java.util.Date

import com.redis._
import org.codefeedr.keymanager.KeyManager

class RedisKeyManager(host: String, root: String = "codefeedr:keymanager") extends KeyManager {
  private var connection: RedisClient = _
  private var requestScriptId: String = _

  connect()

  /**
    * Start a new connection to Redis and configure it properly.
    *
    * @throws RuntimeException
    */
  private def connect(): Unit = {
    val uri = new URI(host)
    connection = new RedisClient(uri)

    val sha = connection.scriptLoad(getRequestLuaScript)
    if (sha.isDefined)
      requestScriptId = sha.get
  }

  /**
    * Custom script for getting a key from the KV-store.
    *
    * @return script
    */
  private def getRequestLuaScript: String = {
    val stream = getClass.getResourceAsStream("/redis_request.lua")

    scala.io.Source.fromInputStream(stream).mkString
  }

  /**
    * Get whether there is an active connection
    * @return has active connection
    */
  private def isConnected: Boolean = connection != null

  /**
    * Get the Redis key for given target.
    * @param target Target of the key pool
    * @return Target key
    */
  private def redisKeyForTarget(target: String): String = root + ":" + target

  override def request(target: String, numberOfCalls: Int): Option[String] = {
    import serialization.Parse.Implicits.parseString

    val targetKey = redisKeyForTarget(target)
    val time = new Date().getTime

    // Run the custom script for a fully atomic get+decr operation
    val result: Option[List[Option[String]]] = connection.evalMultiSHA(requestScriptId, List(targetKey), List(numberOfCalls, time))

    if (result.isEmpty)
      return None

    val data = result.get
    if (data.isEmpty)
      None
    else
      data.head
  }

  ///////// Methods for testing

  private[redis] def disconnect(): Unit = {
    connection.disconnect
    connection = null
  }

  /**
    * Add a new key to redis for testing.
    *
    * @param target Key target
    * @param key Key
    * @param numCalls Number of calls allowed within interval
    * @param interval Interval in milliseconds
    */
  private[redis] def set(target: String, key: String, numCalls: Int, interval: Int): Unit = {
    val targetKey = redisKeyForTarget(target)
    val time = new Date().getTime + interval

    connection.zadd(targetKey + ":keys", numCalls, key)
    connection.zadd(targetKey + ":refreshTime", time, key)
    connection.hset(targetKey + ":limit", key, numCalls)
    connection.hset(targetKey + ":interval", key, interval)
  }

  private[redis] def get(target: String, key: String): Option[Int] = {
    if (!isConnected)
      connect()

    val targetKey = redisKeyForTarget(target)
    val result = connection.zscore(targetKey + ":keys", key)

    if (result.isEmpty)
      None
    else
      Some(result.get.toInt)
  }

  private[redis] def delete(target: String, key: String): Unit = {
    val targetKey = redisKeyForTarget(target)

    connection.zrem(targetKey + ":keys", key)
    connection.zrem(targetKey + ":refreshTime", key)
    connection.hdel(targetKey + ":limit", key)
    connection.hdel(targetKey + ":interval", key)
  }

  private[redis] def deleteAll(): Unit = {
    connection.evalBulk("for _,k in ipairs(redis.call('keys',ARGV[1])) do redis.call('del',k) end", List(), List(root + ":*"))
  }

}