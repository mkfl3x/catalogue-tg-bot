package server.models.requests.data

import com.google.gson.annotations.SerializedName
import server.RequestActions.deleteButton
import server.models.requests.Request
import server.validations.RequestDataValidators

data class DeleteButtonRequest(
    @SerializedName("button_id") val buttonId: String,
) : Request {

    override val schemaPath: String
        get() = "json-schemas/models/requests/delete_button_request.json"

    override fun validateData() {
        RequestDataValidators.validateIds(buttonId)
        RequestDataValidators.validateButtonExists(buttonId)
    }

    override fun relatedAction() = deleteButton(this)
}