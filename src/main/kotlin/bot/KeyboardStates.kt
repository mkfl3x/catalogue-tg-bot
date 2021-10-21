package bot

class KeyboardStates {

    private val states = mutableMapOf<Long, MutableList<String>>()

    fun addKeyboard(chatId: Long, state: String) {
        if (states[chatId] == null) {
            states[chatId] = mutableListOf()
        }
        states[chatId]!!.add(state)
    }

    fun getPreviousKeyboard(chatId: Long): String {
        states[chatId]!!.removeLast()
        return getCurrentKeyboard(chatId)
    }

    fun getCurrentKeyboard(chatId: Long) = states[chatId]!!.last()
}