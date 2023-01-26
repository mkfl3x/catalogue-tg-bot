package server.models.requests.data

import com.google.gson.annotations.SerializedName
import server.RequestActions.createKeyboard
import server.models.objects.Location
import server.models.requests.Request
import server.validations.RequestDataValidators

data class CreateKeyboardRequest(
    @SerializedName("name") val name: String,
    @SerializedName("buttons") val buttons: List<String>?,
    @SerializedName("location") val location: Location?
) : Request {

    override val schemaPath: String
        get() = "json-schemas/models/requests/objects/keyboard.json"

    override fun validateData() {
        RequestDataValidators.validateIds(*buttons?.toTypedArray() ?: emptyArray(), location?.hostKeyboard)
        RequestDataValidators.validateReservedNames(name)
        RequestDataValidators.validateKeyboardDoesNotExist(name)
        buttons?.onEach { RequestDataValidators.validateButtonExists(it) }
        location?.apply { RequestDataValidators.validateLocation(this) }
    }

    override fun relatedAction() = createKeyboard(this)
}