package bot.context

import database.redis.RedisClient
import org.bson.types.ObjectId
import utils.Properties

object KeyboardStates {

    private val prefix: String = Properties.get("redis.prefix")
    
    fun pushKeyboard(chatId: Long, keyboardId: ObjectId) = RedisClient.push("$prefix$chatId", keyboardId.toHexString())

    fun popKeyboard(chatId: Long) = RedisClient.pop("$prefix$chatId")

    fun getCurrentKeyboard(chatId: Long) = RedisClient.getTop("$prefix$chatId")

    fun dropState(chatId: Long) = RedisClient.deleteList("$prefix$chatId")

    fun deleteKeyboard(keyboardId: String) = RedisClient.deleteFromLists(keyboardId)
}