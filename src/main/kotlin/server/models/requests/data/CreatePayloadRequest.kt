package server.models.requests.data

import com.google.gson.annotations.SerializedName
import server.RequestActions.createPayload
import server.models.objects.Location
import server.models.requests.Request
import server.validations.RequestDataValidators

data class CreatePayloadRequest(
    @SerializedName("name") val name: String,
    @SerializedName("type") val type: String,
    @SerializedName("data") val data: String,
    @SerializedName("location") val location: Location?
) : Request {

    override val schemaPath: String
        get() = "json-schemas/models/requests/objects/payload.json"

    override fun validateData() {
        RequestDataValidators.validateIds(location?.hostKeyboard)
        RequestDataValidators.validateReservedNames(name)
        RequestDataValidators.validateButtonDoesNotExist(name)
        location?.apply { RequestDataValidators.validateLocation(this) }
    }

    override fun relatedAction() = createPayload(this)
}