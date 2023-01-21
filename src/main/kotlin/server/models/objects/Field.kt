package server.models.objects

import com.google.gson.annotations.SerializedName

data class Field(
    @SerializedName("name") val name: String,
    @SerializedName("value") val value: String
)