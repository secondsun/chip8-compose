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
package dev.secondsun.chip8

import dev.secondsun.chip8.util.Audio
import dev.secondsun.chip8.util.Input
import java.util.*

/**
 * This class is the main chip8 system
 */
class Chip8 {
    var pc: Int = 0x200
        private set
    private var iRegister = 0
    private val registers = IntArray(0x10)
    private val random = Random()
    var sp: Int = 0
        private set
    private var delayTimer = 0
    private var soundTimer = 0
    private val stack = IntArray(16)
    val memory: ByteArray
    var screen: ByteArray = ByteArray(64 * 32)
        private set
    private var nextTimer: Long = 0

    constructor() {
        this.memory = ByteArray(4096)
        loadFont(memory)
    }

    constructor(memory: ByteArray) {
        require(memory.size <= 4096) { "Memory may not be greater than 4096 bytes" }
        this.memory = memory
        loadFont(memory)
    }

    private fun setVX(value: Int, register: Int) {
        registers[register] = (0x000000FF and value)
    }

    private fun getVX(x: Int): Int {
        return 0x000000FF and registers[x]
    }

    val v0: Int
        get() = registers[0] and 0xFF

    val v1: Int
        get() = registers[1] and 0xFF

    val v2: Int
        get() = registers[0x2] and 0xFF

    val v3: Int
        get() = registers[0x3] and 0xFF

    val v4: Int
        get() = registers[0x4] and 0xFF

    val v5: Int
        get() = registers[0x5] and 0xFF

    val v6: Int
        get() = registers[0x6] and 0xFF

    val v7: Int
        get() = registers[0x7] and 0xFF

    val v8: Int
        get() = registers[0x8] and 0xFF

    val v9: Int
        get() = registers[0x9] and 0xFF

    val vA: Int
        get() = registers[0xa] and 0xFF

    val vB: Int
        get() = registers[0xb] and 0xFF

    val vC: Int
        get() = registers[0xc] and 0xFF

    val vD: Int
        get() = registers[0xd] and 0xFF

    val vE: Int
        get() = registers[0xe] and 0xFF

    val vF: Int
        get() = registers[0xf] and 0xFF

