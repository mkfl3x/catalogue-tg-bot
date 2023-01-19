package server.models.requests

import com.google.gson.annotations.SerializedName
import server.Request
import server.RequestValidator
import server.models.Result
import server.RequestActions.deletePayload

data class DeletePayloadRequest(
    @SerializedName("payload_id") val payloadId: String,
) : Request() {

    override fun validateData(): Result? {
        RequestValidator.validateIds(payloadId)?.let { return it }
        RequestValidator.validatePayloadExistence(payloadId)?.let { return it }
        return null
    }

    override fun relatedAction() = deletePayload(this)
}