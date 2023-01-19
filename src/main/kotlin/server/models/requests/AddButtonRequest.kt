package server.models.requests

import com.google.gson.annotations.SerializedName
import server.Request
import server.RequestValidator
import server.models.Result
import server.models.objects.Location
import server.RequestActions.addButton

data class AddButtonRequest(
    @SerializedName("text") val text: String,
    @SerializedName("type") val type: String,
    @SerializedName("link") val link: String,
    @SerializedName("host_keyboard") val hostKeyboard: String?
) : Request() {

    override fun validateData(): Result? {
        RequestValidator.validateIds(link, hostKeyboard)?.let { return it }
        RequestValidator.validateReservedNames(text)?.let { return it }
        RequestValidator.validateResourceExistence(type, link)?.let { return it }
        hostKeyboard?.let { keyboard ->
            RequestValidator.validateLocation(Location(text, keyboard))?.let { return it }
            if (type == "keyboard") RequestValidator.validateLoopLinking(link, keyboard)?.let { return it }
        }
        return null
    }

    override fun relatedAction() = addButton(this)
}