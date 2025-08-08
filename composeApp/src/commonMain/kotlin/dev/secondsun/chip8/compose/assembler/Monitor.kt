package dev.secondsun.chip8.compose.assembler

sealed interface Monitor {
    data class Register(val name: String, val baseRegister: Int, val length: MonitorLength) : Monitor
    data class Memory(val name: String, val base: Int, val length: MonitorLength) : Monitor
}

sealed interface MonitorLength {
    data class Fixed(val value: Int) : MonitorLength
    data class Format(val parts: List<FormatPart>) : MonitorLength
}

data class FormatPart(val type: Char, val len: Int = 1, val text: String? = null)