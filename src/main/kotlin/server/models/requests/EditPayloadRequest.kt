package server.models.requests

import com.google.gson.annotations.SerializedName
import server.Request
import server.RequestActions.editPayload
import server.RequestValidator
import server.RequestValidator.validatePayloadAbsence
import server.models.Result
import server.models.objects.Field

data class EditPayloadRequest(
    @SerializedName("payload_id") val payloadId: String,
    @SerializedName("fields") val fields: List<Field>
) : Request() {

    override fun validateData(): Result? {
        RequestValidator.validateIds(payloadId)?.let { return it }
        RequestValidator.validatePayloadExistence(payloadId)?.let { return it }
        fields.forEach { field ->
            RequestValidator.validateIsInList(field.name, listOf("name", "data"))?.let { return it }
            RequestValidator.validateReservedNames(field.value)?.let { return it }
            when (field.name) {
                "name" -> validatePayloadAbsence(field.value)?.let{ return it } // Check that name is unique
            }
        }
        return null
    }

    override fun relatedAction() = editPayload(this)
}