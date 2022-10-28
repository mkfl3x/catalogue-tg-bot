package server.models.requests

import com.google.gson.annotations.SerializedName
import database.mongo.DataManager
import io.ktor.http.*
import server.models.Error
import server.models.Result

data class DeleteButtonRequest(
    @SerializedName("button_id") val buttonId: String,
) : Request() {

    override val successMessage: String
        get() = "Button with ID \"$buttonId\" successfully deleted"

    override fun validateData(): Result? {
        RequestValidator.validateIds(buttonId)?.let { return it }
        return if (DataManager.getButton(buttonId) == null)
            Result.error(Error.BUTTON_DOES_NOT_EXIST, buttonId) else null
    }
}