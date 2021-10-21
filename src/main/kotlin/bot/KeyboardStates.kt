package bot

class KeyboardStates {

    private val states = mutableMapOf<Long, MutableList<String>>()

    fun addKeyboard(chatId: Long, state: String) {
        if (states[chatId] == null) {
            states[chatId] = mutableListOf()
        }
        states[chatId]!!.add(state)
    }

    fun removeKeyboard(chatId: Long) {
        states[chatId]!!.removeLast()
    }

    fun getLastKeyboard(chatId: Long) = states[chatId]!!.last()
}