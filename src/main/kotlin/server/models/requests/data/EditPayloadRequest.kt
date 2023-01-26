package server.models.requests.data

import com.google.gson.annotations.SerializedName
import server.RequestActions.editPayload
import server.models.objects.Field
import server.models.requests.Request
import server.validations.RequestDataValidators

data class EditPayloadRequest(
    @SerializedName("payload_id") val payloadId: String,
    @SerializedName("fields") val fields: List<Field>
) : Request {

    override val schemaPath: String
        get() = "json-schemas/models/requests/edit_payload_request.json"

    override fun validateData() {
        RequestDataValidators.validateIds(payloadId)
        RequestDataValidators.validatePayloadExists(payloadId)
        fields.forEach { field ->
            RequestDataValidators.validateValueInList(field.name, listOf("name", "data"))
            RequestDataValidators.validateReservedNames(field.value)
            when (field.name) {
                "name" -> RequestDataValidators.validatePayloadDoesNotExist(field.value)
            }
        }
    }

    override fun relatedAction() = editPayload(this)
}