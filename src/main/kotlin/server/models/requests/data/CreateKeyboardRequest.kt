package server.models.requests.data

import com.google.gson.annotations.SerializedName
import server.RequestActions.createKeyboard
import server.models.objects.Location
import server.models.requests.Request
import server.validations.RequestValidator

data class CreateKeyboardRequest(
    @SerializedName("name") val name: String,
    @SerializedName("buttons") val buttons: List<String>?,
    @SerializedName("location") val location: Location?
) : Request {

    override val schemaPath: String
        get() = "json-schemas/models/requests/objects/keyboard.json"

    override fun validateData() {
        RequestValidator.validateIds(*buttons?.toTypedArray() ?: emptyArray(), location?.hostKeyboard)
        RequestValidator.validateReservedNames(name)
        RequestValidator.validateKeyboardAbsence(name)
        if (!buttons.isNullOrEmpty()) {
            buttons.forEach { id ->
                RequestValidator.validateButtonExistence(id)
                location?.let { location -> RequestValidator.validateLocation(location) }
            }
        }
    }

    override fun relatedAction() = createKeyboard(this)
}