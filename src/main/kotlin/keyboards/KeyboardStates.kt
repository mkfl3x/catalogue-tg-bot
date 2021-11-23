package keyboards

// TODO: should be refactored
class KeyboardStates {

    // stores sent keyboards. key: chatId; value: list of keyboard names
    private val states = mutableMapOf<Long, MutableList<String>>()

    // add keyboard to list of sent keyboards
    fun addKeyboard(chatId: Long, state: String) {
        if (states[chatId] == null) {
            states[chatId] = mutableListOf()
        }
        states[chatId]!!.add(state)
    }

    // returns previous keyboard and remove last one
    fun getPreviousKeyboard(chatId: Long): String {
        if (getCurrentKeyboard(chatId) == "MainKeyboard")
            return getCurrentKeyboard(chatId)
        states[chatId]!!.removeLast()
        return getCurrentKeyboard(chatId)
    }

    // returns current user's keyboard
    fun getCurrentKeyboard(chatId: Long) = states[chatId]!!.last()

    // remove user from states map
    fun dropState(chatId: Long) {
        states.remove(chatId)
    }
}