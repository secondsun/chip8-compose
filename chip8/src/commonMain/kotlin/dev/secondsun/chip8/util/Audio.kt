package dev.secondsun.chip8.util

import java.util.function.IntSupplier
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.LineUnavailableException
import javax.sound.sampled.SourceDataLine
import kotlin.math.sin

/**
 * This class is a helper class for emitting, playing, and viewing audio data.
 *
 * @author summers
 */
object Audio {
    private val AF = AudioFormat(44100f, 8, 1, true, false)
    private var SDL: SourceDataLine? = null
    private val BUF = ByteArray(1)

    init {
        try {
            SDL = AudioSystem.getSourceDataLine(AF)
            SDL!!.open()
        } catch (ex: LineUnavailableException) {
            SDL = null
        }
    }

    private val STREAM: IntSupplier = (object : IntSupplier {
        var i: Int = 0

        override fun getAsInt(): Int {
            val angle = i / (44100f / 880) * 2.0 * Math.PI
            i++
            return (sin(angle) * 20).toInt().toByte().toInt()
        }
    })

    private var PLAYING = false

    fun play() {
        if (!PLAYING) {
            SDL!!.start()
        }

        for (i in 0..<(44100 / 60)) {
            val number = STREAM.getAsInt()
            BUF[0] = number.toByte()
            SDL!!.write(BUF, 0, 1)
        }
    }

    fun stop() {
        if (PLAYING) {
            SDL!!.stop()
            PLAYING = false
        }
    }
}
