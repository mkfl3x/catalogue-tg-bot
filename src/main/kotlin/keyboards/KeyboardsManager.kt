package keyboards

import database.MongoClient
import keyboards.models.Keyboard
import utils.Properties

object KeyboardsManager {

    val keyboardStates = KeyboardStates() // TODO: move to redis
    private lateinit var keyboards: HashSet<Keyboard>

    init {
        reloadKeyboards()
    }

    fun getKeyboards(): HashSet<Keyboard> = keyboards

    fun getKeyboard(name: String): Keyboard? = keyboards.firstOrNull { it.name == name }

    fun reloadKeyboards() {
        keyboards = MongoClient.readAllEntries(
            Properties.get("mongo.collection.keyboards"),
            Keyboard::class.java
        ).toHashSet()
    }
}