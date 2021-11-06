package keyboards

import java.lang.reflect.Type

enum class Schemas(val path: String, val type: Type) {
    KEYBOARD("/json-schemas/keyboard.json", Keyboard::class.java),
    BUTTON("/json-schemas/button.json", Button::class.java)
}