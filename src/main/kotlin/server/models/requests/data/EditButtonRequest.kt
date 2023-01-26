package server.models.requests.data

import com.google.gson.annotations.SerializedName
import server.RequestActions.editButton
import server.models.objects.Field
import server.models.requests.Request
import server.validations.RequestDataValidators
import server.validations.RequestDataValidators.validateButtonNameDuplication

data class EditButtonRequest(
    @SerializedName("button_id") val buttonId: String,
    @SerializedName("fields") val fields: List<Field>
) : Request {

    override val schemaPath: String
        get() = "json-schemas/models/requests/edit_button_request.json"

    override fun validateData() {
        RequestDataValidators.validateIds(buttonId)
        RequestDataValidators.validateButtonExists(buttonId)
        fields.forEach { field ->
            RequestDataValidators.validateValueInList(field.name, listOf("text"))
            RequestDataValidators.validateReservedNames(field.value)
            when (field.name) {
                "text" -> validateButtonNameDuplication(buttonId, field.value)
            }
        }
    }

    override fun relatedAction() = editButton(this)
}