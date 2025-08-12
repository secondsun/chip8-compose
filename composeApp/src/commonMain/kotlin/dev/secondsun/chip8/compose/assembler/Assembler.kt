package dev.secondsun.chip8.compose.assembler

class Assembler {
    fun compile(program: String) : Program {
        return Program().apply {
            if (!program.contains(": main")) {
                addError(Error.NoMain)
            }
        }
    }
}