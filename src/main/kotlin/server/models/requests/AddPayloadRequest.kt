package server.models.requests

import com.google.gson.annotations.SerializedName
import database.DataManager
import database.DataManager.isKeyboardExist
import org.bson.types.ObjectId
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

        // 1. validate payload name
        validateReservedNames(name)

        // 2. validate is payload with same name exists
        if (DataManager.getPayloads().any { it.name == name })
            return Result.error(Error.PAYLOAD_ALREADY_EXISTS, name)

        // 3. check location
        location?.let {

            // validate lead button text
            validateReservedNames(it.leadButtonText)

            // check is host keyboard exists
            if (!isKeyboardExist(ObjectId(it.hostKeyboard)))
                return Result.error(Error.KEYBOARD_DOES_NOT_EXIST, it.hostKeyboard)
        }
        return null
    }
}