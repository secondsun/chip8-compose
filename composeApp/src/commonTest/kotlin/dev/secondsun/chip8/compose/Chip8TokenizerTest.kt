package dev.secondsun.chip8.compose

import dev.secondsun.chip8.compose.assembler.Token
import dev.secondsun.chip8.compose.assembler.tokenize
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.fail


class Chip8TokenizerTest {

    /**
     * Tests that oops all whitespace does not crash
     */
    @Test
    fun testWhitespaceAbuse() {
        val program = """
            : main
            
              
               
                
                 
                  
                   
                    
                     
                      
            
        """.trimIndent()

        tokenize(program)

    }

    @Test
    fun testTokenizeLabelDirective() {
        val program = """
            : main
        """.trimIndent()


        val tokens = tokenize(program)
        assertEquals(2, tokens.size)
        assertTrue { tokens[0] is Token.Colon }
        assertEquals(0, tokens[0].line)
        assertEquals(0, tokens[0].column)

        assertTrue { tokens[1] is Token.Identifier }
        assertEquals(0, tokens[1].line)
        assertEquals(2, tokens[1].column)
        assertEquals("main", (tokens[1] as Token.Identifier).name)

    }


    /**
     * Per the spec, "Using a label by itself will perform a subroutine call to the address the label represents."
     *
     * So just a label should tokenize as an identifier.
     */
    @Test
    fun testTokenizeLabel() {

        //This program calls main recursively until it exceeds up the stack and crashes
        val program = """
            : main
            main 
        """.trimIndent()

        val token = tokenize(program)[2]
        assertTrue { token is Token.Identifier }
        assertEquals(1, token.line)
        assertEquals(0, token.column)
        assertEquals("main", (token as Token.Identifier).name)
    }

    /**
     * Alternatively, you can be more explicit by using :call followed by an address or name.
     */
    @Test
    fun testTokenizeLabelWithCall() {
        val program = """
            : main
            :call main
        """.trimIndent()
        val tokens = tokenize(program)
        assertEquals(4, tokens.size)
        val callToken = tokens[2]
        assertTrue { callToken is Token.CallDirective }

    }

    /**
     * A semicolon (;) is another way to write return, which returns from a subroutine.
     */
    @Test
    fun testTokenizeReturn() {
        val program = """
            : main
            return
        """.trimIndent()
        val tokens = tokenize(program)
        assertEquals(3, tokens.size)
        val returnToken = tokens[2]
        assertTrue { returnToken is Token.Return }
    }

    /**
     * The # directive is a single-line comment; it ignores the rest of the current line.
     */
    @Test
    fun testComments() {
        val program = """
            : main # This is a comment
            main
            
        """.trimIndent()
        val tokens = tokenize(program)
        assertEquals(3, tokens.size)
    }

    /**
     * Numbers can be written using 0x or 0b prefixes to indicate hexadecimal or binary encodings, respectively.
     */
    @Test
    fun testNumbers() {
        val program = """
            : main
            v1 := 0x82
            v2 := 0b10101010
        """.trimIndent()
        val tokens = tokenize(program)
        assertEquals(6, tokens.size)
    }
}