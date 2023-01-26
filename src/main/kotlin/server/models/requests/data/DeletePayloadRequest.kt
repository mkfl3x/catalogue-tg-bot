package server.models.requests.data

import com.google.gson.annotations.SerializedName
import server.RequestActions.deletePayload
import server.models.requests.Request
import server.validations.RequestDataValidators

data class DeletePayloadRequest(
    @SerializedName("payload_id") val payloadId: String,
) : Request {

    override val schemaPath: String
        get() = "json-schemas/models/requests/delete_payload_request.json"

    override fun validateData() {
        RequestDataValidators.validateIds(payloadId)
        RequestDataValidators.validatePayloadExists(payloadId)
    }

    override fun relatedAction() = deletePayload(this)
}