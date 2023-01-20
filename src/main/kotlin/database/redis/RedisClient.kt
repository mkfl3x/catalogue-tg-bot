package database.redis

import redis.clients.jedis.JedisPool
import redis.clients.jedis.JedisPoolConfig
import utils.Properties


object RedisClient {

    private val connectionPool = JedisPool(
        JedisPoolConfig(),
        Properties.get("redis.host"),
        Properties.get("redis.port").toInt(),
        true
    )

    init {
        connectionPool.maxTotal = 20
    }

    fun getTop(key: String): String? {
        getConnection().use { connection ->
            return connection.lindex(key, 0)
        }
    }

    fun push(key: String, vararg values: String, first: Boolean = true): Long {
        getConnection().use { connection ->
            return if (first)
                connection.lpush(key, *values)
            else
                connection.rpush(key, *values)
        }
    }

    fun pop(key: String, count: Int = 1, first: Boolean = true): MutableList<String>? {
        getConnection().use { connection ->
            return if (first)
                connection.lpop(key, count)
            else
                connection.rpop(key, count)
        }
    }

    fun deleteFromLists(value: String, keyPattern: String = "*") {
        getConnection().use { connection ->
            return connection.keys(keyPattern).forEach { connection.lrem(it, -1000000, value) }
        }
    }

    fun deleteList(key: String): Long {
        getConnection().use { connection ->
            return connection.del(key)
        }
    }

    private fun getConnection() =
        connectionPool.resource.apply { this.auth(Properties.get("redis.user"), Properties.get("redis.password")) }
}