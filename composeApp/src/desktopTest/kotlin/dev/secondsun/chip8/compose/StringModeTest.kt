package dev.secondsun.chip8.compose

import dev.secondsun.chip8.compose.assembler.StringMode
import dev.secondsun.chip8.compose.assembler.tokenize
import kotlin.test.Test
import kotlin.test.assertEquals

class StringModeTest {

    @Test
    fun `string mode should only define a character once`() {
TODO()
    }

    @Test
    fun testStringMode() {
        val stringMode = StringMode("paint")

        val program = """
            
            	v0 += 4
            
        """.trimIndent()

        val program2 = """
            
            	:calc S { let-A + VALUE * 4 }
            
        """.trimIndent()

        val tokens = tokenize(program)
        val tokens2 = tokenize(program2)

        stringMode.addAlphabet(" ", tokens)
        stringMode.addAlphabet("ADEGHILMNOPRSTVY", tokens2)

        val replcaedTokens = stringMode.evaluate("S")
        assertEquals(9, replcaedTokens.size)

    }

    @Test
    fun `string mode should throw exception  is undefined character is used`() {
        TODO($$"throw `String mode '${token}' is not defined for the character '${char}'.`;\n")
    }

    @Test
    fun `test constants CALL, VALUE, INDEX, and CHAR`() {
        val macroBody = """
            	:calc S { let-A + CALL * 4 }
                :calc S { let-A + VALUE * 4 }
                :calc S { let-A + INDEX * 4 }
                :calc S { let-A + CHAR * 4 }
        """.trimIndent()

        val tokens = tokenize(macroBody)
        val stringMode = StringMode("paint")
        stringMode.addAlphabet(" ", tokens)

        val replacedTokens = stringMode.evaluate(" ")


    }

}