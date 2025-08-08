package dev.secondsun.chip8.compose.assembler

data class AssemblerResult(
    val rom: UByteArray,
    val debugInfo: DebugInfo,
    val hasMain: Boolean,
    val schip: Boolean,
    val xo: Boolean,
    val breakpoints: Map<Int, String>,
    val monitors: Map<String, Monitor>,
    val hereAddr: Int,
    val labels: Map<String, Int>,
    val aliases: Map<String, Int>,
    val constants: Map<String, Int>
)
