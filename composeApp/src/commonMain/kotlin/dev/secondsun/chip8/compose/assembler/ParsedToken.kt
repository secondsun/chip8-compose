package dev.secondsun.chip8.compose.assembler

data class ParsedToken(val type : ParsedTokenType,val tokens: List<Token>)

enum class ParsedTokenType {
    Error,
    Label,
    Constant,
    Number,
    Next,
    Unpack
}