    fun execute(instruction: Int) {
        val high = (instruction and 0xF000)

        when (high) {
            0x1000 -> {
                //1NNN Jump to NNN
                val low = 0x0FFF and instruction
                this.pc = low
            }

            0x2000 -> {
                //2NNN start subroutine at NNN
                stack[this.sp] = this.pc
                this.sp++
                val low = 0x0FFF and instruction
                this.pc = low
            }

            0x3000 -> {
                //3XNN Skip if Vx = NN
                val low = 0x0FF and instruction
                val register = (instruction and 0x0f00) shr 8
                if ((getVX(register)) == low) {
                    this.pc += 0x2
                }
            }

            0x5000 -> {
                //5XY0 Skip if Vx = Vy
                val registery = (instruction and 0x00f0) shr 4
                val registerx = (instruction and 0x0f00) shr 8
                if (getVX(registerx) == getVX(registery)) {
                    this.pc += 0x2
                }
            }

            0x4000 -> {
                //$XNN Skip if Vx != NN
                val low = 0x0FF and instruction
                val register = (instruction and 0x0f00) shr 8
                if (getVX(register) != low) {
                    this.pc += 0x2
                }
            }

            0x9000 -> {
                //9XY0 Skip if Vx != Vy
                val registery = (instruction and 0x00f0) shr 4
                val registerx = (instruction and 0x0f00) shr 8
                if (getVX(registerx) != getVX(registery)) {
                    this.pc += 0x2
                }
            }

            0x0000 -> {
                when (instruction) {
                    0x00EE -> {
                        this.sp--
                        this.pc = stack[this.sp]
                    }

                    0x00E0 -> this.screen = ByteArray(screen.size)
                    else -> throw UnsupportedOperationException("Unsupported opcode:" + Integer.toHexString(instruction))
                }
            }

            0xF000 -> {
                val low = instruction and 0xFF
                val register = (instruction and 0x0F00) shr 8

                when (low) {
                    0x15 -> delayTimer = getVX(register)
                    0x65 -> {
                        val maxRegister = register.toByte()
                        var i = 0
                        while (i <= maxRegister) {
                            setVX(memory[iRegister].toInt(), i)
                            iRegister++
                            i++
                        }
                    }

                    0x55 -> {
                        val maxRegister = register.toByte()
                        var i = 0
                        while (i <= maxRegister) {
                            memory[iRegister] = getVX(i).toByte()
                            iRegister++
                            i++
                        }
                    }

                    0x18 -> soundTimer = getVX(register)
                    0x0A -> if (Input.read() == -1) {
                        this.pc -= 0x2
                    } else {
                        setVX(Input.read(), register)
                    }

                    0x07 -> setVX(delayTimer, register)
                    0x29 -> iRegister = getCharacterAddress(getVX(register))
                    0x33 -> {
                        val value = getVX(register)
                        memory[iRegister] = (value / 100).toByte()
                        memory[iRegister + 1] = (((value) % 100) / 10).toByte()
                        memory[iRegister + 2] = (((value) % 100) % 10).toByte()
                    }

                    0x1E -> iRegister += getVX(register)
                    else -> throw UnsupportedOperationException("Unsupported opcode:" + Integer.toHexString(instruction))
                }
            }

            0xB000 -> {
                //BNNN Jump to NNN + V0
                val low = 0x0FFF and instruction
                this.pc = low + getVX(0)
            }

            0x6000 -> {
                //6XNN	Store number NN in register VX
                val low = 0x0FF and instruction
                val register = (instruction and 0x0f00) shr 8
                setVX(low, register)
            }

            0x7000 -> {
                //7XNN	Adds number NN to register VX
                val low = 0x0FF and instruction
                val register = (instruction and 0x0f00) shr 8
                setVX(getVX(register) + low, register)
            }

            0xC000 -> {
                //7XNN	Mask a random and  number NN to register VX
                val low = 0x0FF and instruction
                val register = (instruction and 0x0f00) shr 8
                setVX(random.nextInt(0xFF) and low, register)
            }

            0xE000 -> {
                //EXop Skips based on keyboard input
                val low = 0x0FF and instruction
                val register = (instruction and 0x0f00) shr 8

                when (low) {
                    0x9E ->                         //skip if register == input
                        if (getVX(register) == Input.read()) {
                            this.pc += 0x2
                        }

                    0xA1 ->                         //skip if register != input
                        if (getVX(register) != Input.read()) {
                            this.pc += 0x2
                        }

                    else -> throw UnsupportedOperationException("Unsupported opcode:" + Integer.toHexString(instruction))
                }
            }

            0x8000 -> {
                val low = 0x00F and instruction
                val registerX = (instruction and 0x0F00) shr 8
                val registerY = (instruction and 0x00F0) shr 4

                when (low) {
                    0 -> {
                        setVX(getVX(registerY), registerX)
                    }

                    1 -> {
                        setVX(getVX(registerY) or getVX(registerX), registerX)
                    }

                    2 -> {
                        setVX(getVX(registerY) and getVX(registerX), registerX)
                    }

                    3 -> {
                        setVX(getVX(registerY) xor getVX(registerX), registerX)
                    }

                    4 -> {
                        val sum = getVX(registerX) + getVX(registerY)
                        registers[0xf] = if (sum > 0xFF) 1 else 0
                        setVX(sum, registerX)
                    }

                    5 -> {
                        val difference = getVX(registerX) - getVX(registerY)
                        registers[0xf] = if (difference > 0) 1 else 0
                        setVX(difference, registerX)
                    }

                    6 -> {
                        registers[0xf] = getVX(registerX) and 0x01
                        setVX(getVX(registerX) shr 1, registerX)
                    }

                    0xE -> {
                        registers[0xf] = (getVX(registerX) shr 7) and 0x01
                        setVX(getVX(registerX) shl 1, registerX)
                    }

                    7 -> {
                        val difference = getVX(registerY) - getVX(registerX)
                        registers[0xf] = if (difference > 0) 1 else 0
                        setVX(difference, registerX)
                    }

                    else -> throw UnsupportedOperationException("Unsupported opcode:" + Integer.toHexString(instruction))
                }
            }

            0xA000 -> {
                val low = 0x0FFF and instruction
                iRegister = low
            }

            0xD000 -> {
                val lines = 0x00F and instruction
                val registerX = (instruction and 0x0F00) shr 8
                val registerY = (instruction and 0x00F0) shr 4

                val x = getVX(registerX)
                val y = getVX(registerY)
                registers[0xF] = 0
                var count = 0
                while ((count < lines)) {
                    val oldVideo = getSpriteRow(x, y + count)
                    writeVideo(x, y + count, memory[iRegister + count])
                    registers[0xF] =
                        (if (((memory[iRegister + count].toInt() and oldVideo.toInt())) == 0) (registers[0xF].toByte()) else 1).toInt()
                    count++
                }
            }

            else -> throw UnsupportedOperationException("Unsupported opcode:" + Integer.toHexString(instruction))

        }
    }

