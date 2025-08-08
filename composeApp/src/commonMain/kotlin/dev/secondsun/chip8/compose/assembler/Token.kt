package dev.secondsun.chip8.compose.assembler

data class Token(val token : TypedToken,val  tokenStart: Int,val index: Int)


sealed interface  TypedToken {
    data class StringType(val value: String) : TypedToken
    data class IntType(val value: Int) : TypedToken
}