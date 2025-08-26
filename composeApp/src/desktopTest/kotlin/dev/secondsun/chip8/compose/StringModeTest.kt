package dev.secondsun.chip8.compose

import dev.secondsun.chip8.compose.assembler.StringMode
import dev.secondsun.chip8.compose.assembler.Token
import dev.secondsun.chip8.compose.assembler.tokenize
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import kotlin.test.Test
import kotlin.test.assertEquals

class StringModeTest {

    @Test
    fun `string mode should only define a character once`() {
        val stringMode = StringMode("paint")
        assertThrows<IllegalStateException>  {stringMode.addAlphabet("ACA", listOf<Token>()) }


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
        val macroBody = """
            	:calc S { let-A + VALUE * 4 }
        """.trimIndent()

        val tokens = tokenize(macroBody)
        val stringMode = StringMode("paint")
        stringMode.addAlphabet("PCA", tokens)

        assertThrows<IllegalStateException>  { stringMode.evaluate("Z") }

    }

    @ParameterizedTest
    @CsvSource(value = ["CALLS:1", "VALUE:2", "INDEX:1", "CHAR:65"], delimiter = ':')
    fun `test constants CALLS, VALUE, INDEX, and CHAR`(method:String, expected:String) {
        val macroBody = """
            	:calc S { let-A + ${method} * 4 }
        """.trimIndent()

        val tokens = tokenize(macroBody)
        val stringMode = StringMode("paint")
        stringMode.addAlphabet("PCA", tokens)

        val replacedTokens = stringMode.evaluate("CA")
        assertTrue( replacedTokens[14] is Token.Number)
        assertEquals(expected, (replacedTokens[14] as Token.Number).value.toString())


    }

}