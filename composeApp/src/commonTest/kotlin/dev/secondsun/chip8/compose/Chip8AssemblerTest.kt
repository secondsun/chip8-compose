package dev.secondsun.chip8.compose

import org.junit.Test
import kotlin.random.Random
import kotlin.test.assertEquals

class Chip8AssemblerTest {

    /**
     * This tests that a screen sized byte array can become an array of ints in RGB.
     * Note : This test only checks the right size is returned by the mapping.
     */
    @Test()
    fun testColorMap() {

        val bytes = ByteArray(64*32)

        for (i in 0..<(64*32)) {
            bytes[i] = Random.nextInt(0, 2).toByte()
        }

        val pixels = bytes.map {
            when(it.toInt()) {
                0 -> byteArrayOf(0,0,0,0)
                1 -> byteArrayOf(0, 0xFF.toByte(),0xFF.toByte(),0xFF.toByte())
                else -> throw IllegalStateException("Screen pixel value not supported")
            }
        }.flatMap { it.asIterable() }.toByteArray()

        assertEquals(64*32*4, pixels.size)

    }
}