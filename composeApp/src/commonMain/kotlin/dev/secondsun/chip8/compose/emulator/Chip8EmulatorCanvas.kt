package dev.secondsun.chip8.compose.emulator

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.toIntSize
import dev.secondsun.chip8.compose.emulator.state.Chip8EmulatorViewModel


@Composable
fun Chip8EmulatorCanvas(modifier: Modifier = Modifier, chip8EmulatorViewModel: Chip8EmulatorViewModel) {

    val bitmap = chip8EmulatorViewModel.frame.collectAsState()

    Canvas(modifier = modifier) {
        this.drawImage(bitmap.value,
            blendMode = androidx.compose.ui.graphics.BlendMode.Src,
            filterQuality = androidx.compose.ui.graphics.FilterQuality.None,
            dstSize = this.size.toIntSize())
    }
}