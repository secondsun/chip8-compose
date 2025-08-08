package dev.secondsun.chip8.compose.assembler

class MacroBody {
    val args = mutableListOf<String>()
    val body = mutableListOf<String>()
    var calls = 0
}
