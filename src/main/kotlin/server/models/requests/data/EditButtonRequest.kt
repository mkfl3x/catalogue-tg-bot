package server.models.requests.data

import com.google.gson.annotations.SerializedName
import server.RequestActions.editButton
import server.models.objects.Field
import server.models.requests.Request
import server.validations.RequestValidator
import server.validations.RequestValidator.validateKeyboardsButtonConflicts

data class EditButtonRequest(
    @SerializedName("button_id") val buttonId: String,
    @SerializedName("fields") val fields: List<Field>
) : Request {

    override val schemaPath: String
        get() = "json-schemas/models/requests/edit_button_request.json"

    override fun validateData() {
        RequestValidator.validateIds(buttonId)
        RequestValidator.validateButtonExistence(buttonId)
        fields.forEach { field ->
            RequestValidator.validateIsInList(field.name, listOf("text"))
            RequestValidator.validateReservedNames(field.value)
            when (field.name) {
                "text" -> validateKeyboardsButtonConflicts(buttonId, field.value)
            }
        }
    }

    override fun relatedAction() = editButton(this)
}