    fun cycle() {
        val instruction = ((memory[this.pc++].toInt() shl 8) and 0xFF00) or (memory[this.pc++].toInt() and 0xFF)
        val time = System.currentTimeMillis()
        if (time > nextTimer) {
            countDownTimers()
            nextTimer = time + (1000 / 60)
        }
        execute(instruction)
    }

    private fun countDownTimers() {
        if (delayTimer > 0) {
            delayTimer--
        }
        if (soundTimer > 0) {
            soundTimer--
            Audio.play()
        } else {
            Audio.stop()
        }
    }

    fun getiRegister(): Int {
        return iRegister
    }

    /**
     *
     * Packs a graphics row (8 pixels of the sprite) into a btye
     *
     * @param x
     * @param y
     * @return
     */
    private fun getSpriteRow(x: Int, y: Int): Byte {
        var x = x
        var y = y
        x = x % 64
        y = y % 32
        val byte1 = this.screen[x + y * 64]
        val byte2 = this.screen[(x + 1) % 64 + y * 64]
        val byte3 = this.screen[(x + 2) % 64 + y * 64]
        val byte4 = this.screen[(x + 3) % 64 + y * 64]
        val byte5 = this.screen[(x + 4) % 64 + y * 64]
        val byte6 = this.screen[(x + 5) % 64 + y * 64]
        val byte7 = this.screen[(x + 6) % 64 + y * 64]
        val byte8 = this.screen[(x + 7) % 64 + y * 64]

        return (((byte1.toInt() shl 7)
                or (byte2.toInt() shl 6)
                or (byte3.toInt() shl 5)
                or (byte4.toInt() shl 4)
                or (byte5.toInt() shl 3)
                or (byte6.toInt() shl 2)
                or (byte7.toInt() shl 1)
                or (byte8).toInt())).toByte()
    }

    private fun writeVideo(x: Int, y: Int, b: Byte) {
        var x = x
        var y = y
        x = x % 64
        y = y % 32
        this.screen[(x) + (y) * 64] =
            (this.screen[(x) + (y) * 64].toInt() xor ((b.toInt() and 128) shr 7).toByte().toInt()).toByte()
        this.screen[(1 + (x)) % 64 + (y) * 64] =
            (this.screen[(1 + (x)) % 64 + (y) * 64].toInt() xor ((b.toInt() and 64) shr 6).toByte().toInt()).toByte()
        this.screen[(2 + (x)) % 64 + (y) * 64] =
            (this.screen[(2 + (x)) % 64 + (y) * 64].toInt() xor ((b.toInt() and 32) shr 5).toByte().toInt()).toByte()
        this.screen[(3 + (x)) % 64 + (y) * 64] =
            (this.screen[(3 + (x)) % 64 + (y) * 64].toInt() xor ((b.toInt() and 16) shr 4).toByte().toInt()).toByte()
        this.screen[(4 + (x)) % 64 + (y) * 64] =
            (this.screen[(4 + (x)) % 64 + (y) * 64].toInt() xor ((b.toInt() and 8) shr 3).toByte().toInt()).toByte()
        this.screen[(5 + (x)) % 64 + (y) * 64] =
            (this.screen[(5 + (x)) % 64 + (y) * 64].toInt() xor ((b.toInt() and 4) shr 2).toByte().toInt()).toByte()
        this.screen[(6 + (x)) % 64 + (y) * 64] =
            (this.screen[(6 + (x)) % 64 + (y) * 64].toInt() xor ((b.toInt() and 2) shr 1).toByte().toInt()).toByte()
        this.screen[(7 + (x)) % 64 + (y) * 64] =
            (this.screen[(7 + (x)) % 64 + (y) * 64].toInt() xor ((b.toInt() and 1)).toByte().toInt()).toByte()
    }

