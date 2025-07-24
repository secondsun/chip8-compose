package dev.secondsun.chip8.compose

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform