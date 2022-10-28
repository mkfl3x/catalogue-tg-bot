package server.models.requests

import com.google.gson.annotations.SerializedName
import database.mongo.DataManager
import io.ktor.http.*
import server.models.Error
import server.models.Result

data class DeletePayloadRequest(
    @SerializedName("payload_id") val payloadId: String,
) : Request() {

    override val successMessage: String
        get() = "Payload with ID \"$payloadId\" successfully deleted"

    override fun validateData(): Result? {
        RequestValidator.validateIds(payloadId)?.let { return it }
        return if (DataManager.getPayload(payloadId) == null)
            Result.error(Error.BUTTON_DOES_NOT_EXIST, payloadId) else null
    }
}