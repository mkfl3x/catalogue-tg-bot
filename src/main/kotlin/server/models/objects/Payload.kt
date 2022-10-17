package server.models.objects

import com.google.gson.annotations.SerializedName

@Deprecated("Object described in request model")
data class Payload(
    @SerializedName("name") val name: String,
    @SerializedName("type") val type: String,
    @SerializedName("data") val data: String,
    @SerializedName("location") val location: Location?
)