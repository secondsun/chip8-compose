package dev.secondsun.chip8.util

import java.util.*

object Input {
    private val keyMap: MutableMap<String?, Int?> = HashMap<String?, Int?>()

    init {
        keyMap.put("x", 0x0)
        keyMap.put("1", 0x1)
        keyMap.put("2", 0x2)
        keyMap.put("3", 0x3)
        keyMap.put("q", 0x4)
        keyMap.put("w", 0x5)
        keyMap.put("e", 0x6)
        keyMap.put("a", 0x7)
        keyMap.put("s", 0x8)
        keyMap.put("d", 0x9)
        keyMap.put("z", 0xa)
        keyMap.put("c", 0xb)
        keyMap.put("4", 0xc)
        keyMap.put("r", 0xd)
        keyMap.put("f", 0xe)
        keyMap.put("v", 0xf)
    }

    fun map(i: Int, text: String?) {
        if (text != null && !text.isEmpty()) {
            keyMap.put(text.lowercase(Locale.getDefault()), i)
        }
    }

    private var KEYS = -1

    fun press(i: Int) {
        KEYS = 0x0000000F and i
    }

    fun press(input: String) {
        var input = input
        input = input.lowercase(Locale.getDefault())
        if (keyMap.containsKey(input)) {
            KEYS = 0x0000000F and keyMap.get(input)!!
        }
    }

    fun unpress(input: String) {
        var input = input
        input = input.lowercase(Locale.getDefault())

        if (keyMap.containsKey(input)) {
            KEYS = -1
        }
    }

    fun unpress() {
        KEYS = -1
    }

    /**
     * returns the currently pressed keys
     *
     * @return the int value of the key pressed
     */
    fun read(): Int {
        return KEYS
    }
}