    private fun getCharacterAddress(digit: Int): Int {
        require(((0xff) and digit) <= 0xf) { "$digit is not a valid character" }
        return 5 * digit
    }

    private fun loadFont(memory: ByteArray) {
        var i = 0

        memory[i++] = 0xF0.toByte()
        memory[i++] = 0x90.toByte()
        memory[i++] = 0x90.toByte()
        memory[i++] = 0x90.toByte()
        memory[i++] = 0xF0.toByte()

        memory[i++] = 0x20.toByte()
        memory[i++] = 0x60.toByte()
        memory[i++] = 0x20.toByte()
        memory[i++] = 0x20.toByte()
        memory[i++] = 0x70.toByte()

        memory[i++] = 0xF0.toByte()
        memory[i++] = 0x10.toByte()
        memory[i++] = 0xF0.toByte()
        memory[i++] = 0x80.toByte()
        memory[i++] = 0xF0.toByte()

        memory[i++] = 0xF0.toByte()
        memory[i++] = 0x10.toByte()
        memory[i++] = 0xF0.toByte()
        memory[i++] = 0x10.toByte()
        memory[i++] = 0xF0.toByte()

        memory[i++] = 0x90.toByte()
        memory[i++] = 0x90.toByte()
        memory[i++] = 0xF0.toByte()
        memory[i++] = 0x10.toByte()
        memory[i++] = 0x10.toByte()

        memory[i++] = 0xF0.toByte()
        memory[i++] = 0x80.toByte()
        memory[i++] = 0xF0.toByte()
        memory[i++] = 0x10.toByte()
        memory[i++] = 0xF0.toByte()

        memory[i++] = 0xF0.toByte()
        memory[i++] = 0x80.toByte()
        memory[i++] = 0xF0.toByte()
        memory[i++] = 0x90.toByte()
        memory[i++] = 0xF0.toByte()

        memory[i++] = 0xF0.toByte()
        memory[i++] = 0x10.toByte()
        memory[i++] = 0x20.toByte()
        memory[i++] = 0x40.toByte()
        memory[i++] = 0x40.toByte()

        memory[i++] = 0xF0.toByte()
        memory[i++] = 0x90.toByte()
        memory[i++] = 0xF0.toByte()
        memory[i++] = 0x90.toByte()
        memory[i++] = 0xF0.toByte()

        memory[i++] = 0xF0.toByte()
        memory[i++] = 0x90.toByte()
        memory[i++] = 0xF0.toByte()
        memory[i++] = 0x10.toByte()
        memory[i++] = 0xF0.toByte()

        memory[i++] = 0xF0.toByte()
        memory[i++] = 0x90.toByte()
        memory[i++] = 0xF0.toByte()
        memory[i++] = 0x90.toByte()
        memory[i++] = 0x90.toByte()

        memory[i++] = 0xE0.toByte()
        memory[i++] = 0x90.toByte()
        memory[i++] = 0xE0.toByte()
        memory[i++] = 0x90.toByte()
        memory[i++] = 0xE0.toByte()

        memory[i++] = 0xF0.toByte()
        memory[i++] = 0x80.toByte()
        memory[i++] = 0x80.toByte()
        memory[i++] = 0x80.toByte()
        memory[i++] = 0xF0.toByte()

        memory[i++] = 0xE0.toByte()
        memory[i++] = 0x90.toByte()
        memory[i++] = 0x90.toByte()
        memory[i++] = 0x90.toByte()
        memory[i++] = 0xE0.toByte()

        memory[i++] = 0xF0.toByte()
        memory[i++] = 0x80.toByte()
        memory[i++] = 0xF0.toByte()
        memory[i++] = 0x80.toByte()
        memory[i++] = 0xF0.toByte()

        memory[i++] = 0xF0.toByte()
        memory[i++] = 0x80.toByte()
        memory[i++] = 0xF0.toByte()
        memory[i++] = 0x80.toByte()
        memory[i++] = 0x80.toByte()
    }

}
