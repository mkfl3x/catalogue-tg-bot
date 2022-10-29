package server.models.requests

import com.google.gson.annotations.SerializedName
import server.models.Result
import server.models.objects.Location

data class AddPayloadRequest(
    @SerializedName("name") val name: String,
    @SerializedName("type") val type: String,
    @SerializedName("data") val data: String,
    @SerializedName("location") val location: Location?
) : Request() {

    override val successMessage: String
        get() = "Payload \"${name}\" successfully added"

    override fun validateData(): Result? {
        RequestValidator.validateIds(location?.hostKeyboard)?.let { return it }
        RequestValidator.validateReservedNames(name)?.let { return it }
        RequestValidator.validatePayloadAbsence(name)?.let { return it }
        location?.let { location -> RequestValidator.validateLocation(location)?.let { return it } }
        return null
    }
}