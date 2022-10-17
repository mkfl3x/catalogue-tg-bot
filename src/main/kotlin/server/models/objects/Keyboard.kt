package server.models.objects

import com.google.gson.annotations.SerializedName

@Deprecated("Object described in request model")
data class Keyboard(
    @SerializedName("name") val name: String,
    @SerializedName("buttons") val buttons: List<String>,
    @SerializedName("location") val location: Location?
)