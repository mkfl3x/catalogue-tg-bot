package server.models.requests

import com.google.gson.annotations.SerializedName
import database.DataManager
import io.ktor.http.*
import org.bson.types.ObjectId
import server.models.Error
import server.models.Result

data class DeletePayloadRequest(
    @SerializedName("payload_id") val payloadId: String,
) : Request() {

    override val schema: Schemas
        get() = Schemas.DELETE_PAYLOAD_REQUEST

    override val successMessage: String
        get() = "Payload '$payloadId' successfully deleted"

    override fun validateData(): Result? {
        if (DataManager.getPayload(ObjectId(payloadId)) == null)
            return Result.error(Error.BUTTON_DOES_NOT_EXIST, payloadId)
        return null
    }
}