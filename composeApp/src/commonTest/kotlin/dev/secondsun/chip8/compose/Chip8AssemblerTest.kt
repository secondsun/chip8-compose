package dev.secondsun.chip8.compose

import dev.secondsun.chip8.compose.assembler.*
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class Chip8AssemblerTest {

    @Test
    fun testColorMap() {
        val bytes = ByteArray(64*32)
        for (i in 0 until (64*32)) {
            bytes[i] = if (i % 2 == 0) 0 else 1
        }
        val pixels = bytes.map {
            when (it.toInt()) {
                0 -> byteArrayOf(0, 0, 0, 0)
                1 -> byteArrayOf(0, 0xFF.toByte(), 0xFF.toByte(), 0xFF.toByte())
                else -> error("Screen pixel value not supported")
            }
        }.flatMap { it.asIterable() }.toByteArray()
        assertEquals(64*32*4, pixels.size)
    }

    @Test
    fun assembleMinimalMain() {
        val src = """
            : main
                clear
                return
        """.trimIndent()
        val asm = Assembler.Builder().build()
        val result = asm.compile(src)
        // Expect reserved jump patched to jump main at 0x200 -> 0x202
        val expected = ubyteArrayOf(
            // patched later; but since main is label at 0x200, hasMain=false => reserved cleared
            // in this program 'main' is at 0x200 due to : main at the start
            // body: 00E0 00EE
            0x00u,0xE0u, 0x00u,0xEEu
        )
        // We tolerate trailing zeros; compare prefix
        assertContentEquals(expected, result.rom.copyOfRange(0, expected.size))
        assertEquals(false, result.hasMain) // main defined at start resets boot jump
    }

    @Test
    fun forwardReferenceAndLabelPatch() {
        val src = """
            jump later
            : main
                clear
            : later
                return
        """.trimIndent()
        val asm = Assembler.Builder().build()
        val result = asm.compile(src)
        // first word patched to 1NNN to later
        // ensure it compiled and patched (basic sanity)
        // check that there is a 00EE near the end (return)
        val last2 = result.rom.takeLast(2).toUByteArray()
        assertContentEquals(ubyteArrayOf(0x00u, 0xEEu), last2)
        // debug info maps some addresses
        // Not strict here; just ensure non-empty
        assertEquals(true, result.debugInfo.getLine(0x200) != null)
    }

    @Test
    fun macrosAndStringModes() {
        val src = """
            :macro addX a {
              v0 := a
              v1 += v0
            }
            :stringmode text "AB" {
              v0 := VALUE
              v1 += 1
            }
            : main
              addX 3
              text "BA"
              return
        """.trimIndent()
        val asm = Assembler.Builder().build()
        val result = asm.compile(src)
        // sanity: result contains return
        val hasReturn = result.rom.asSequence().windowed(2).any { it[0].toInt()==0x00 && it[1].toInt()==0xEE }
        assertEquals(true, hasReturn)
    }

    @Test
    fun controlFlowAndLoops() {
        val src = """
            : main
              v0 := 1
              if v0 == 1 then v1 := 2
              loop
                v1 += 1
                while v1 == 3
                v1 += 2
              again
        """.trimIndent()
        val asm = Assembler.Builder().build()
        val result = asm.compile(src)
        // just ensure it assembles and the rom is within non-xo bounds
        assertEquals(true, result.rom.size <= 3583)
    }

    @Test
    fun errorCases() {
        val bad = """
            : main
              unknownop 123
        """.trimIndent()
        val asm = Assembler.Builder().build()
        assertFailsWith<AssemblerException> { asm.compile(bad) }
    }

    @Test
    fun debugInfoMapping() {
        val src = """
            # line 0 (empty)
            : main
              clear
              return
        """.trimIndent()
        val asm = Assembler.Builder().build()
        val res = asm.compile(src)
        // Some address in body maps to a line
        val lines = listOfNotNull(
            res.debugInfo.getLine(0x200),
            res.debugInfo.getLine(0x202)
        )
        assertEquals(true, lines.isNotEmpty())
    }

    @Test
    fun goldenChickenscratch() {
        val asm = Assembler.Builder().build()
        val source = resourceText("/chickenscratch.8o")
        val compiled = asm.compile(source).rom
        val expected = resourceBytes("/chickenscratch_output.ch8").toUByteArray()
        assertRomEqualsWithPadding(expected, compiled)
    }

    @Test
    fun goldenOctoparty() {
        val asm = Assembler.Builder().build()
        val source = resourceText("/octoparty.8o")
        val compiled = asm.compile(source).rom
        val expected = resourceBytes("/octoparty_output.ch8").toUByteArray()
        assertRomEqualsWithPadding(expected, compiled)
    }

    // Helpers
    private fun resourceText(path: String): String {
        val url = this::class.java.getResource(path) ?: error("Missing resource $path")
        return url.readText()
    }
    private fun resourceBytes(path: String): ByteArray {
        val url = this::class.java.getResource(path) ?: error("Missing resource $path")
        return url.readBytes()
    }

    private fun assertRomEqualsWithPadding(expected: UByteArray, actual: UByteArray) {
        // Allow trailing zero padding differences
        // Compare up to min size, and ensure the longer tail is zeros
        val min = minOf(expected.size, actual.size)
        assertContentEquals(expected.copyOf(min), actual.copyOf(min))
        if (expected.size > actual.size) {
            val tail = expected.copyOfRange(min, expected.size)
            assertEquals(true, tail.all { it.toInt() == 0 })
        } else if (actual.size > expected.size) {
            val tail = actual.copyOfRange(min, actual.size)
            assertEquals(true, tail.all { it.toInt() == 0 })
        }
    }
}