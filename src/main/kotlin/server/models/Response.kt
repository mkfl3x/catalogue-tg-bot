package server.models

import com.google.gson.JsonObject
import org.bson.types.ObjectId

data class Response(
    val status: ResponseStatus,
    val id: ObjectId,
    val message: String = ""
) {
    fun toJson() = JsonObject().apply {
        addProperty("status", status.text)
        addProperty("id", id.toHexString())
        if (message.isNotEmpty())
            addProperty("message", message)
    }
}

enum class ResponseStatus(val text: String) {
    SUCCESS("success"),
    FAILED("failed")
}