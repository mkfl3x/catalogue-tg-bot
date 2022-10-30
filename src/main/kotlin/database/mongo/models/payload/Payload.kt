package database.mongo.models

import com.google.gson.JsonObject
import com.pengrad.telegrambot.request.*
import database.mongo.models.payload.messages.Image
import database.mongo.models.payload.messages.Location
import database.mongo.models.payload.messages.Text
import org.bson.codecs.pojo.annotations.BsonCreator
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.codecs.pojo.annotations.BsonProperty
import org.bson.types.ObjectId
import utils.GsonMapper

data class Payload @BsonCreator constructor(

    @BsonId
    val id: ObjectId,

    @param:BsonProperty("name")
    @field:BsonProperty("name")
    val name: String,

    @param:BsonProperty("type")
    @field:BsonProperty("type")
    val type: String, // TODO: delete

    @param:BsonProperty("data")
    @field:BsonProperty("data")
    val data: String

) : MongoEntity {

    override fun toJson() = JsonObject().apply {
        addProperty("id", id.toHexString())
        addProperty("name", name)
        addProperty("type", type)
        addProperty("data", data)
    }

    fun getMessage(forChatId: Long): AbstractSendRequest<*> {
        GsonMapper.parse(data).get("message").apply {
            return when (this.asJsonObject.get("type").asString) {
                "text" -> generateTextMessage(forChatId, GsonMapper.deserialize(this.toString(), Text::class.java))
                "image" -> generateImagesMessage(forChatId, GsonMapper.deserialize(this.toString(), Image::class.java))
                "location" -> generateLocationMessage(forChatId, GsonMapper.deserialize(this.toString(), Location::class.java))
                else -> throw Exception("Unknown payload message type")
            }
        }
    }

    private fun generateTextMessage(chatId: Long, text: Text): AbstractSendRequest<*> {
        return SendMessage(chatId, text.text).apply {
            text.inlineKeyboard?.let { this.replyMarkup(it.toMarkup()) }
        }
    }

    private fun generateImagesMessage(chatId: Long, image: Image): AbstractSendRequest<*> {
        return SendPhoto(chatId, downloadImage(image.link)).apply {
            image.caption?.let { this.caption(it) }
            image.inlineKeyboard?.let { this.replyMarkup(it.toMarkup()) }
        }
    }

    private fun generateLocationMessage(chatId: Long, location: Location): AbstractSendRequest<*> {
        return SendLocation(chatId, location.latitude, location.longitude).apply {
            location.inlineKeyboard?.let { this.replyMarkup(it.toMarkup()) }
        }
    }

    private fun downloadImage(link: String): ByteArray = khttp.get(link).content
}