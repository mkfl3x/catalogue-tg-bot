package server.models.requests.data

import com.google.gson.annotations.SerializedName
import server.RequestActions.addKeyboard
import server.models.objects.Location
import server.models.requests.Request
import server.validations.RequestValidator

data class AddKeyboardRequest(
    @SerializedName("name") val name: String,
    @SerializedName("buttons") val buttons: List<String>,
    @SerializedName("location") val location: Location?
) : Request {

    override val schemaPath: String
        get() = "json-schemas/models/requests/objects/keyboard.json"

    override fun validateData() {
        RequestValidator.validateIds(*buttons.toTypedArray(), location?.hostKeyboard)
        RequestValidator.validateReservedNames(name)
        RequestValidator.validateKeyboardAbsence(name)
        buttons.forEach { id ->
            RequestValidator.validateButtonExistence(id)
            location?.let { location -> RequestValidator.validateLocation(location) }
        }
    }

    override fun relatedAction() = addKeyboard(this)
}