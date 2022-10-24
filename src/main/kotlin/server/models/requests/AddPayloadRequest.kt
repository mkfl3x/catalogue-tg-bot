package server.models.requests

import com.google.gson.annotations.SerializedName
import database.mongo.DataManager
import server.models.Error
import server.models.Result
import server.models.objects.Location

data class AddPayloadRequest(
    @SerializedName("name") val name: String,
    @SerializedName("type") val type: String,
    @SerializedName("data") val data: String,
    @SerializedName("location") val location: Location?
) : Request() {

    override val schema: Schemas
        get() = Schemas.ADD_PAYLOAD_REQUEST

    override val successMessage: String
        get() = "Payload \"${name}\" successfully added"

    override fun validateData(): Result? {
        RequestValidator.validateIds(location?.hostKeyboard)?.let { return it }
        RequestValidator.validateReservedNames(name)?.let { return it }
        if (DataManager.getPayloads().any { it.name == name })
            return Result.error(Error.PAYLOAD_ALREADY_EXISTS, name)
        location?.let { location -> RequestValidator.validateLocation(location)?.let { return it } }
        return null
    }
}