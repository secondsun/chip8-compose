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
package dev.secondsun.chip8.util

import dev.secondsun.chip8.Chip8
import java.awt.Color
import java.awt.Graphics
import javax.swing.JPanel

/**
 *
 * @author summers
 */
class Chip8DisplayPanel : JPanel() {
    private var chip8 = Chip8()

    override fun paint(g: Graphics) {
        val width = super.getWidth()
        val height = super.getHeight()
        val pixelWidth = width / 64
        val pixelHeight = height / 32
        val video = chip8.screen
        for (x in 0..63) {
            for (y in 0..31) {
                if (video[y * 64 + x].toInt() == 0) {
                    g.setColor(Color.BLACK)
                } else {
                    g.setColor(Color.WHITE)
                }
                g.fillRect(x * pixelWidth, y * pixelHeight, pixelWidth, pixelHeight)
            }
        }
    }

    fun setChip8(chip8: Chip8) {
        this.chip8 = chip8
    }


    companion object {
        private const val serialVersionUID = 0x203920L
    }
}
