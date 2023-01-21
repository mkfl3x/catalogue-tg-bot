package server.models

import com.google.gson.JsonObject

data class Response(
    val status: ResponseStatus,
    val id: String,
    val message: String = ""
) {
    fun toJson() = JsonObject().apply {
        addProperty("status", status.text)
        addProperty("id", id)
        if (message.isNotEmpty())
            addProperty("message", message)
    }
}

enum class ResponseStatus(val text: String) {
    SUCCESS("success"),
    FAILED("failed")
}