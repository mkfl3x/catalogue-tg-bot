package server.models

import com.google.gson.JsonObject
import org.bson.types.ObjectId

data class Response(
    val status: ResponseStatus,
    val action: RequestAction,
    val objectId: ObjectId,
    val message: String = ""
) {
    fun toJson() = JsonObject().apply {
        addProperty("status", status.text)
        addProperty("action", action.text)
        addProperty("object_id", objectId.toHexString())
        if (message.isNotEmpty())
            addProperty("message", message)
    }
}

enum class ResponseStatus(val text: String) {
    SUCCESS("success"),
    FAILED("failed")
}

enum class RequestAction(val text: String) {
    CREATE("create"),
    DELETE("delete"),
    LINK("link"),
    DETACH("detach")
}