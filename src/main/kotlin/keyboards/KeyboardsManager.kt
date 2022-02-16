package keyboards

import database.MongoClient
import keyboards.models.Button
import keyboards.models.Keyboard
import utils.Properties

object KeyboardsManager {

    val keyboardStates = KeyboardStates()
    private lateinit var keyboards: HashSet<Keyboard>

    init {
        reloadKeyboards()
    }

    fun reloadKeyboards() {
        keyboards = MongoClient.readAllEntries(
            Properties.get("mongo.collection.keyboards"),
            Keyboard::class.java
        ).toHashSet()
    }

    fun getKeyboards(): HashSet<Keyboard> =
        keyboards

    fun getKeyboard(name: String): Keyboard? =
        keyboards.firstOrNull { it.name == name }

    fun getButton(keyboardName: String, buttonText: String): Button? =
        getKeyboard(keyboardName)?.buttons?.firstOrNull { it.text == buttonText }

    fun isKeyboardExist(keyboard: String): Boolean =
        keyboards.any { it.name == keyboard }

    fun isButtonExist(keyboard: String, button: String): Boolean =
        getKeyboard(keyboard)!!.buttons.any { it.text == button }
}