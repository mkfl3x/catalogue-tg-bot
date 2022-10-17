package server.models.requests

import com.google.gson.annotations.SerializedName
import database.DataManager
import io.ktor.http.*
import org.bson.types.ObjectId
import server.models.Error
import server.models.Result

data class DeleteButtonRequest(
    @SerializedName("button_id") val buttonId: String,
) : Request() {

    override val schema: Schemas
        get() = Schemas.DELETE_BUTTON_REQUEST

    override val successMessage: String
        get() = "Button '$buttonId' successfully deleted"

    override fun validateData(): Result? {
        if (DataManager.getButton(ObjectId(buttonId)) == null)
            return Result.error(Error.BUTTON_DOES_NOT_EXIST, buttonId)
        return null
    }
}