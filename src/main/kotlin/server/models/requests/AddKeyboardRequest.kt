package server.models.requests

import com.google.gson.annotations.SerializedName
import server.Request
import server.RequestValidator
import server.models.Result
import server.models.objects.Location
import server.RequestActions.addKeyboard

data class AddKeyboardRequest(
    @SerializedName("name") val name: String,
    @SerializedName("buttons") val buttons: List<String>,
    @SerializedName("location") val location: Location?
) : Request() {

    override fun validateData(): Result? {
        RequestValidator.validateIds(*buttons.toTypedArray(), location?.hostKeyboard)?.let { return it }
        RequestValidator.validateReservedNames(name)?.let { return it }
        RequestValidator.validateKeyboardAbsence(name)?.let { return it }
        buttons.forEach { id -> RequestValidator.validateButtonExistence(id)?.let { return it } }
        location?.let { location -> RequestValidator.validateLocation(location)?.let { return it } }
        return null
    }

    override fun relatedAction() = addKeyboard(this)
}