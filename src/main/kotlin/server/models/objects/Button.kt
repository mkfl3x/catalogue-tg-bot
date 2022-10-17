package server.models.objects

import com.google.gson.annotations.SerializedName

@Deprecated("Object described in request model")
data class Button(
    @SerializedName("text") val text: String,
    @SerializedName("type") val type: String,
    @SerializedName("link") val link: String,
    @SerializedName("host_keyboard") val hostKeyboard: String?
)