package database.redis

import redis.clients.jedis.Jedis
import redis.clients.jedis.exceptions.JedisConnectionException
import utils.Properties


object RedisClient {

    private val client = Jedis(Properties.get("redis.host"), Properties.get("redis.port").toInt(), true)

    init {
        client.auth(Properties.get("redis.user"), Properties.get("redis.password"))
    }

    fun getTop(key: String) = client.lindex(key, 0)

    fun push(key: String, vararg values: String, first: Boolean = true) =
        if (first)
            client.lpush(key, *values)
        else
            client.rpush(key, *values)

    fun pop(key: String, count: Int = 1, first: Boolean = true) =
        if (first)
            client.lpop(key, count)
        else
            client.rpop(key, count)

    fun deleteFromLists(value: String, keyPattern: String = "*") =
        client.keys(keyPattern).forEach { client.lrem(it, -1000000, value) }

    fun deleteList(key: String) = client.del(key)
}