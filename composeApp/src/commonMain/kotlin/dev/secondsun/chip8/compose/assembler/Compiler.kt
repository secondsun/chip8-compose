package dev.secondsun.chip8.compose.assembler

class Compiler {
    fun compile(parsedProgram: ParserOutput): ByteArray {
        var context = CompilerContext(parsedProgram)
        return byteArrayOf(1)
    }
}