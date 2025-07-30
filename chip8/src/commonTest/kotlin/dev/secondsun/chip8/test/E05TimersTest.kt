package dev.secondsun.chip8.test

import dev.secondsun.chip8.Chip8
import dev.secondsun.chip8.util.Chip8Utils
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Timeout
import java.io.IOException
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 *
 * @author summers
 */
class E05TimersTest {
    private lateinit var chip8: Chip8

    @BeforeTest
    @Throws(IOException::class)
    fun setUp() {
        this.chip8 = Chip8Utils.createFromRom(javaClass.getResource("/E05TimerLoop.ch8"))
        this.chip8.execute(0x6064)
        this.chip8.execute(0x6127)
        this.chip8.execute(0x6212)
        this.chip8.execute(0x63AE)
        this.chip8.execute(0x64FF)
        this.chip8.execute(0x65B4)
        this.chip8.execute(0x6642)
        this.chip8.execute(0x6F25)
    }

    /**
     * As documented in E03, we will modify the cycle method to count down the
     * delay timer.
     *
     * There are also timer instructions to set the timer to begin a countdown.
     *
     * FX15 Set the timer to VX FX07 Store the value of the timer to VX
     *
     * The timer works on a 60 hertz cycle. IE if you set the timer to 60 it
     * will countdown to 0 over the course of a second.
     *
     */
    @Test
    fun testDelayTimerOpcodes() {
        chip8.execute(0xF015) //Set timer to 0x64
        chip8.execute(0xF107) //Read timer into V1
        assertEquals(0x64, chip8.v1)
    }

    @Test
    @Timeout(500L)
    fun testDelayTimerCountdown() {
        while (chip8.v5 !== 255) {
            chip8.cycle()
        }
    }

    /**
     * There is a second timer, the sound timer. The sound timer will emit a
     * tone until it reaches 0. It operates on the same cycle as the delay
     * timer.
     *
     * Opcodes : 0xFX18 Set the sound timer to the value in VX
     *
     */
    @Test
    fun testSoundTimer() {
        chip8.execute(0xF018) //Set timer to 0x64
        chip8.cycle()
    }

    /**
     * This test will test that sound is actually emitted.
     *
     * It is disabled by default because it could be annoying.
     *
     * @throws IOException loading the program may throw an ioexception.
     */

    @Test
    @Disabled
    @Timeout(110000L)
    @Throws(IOException::class)
    fun testEmitSoundTimer() {
        val soundChip: Chip8 = Chip8Utils.createFromRom(javaClass.getResource("/E05SoundLoop.ch8"))
        while (soundChip.v5 !== 255) {
            soundChip.cycle()
        }
    }
}
