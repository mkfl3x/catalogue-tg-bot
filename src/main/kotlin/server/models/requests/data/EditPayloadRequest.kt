package server.models.requests.data

import com.google.gson.annotations.SerializedName
import server.RequestActions.editPayload
import server.models.objects.Field
import server.models.requests.Request
import server.validations.RequestValidator
import server.validations.RequestValidator.validatePayloadAbsence

data class EditPayloadRequest(
    @SerializedName("payload_id") val payloadId: String,
    @SerializedName("fields") val fields: List<Field>
) : Request {

    override val schemaPath: String
        get() = "json-schemas/models/requests/edit_payload_request.json"

    override fun validateData() {
        RequestValidator.validateIds(payloadId)
        RequestValidator.validatePayloadExistence(payloadId)
        fields.forEach { field ->
            RequestValidator.validateIsInList(field.name, listOf("name", "data"))
            RequestValidator.validateReservedNames(field.value)
            when (field.name) {
                "name" -> validatePayloadAbsence(field.value) // Check that name is unique
            }
        }
    }

    override fun relatedAction() = editPayload(this)
}