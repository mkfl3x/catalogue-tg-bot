package server.models.requests

import com.google.gson.annotations.SerializedName
import server.Request
import server.RequestValidator
import server.models.Result
import server.RequestActions.deleteButton

data class DeleteButtonRequest(
    @SerializedName("button_id") val buttonId: String,
) : Request() {

    override fun validateData(): Result? {
        RequestValidator.validateIds(buttonId)?.let { return it }
        RequestValidator.validateButtonExistence(buttonId)
        return null
    }

    override fun relatedAction() = deleteButton(this)
}