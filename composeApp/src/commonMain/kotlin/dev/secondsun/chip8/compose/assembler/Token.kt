package dev.secondsun.chip8.compose.assembler

sealed interface Token {
    val line : Int
    val column : Int

    data class Colon(override val line : Int,override  val column : Int) : Token
    data class Identifier(val name : String,override  val line : Int,override  val column : Int) : Token
}

