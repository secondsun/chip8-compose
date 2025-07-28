package dev.secondsun.chip8.compose.localproviders

import androidx.compose.runtime.compositionLocalOf

data class DarkModeState(val isDarkMode: Boolean)

val LocalDarkMode = compositionLocalOf { DarkModeState(true) }
