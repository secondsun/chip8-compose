package dev.secondsun.chip8.compose.localproviders
import androidx.compose.runtime.compositionLocalOf


data class KCEFState(val restartRequired:Boolean, val downloading:Float, val initialized:Boolean)

val LocalKCEF = compositionLocalOf { KCEFState(false, 0F, false) }
