/*
 * The MIT License
 *
 * Copyright 2016 summers.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package dev.secondsun.chip8.test

import dev.secondsun.chip8.Chip8
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 *
 * Chip 8 has a few opcodes which are convenient utility methods.
 */
class E08UtilitiesTest {
    private lateinit var chip8: Chip8

    @BeforeTest
    fun setup() {
        this.chip8 = Chip8()
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
     * FX33 : Convert the value in VX to decimal and store each digit in I, I+1,
     * and I+2 along with any leading 0s.
     */
    @Test
    fun testBCD() {
        chip8.execute(0xA200)

        chip8.execute(0xF033)
        var memory: ByteArray = chip8.memory
        assertEquals(1, memory[0x200])
        assertEquals(0, memory[0x201])
        assertEquals(0, memory[0x202])


        chip8.execute(0xF133)
        memory = chip8.memory
        assertEquals(0, memory[0x200])
        assertEquals(3, memory[0x201])
        assertEquals(9, memory[0x202])

        chip8.execute(0xF433)
        memory = chip8.memory
        assertEquals(2, memory[0x200])
        assertEquals(5, memory[0x201])
        assertEquals(5, memory[0x202])
    }

    /**
     * Chip8 has the ability to copy a range of registers to memory.
     * FX55 : Store the values of registers V0 -> VX to memory addresses I -> I + X.
     * Set I to I + X + 1 afterward.
     */
    @Test
    fun copyToMemory() {
        chip8.execute(0xA200)
        chip8.execute(0xF355)

        val memory: ByteArray = chip8.memory

        assertEquals(0x64.toByte(), memory[0x200])
        assertEquals(0x27.toByte(), memory[0x201])
        assertEquals(0x12.toByte(), memory[0x202])
        assertEquals(0xAE.toByte(), memory[0x203])
        assertEquals(0x204, chip8.getiRegister())
    }

    /**
     * Chip8 has the ability to copy a range of memory to registers.
     * FX65 : Store the values of memory addresses I -> I + X to registers V0 -> VX.
     * Set I to I + X + 1 afterward.
     */
    @Test
    fun copyFromMemory() {
        chip8.execute(0xA200)
        chip8.execute(0xF355)

        val memory: ByteArray = chip8.memory

        assertEquals(0x64.toByte(), memory[0x200])
        assertEquals(0x27.toByte(), memory[0x201])
        assertEquals(0x12.toByte(), memory[0x202])
        assertEquals(0xAE.toByte(), memory[0x203])
        assertEquals(0x204, chip8.getiRegister())

        chip8.execute(0xA200)
        this.chip8.execute(0x6000)
        this.chip8.execute(0x6100)
        this.chip8.execute(0x6200)
        this.chip8.execute(0x6300)
        chip8.execute(0xF365)

        assertEquals(0x64, chip8.v0)
        assertEquals(0x27, chip8.v1)
        assertEquals(0x12, chip8.v2)
        assertEquals(0xAE, chip8.v3 )
        assertEquals(0x204, chip8.getiRegister())
    }
}
