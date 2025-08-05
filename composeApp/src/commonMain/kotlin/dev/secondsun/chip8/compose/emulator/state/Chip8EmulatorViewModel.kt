package dev.secondsun.chip8.compose.emulator.state

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.secondsun.chip8.Chip8
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.jetbrains.skia.Image
import org.jetbrains.skia.ImageInfo
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.DurationUnit
import kotlin.time.ExperimentalTime
import kotlin.time.toDuration

class Chip8EmulatorViewModel(val chip8: Chip8, var cyclesPerFrame : Int) : ViewModel() {


    private var job: Job? = null

    //private var emulatorState = Chip8Emulator()
    private var running = false
    private var pause = false
    private var step = false
    private var cycleDelayCounter = 0

    val frame = MutableStateFlow<ImageBitmap>(getFrame())

    override fun onCleared() {
        super.onCleared()
        stop()
    }

    fun stop() {
        running = false
        job?.cancel()
    }

    suspend fun launch() {

        running = true
        job = viewModelScope.launch(Dispatchers.Default) {
            while (running) {
                chip8.cycle();
                cycleDelayCounter++
                if (cycleDelayCounter >= cyclesPerFrame) {
                    cycleDelayCounter = 0
                    frame.value = getFrame()
                    delay(16.666.milliseconds)
                }
            }
        }

    }

    /**
     * This class returns the screen frame of the chip8 screen data
     */
    fun getFrame(): ImageBitmap {

        val resolution = when(chip8.screen.size) {
            2048 -> Dimension(64, 32)
            8192 -> Dimension(128, 64)
            else -> throw IllegalStateException("Screen size not supported")
        }


        val pixels = chip8.screen.map {
            when(it.toInt()) {
                0 -> byteArrayOf(0x00.toByte(),0,0,0xFF.toByte())
                1 -> byteArrayOf(0xFF.toByte(), 0xFF.toByte(),0xFF.toByte(),0xFF.toByte())
                else -> throw IllegalStateException("Screen pixel value not supported")
            }
        }.flatMap { it.asIterable() }.toByteArray()


        val image: Image = Image.makeRaster(
            imageInfo = ImageInfo.makeN32Premul(resolution.width, resolution.height),
            bytes = pixels,
            rowBytes = resolution.width * 4,
        )

        return image.toComposeImageBitmap()


    }


    data class Dimension(val width: Int, val height: Int)

}

