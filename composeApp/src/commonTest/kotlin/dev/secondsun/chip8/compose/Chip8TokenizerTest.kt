package dev.secondsun.chip8.compose

import dev.secondsun.chip8.compose.assembler.Token
import dev.secondsun.chip8.compose.assembler.tokenize
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue


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
}