package server.models.requests

import com.google.gson.annotations.SerializedName
import io.ktor.http.*
import server.models.Result

data class DeletePayloadRequest(
    @SerializedName("payload_id") val payloadId: String,
) : Request() {

    override val successMessage: String
        get() = "Payload with ID \"$payloadId\" successfully deleted"

    override fun validateData(): Result? {
        RequestValidator.validateIds(payloadId)?.let { return it }
        RequestValidator.validatePayloadExistence(payloadId)?.let { return it }
        return null
    }
}