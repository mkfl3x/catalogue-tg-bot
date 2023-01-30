package server.models.requests.data

import com.google.gson.annotations.SerializedName
import server.RequestActions.renameButton
import server.models.requests.Request
import server.validations.RequestDataValidators

data class RenameButtonRequest(
    @SerializedName("button_id") val buttonId: String,
    @SerializedName("new_name") val newName: String
) : Request {

    override val schemaPath: String
        get() = "json-schemas/models/requests/rename_button_request.json"

    override fun validateData() {
        RequestDataValidators.validateIds(buttonId)
        RequestDataValidators.validateReservedNames(newName)
        RequestDataValidators.validateButtonExists(buttonId)
        RequestDataValidators.validateButtonDoesNotExist(newName)
    }

    override fun relatedAction() = renameButton(this)
}