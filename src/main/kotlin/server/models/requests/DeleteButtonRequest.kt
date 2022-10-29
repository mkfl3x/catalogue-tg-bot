package server.models.requests

import com.google.gson.annotations.SerializedName
import io.ktor.http.*
import server.models.Result

data class DeleteButtonRequest(
    @SerializedName("button_id") val buttonId: String,
) : Request() {

    override val successMessage: String
        get() = "Button with ID \"$buttonId\" successfully deleted"

    override fun validateData(): Result? {
        RequestValidator.validateIds(buttonId)?.let { return it }
        RequestValidator.validateButtonExistence(buttonId)
        return null
    }
}