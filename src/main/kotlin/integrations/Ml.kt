package integrations

import com.google.gson.annotations.SerializedName
import io.ktor.http.*
import org.slf4j.LoggerFactory
import utils.GsonMapper
import utils.Properties

data class MlResponse(
    @SerializedName("unixtime") val unixTime: Long,
    @SerializedName("request_id") val requestId: String,
    @SerializedName("intent") val intent: String,
    @SerializedName("perk") val perk: String,
    @SerializedName("message") val message: String,
    @SerializedName("img") val img: String?, // TODO: handle it when null (to be link string)
    @SerializedName("execution_time_s") val executionTime: Double,
    @SerializedName("model") val model: String,
    @SerializedName("experiment") val experiment: String
)

object Ml {

    private val logger = LoggerFactory.getLogger(this.javaClass.name)

    fun getAnswer(questionMessage: String, requestType: String, chatId: Long): String {
        return when (requestType) {
            "text" -> question(requestType, "text", questionMessage, chatId)
            "voice" -> question(requestType, "ogg_url", questionMessage, chatId)
            else -> throw Exception("Unknown request type: \"$requestType\"")
        }
    }

    private fun question(type: String, payloadType: String, questionMessage: String, chatId: Long): String {
        val response = khttp.post(
            url = Properties.get("integrations.ml"),
            headers = mapOf("Content-Type" to "application/json"),
            data = "{\"request_type\": \"$type\", \"$payloadType\": \"$questionMessage\", \"t_uid\": \"$chatId\"}"
        )
        return if (response.statusCode == HttpStatusCode.OK.value) {
            try {
                val responseMessage = GsonMapper.deserialize(response.text, MlResponse::class.java).message
                if (responseMessage.isNullOrEmpty()) response.text else responseMessage // TODO: while debugging
            } catch (e: Exception) {
                logger.error(e.message)
                "Что-то пошло не так \uD83E\uDD72" // TODO: move to constant
            }
        } else {
            logger.error("response_code from Ml is ${response.statusCode}")
            "Что-то пошло не так \uD83E\uDD72" // TODO: move to constant
        }
    }
}