package dev.secondsun.chip8.compose

import dev.secondsun.chip8.compose.assembler.parse
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class Chip8AssemblerTests {

    @Test
    fun `labels get addresses`() {
        val program = """
            : main
        }""".trimIndent()

        val context = parse(program)
        assertEquals(0x200, context.labels["main"])
    }

    @Test
    fun `can assert`() {
        TODO()
    }

}