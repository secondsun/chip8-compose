package dev.secondsun.chip8.compose.assembler

class StringMode {
    val values = mutableMapOf<String, Int>()
    val bodies = mutableMapOf<String, List<Any>>() // raw tokens per char
    var calls: Int = 0
}