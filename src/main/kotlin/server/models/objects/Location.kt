package server.models.objects

import com.google.gson.annotations.SerializedName

data class Location(
    @SerializedName("lead_button") val leadButtonText: String,
    @SerializedName("host_keyboard") val hostKeyboard: String
)