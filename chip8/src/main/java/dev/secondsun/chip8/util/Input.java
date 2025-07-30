package dev.secondsun.chip8.util;

import java.util.HashMap;
import java.util.Map;

public final class Input {

    private final static Map<String, Integer> keyMap = new HashMap<>();

    static {
        keyMap.put("x", 0x0);
        keyMap.put("1", 0x1);
        keyMap.put("2", 0x2);
        keyMap.put("3", 0x3);
        keyMap.put("q", 0x4);
        keyMap.put("w", 0x5);
        keyMap.put("e", 0x6);
        keyMap.put("a", 0x7);
        keyMap.put("s", 0x8);
        keyMap.put("d", 0x9);
        keyMap.put("z", 0xa);
        keyMap.put("c", 0xb);
        keyMap.put("4", 0xc);
        keyMap.put("r", 0xd);
        keyMap.put("f", 0xe);
        keyMap.put("v", 0xf);

    }

    public static void map(int i, String text) {
        if (text != null && !text.isEmpty()) {
            keyMap.put(text.toLowerCase(), i);
        }
    }

    private Input() {
    }

    private static int KEYS = -1;

    public static void press(int i) {
        KEYS = 0x0000000F & i;
    }

    public static void press(String input) {
        input = input.toLowerCase();
        if (keyMap.containsKey(input)) {
            KEYS = 0x0000000F & keyMap.get(input);
        }

    }

    public static void unpress(String input) {
        input = input.toLowerCase();

        if (keyMap.containsKey(input)) {
            KEYS = -1;
        }
    }

    public static void unpress() {
        KEYS = -1;
    }

    /**
     * returns the currently pressed keys
     *
     * @return the int value of the key pressed
     */
    public static int read() {
        return KEYS;
    }

}
