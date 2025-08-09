package dev.secondsun.chip8.compose.assembler

sealed interface Error {
    object NoMain : Error
}