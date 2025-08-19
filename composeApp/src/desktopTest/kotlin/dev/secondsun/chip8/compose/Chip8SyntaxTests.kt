package dev.secondsun.chip8.compose


import dev.secondsun.chip8.compose.assembler.ParsedToken
import dev.secondsun.chip8.compose.assembler.ParsedTokenType
import dev.secondsun.chip8.compose.assembler.Token
import dev.secondsun.chip8.compose.assembler.parse
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.test.junit5.JUnit5Asserter.fail

/**
 * Tests for the Chip-8 assembly syntax. This includes constant experssions, directive arguments,
 * and simple types. The output of syntax analysis is a (mostly linear) parse tree.
 */
class Chip8SyntaxTests {

    @Test
    fun defineMonitor() {
        val program = """
            :monitor foobar 10                 # 10 bytes of RAM, starting at a label "foobar"
            :monitor v3 4                      # registers v3-v6, shown as a block of hex digits
            :monitor quux "x:%2i y:%2i z:%2i"  # a 3-component vector, starting at label "quux", where each digit is 2 bytes
        """.trimIndent()

        val parsed = parse(program)
        assertEquals(3 , parsed.parsedTokens.size )
        assertEquals(ParsedTokenType.Monitor, parsed.parsedTokens.first().type)

        val tokens = parsed.parsedTokens

        assertTrue( tokens[0].tokens[1] is Token.Identifier)
        assertTrue( tokens[1].tokens[1] is Token.Register)
        assertTrue( tokens[2].tokens[1] is Token.Identifier)


        assertTrue( tokens[0].tokens[2] is Token.Number)
        assertTrue( tokens[1].tokens[2] is Token.Number)
        assertTrue( tokens[2].tokens[2] is Token.StringToken)

    }

    @Test
    fun defineLabel() {
        val program = """
            : main
        """.trimIndent()
        val context = parse(program)
        val parsed = context.parsedTokens
        assertEquals(1 , parsed.size )
        assertEquals(ParsedTokenType.Label, parsed[0].type)
    }

    @Test
    fun `can only define label once`() {
        val program = """
            : main
            : main
        """.trimIndent()
        val context = parse(program)
        val parsed = context.parsedTokens
        assertEquals(2 , parsed.size )
        assertEquals(ParsedTokenType.Error, parsed[1].type)

    }


    @Test
    fun testUnpack() {
        val program = """
            :unpack long 0xaa
            :unpack 0xa
            :unpack :const
        """.trimIndent()
        val context = parse(program)
        val parsed = context.parsedTokens
        assertEquals(3 , parsed.size )
        assertEquals(3 , parsed[0].tokens.size )
        assertEquals(ParsedTokenType.Unpack , parsed[0].type )
        assertEquals(2 , parsed[1].tokens.size )
        assertEquals(ParsedTokenType.Unpack , parsed[1].type )
        assertEquals(ParsedTokenType.Error , parsed[2].type )
    }

    /**
     * Numeric constants can be defined with the :const directive followed by a name and
     * then a value, which may be a number, another constant or a (non forward-declared) label.
     */
    @Test
    fun testConstants() {
        val program = """
            : main
                :const FIVE 5
                :const MAIN_LABEL main
                :const FIVE_CONST FIVE
        """.trimIndent()

        val context = parse(program)
        val parsed = context.parsedTokens
        assertEquals(4 , parsed.size )
        assertEquals(ParsedTokenType.Constant, parsed[1].type)
        assertEquals(ParsedTokenType.Constant, parsed[2].type)
        assertEquals(ParsedTokenType.Constant, parsed[3].type)

        assertEquals(5, context.constants["FIVE"]?.evaluate())
        assertEquals(5, context.constants["FIVE_CONST"]?.evaluate())



    }



    @Test
    fun testRegisterBreakpoint() {
        val program = """
            :breakpoint example-breakpoint
        """.trimIndent()
        val parsed = parse(program)
        assertEquals(1 , parsed.parsedTokens.size )
        assertEquals(ParsedTokenType.Breakpoint, parsed.parsedTokens[0].type)
        assertEquals("example-breakpoint", (parsed.parsedTokens[0].tokens[1] as Token.Identifier).name)
    }
    /**
     * Registers may be given named aliases with :alias followed by a name and then a
     * register or a constant expression 0-15 enclosed in curly braces ({ ... }).
     * The i register may not be given an alias, but v registers can be given as many aliases as desired.
     */
    @Test
    fun testRegisterAliases() {
        val program = """
            :alias x v0
            :alias CARRY_FLAG vF
            :alias NTH { 3 + CALLS }
            :alias eye i
            :alias toobig 16
        """.trimIndent()

        val parsed = parse(program)
        val tokens = parsed.parsedTokens

        assertEquals(5, tokens.size)
        assertTrue(tokens[0].tokens[2] is Token.Register)
        assertEquals(ParsedTokenType.Alias, tokens[0].type)

        assertTrue(tokens[1].tokens[2] is Token.Register)
        assertTrue(tokens[2].tokens[2] is Token.LBrace)
        assertEquals(ParsedTokenType.Error, tokens[3].type)
        assertEquals(ParsedTokenType.Error, tokens[4].type)

    }
}