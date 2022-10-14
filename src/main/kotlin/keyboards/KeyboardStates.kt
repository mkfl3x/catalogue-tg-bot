package keyboards

import common.ReservedNames
import keyboards.models.Keyboard

// TODO: should be refactored
class KeyboardStates {

    // stores sent keyboards. key: chatId; value: list of keyboards
    private val states = mutableMapOf<Long, MutableList<Keyboard>>()

    // add keyboard to list of sent keyboards
    fun addKeyboard(chatId: Long, keyboard: Keyboard) {
        if (states[chatId] == null) {
            states[chatId] = mutableListOf()
        }
        states[chatId]!!.add(keyboard)
    }

    // returns previous keyboard and remove last one
    fun getPreviousKeyboard(chatId: Long): Keyboard {
        if (getCurrentKeyboard(chatId).name == ReservedNames.MAIN_KEYBOARD.text)
            return getCurrentKeyboard(chatId)
        states[chatId]!!.removeLast()
        return getCurrentKeyboard(chatId)
    }

    // returns current user's keyboard
    fun getCurrentKeyboard(chatId: Long) =
        states[chatId]!!.last()

    // remove user from states map
    fun dropState(chatId: Long) {
        states.remove(chatId)
    }

    // delete removed keyboard from states of all users
    fun delete(keyboard: String) {
        states.values.forEach { list ->
            list.removeIf { it.name == keyboard }
        }
    }
}