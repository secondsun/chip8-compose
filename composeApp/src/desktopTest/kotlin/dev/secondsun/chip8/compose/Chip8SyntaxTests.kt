package dev.secondsun.chip8.compose


import chip8_compose.composeapp.generated.resources.Res
import dev.secondsun.chip8.compose.assembler.ParsedConditionalToken
import dev.secondsun.chip8.compose.assembler.ParsedMacroExpandToken
import dev.secondsun.chip8.compose.assembler.ParsedToken
import dev.secondsun.chip8.compose.assembler.ParsedTokenType
import dev.secondsun.chip8.compose.assembler.Token
import dev.secondsun.chip8.compose.assembler.parse
import dev.secondsun.chip8.compose.assembler.tokenize
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.net.URI
import java.nio.file.Paths
import kotlin.test.assertFalse
import kotlin.test.assertNotNull

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
        assertEquals(3, parsed.parsedTokens.size)
        assertEquals(ParsedTokenType.Monitor, parsed.parsedTokens.first().type)

        val tokens = parsed.parsedTokens

        assertTrue(tokens[0].tokens[1] is Token.Identifier)
        assertTrue(tokens[1].tokens[1] is Token.Register)
        assertTrue(tokens[2].tokens[1] is Token.Identifier)


        assertTrue(tokens[0].tokens[2] is Token.Number)
        assertTrue(tokens[1].tokens[2] is Token.Number)
        assertTrue(tokens[2].tokens[2] is Token.StringToken)

    }

    @Test
    fun defineLabel() {
        val program = """
            : main
        """.trimIndent()
        val context = parse(program)
        val parsed = context.parsedTokens
        assertEquals(1, parsed.size)
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
        assertEquals(2, parsed.size)
        assertEquals(ParsedTokenType.Error, parsed[1].type)

    }


    @Test
    fun `simple macro test`() {
        val program = """
         
            :macro steps-reset-level { copy-2 step-total step-level }
            :macro steps-next-level  { copy-2 step-level step-total }
        
        """.trimIndent()

        val parsed = parse(program)
        val tokens = parsed.parsedTokens

        assertEquals(2, tokens.size)
        assertEquals(ParsedTokenType.Macro, tokens[0].type)
        assertEquals(ParsedTokenType.Macro, tokens[1].type)

        assertTrue(parsed.macros["steps-reset-level"]!!.body.first() is Token.LBrace)
        assertEquals(7, parsed.macros["steps-reset-level"]!!.tokens.size)
        assertEquals(5, parsed.macros["steps-reset-level"]!!.body.size)


    }


    @Test
    fun `basic test of string mode`() {
        val program = """
            :stringmode print "ADEGHILMNOPRSTVY" {
            	:calc S { let-A + VALUE * 4 }
            	i := S
            	sprite v0 v1 4
            	# compute the width of each glyph statically:
            	:calc p { ( @ S ) | ( @ 1 + S ) | ( @ 2 + S ) | ( @ 3 + S ) }
            	:calc w { 9 - ( log p & - p ) / log 2 }
            	v0 += w
            }
        """.trimIndent()

        val parsed = parse(program)

        assertEquals(1, parsed.parsedTokens.size)
        assertNotNull(parsed.stringModes["print"])

    }


    @Test
    fun `string mode should only define a character once`() {
        val program = """
            :stringmode print "AAD" {}
        """.trimIndent()

        val parsed = parse(program)
        assertEquals(1, parsed.parsedTokens.size)
        assertEquals(ParsedTokenType.Error, parsed.parsedTokens[0].type)
        assertTrue(parsed.parsedTokens[0].tokens[2] is Token.Error)

    }

    @Test
    fun `calc can be reassigned`() {
        val program = """
            :calc S { 42 }
            :calc S { 47 }
        """.trimIndent()

        val pased = parse(program)
        assertEquals(2, pased.parsedTokens.size)
        assertEquals(ParsedTokenType.Calc, pased.parsedTokens[0].type)
        assertEquals(ParsedTokenType.Calc, pased.parsedTokens[1].type)
        assertEquals(47, pased.mutables["S"]?.evaluate())
    }

    @Test
    fun `parse if-then statements`() {
        val program = """
            		if v0 == 64 then v1 += 1
        """.trimIndent()

        val parsed = parse(program)

        assertEquals(2, parsed.parsedTokens.size)
        assertEquals(ParsedTokenType.If, parsed.parsedTokens[0].type)
        val ifToken = parsed.parsedTokens[0] as ParsedConditionalToken
        val conditionalTokens = ifToken.condition[0].tokens
        val bodyTokens = parsed.parsedTokens[1].tokens

        assertEquals(3, conditionalTokens.size)
        assertEquals(3, bodyTokens.size)

        assertTrue(conditionalTokens[0] is Token.Register)
        assertTrue(conditionalTokens[1] is Token.Equal)
        assertTrue(conditionalTokens[2] is Token.Number)

        assertTrue(bodyTokens[0] is Token.Register)
        assertTrue(bodyTokens[1] is Token.AdditionAssignment)
        assertTrue(bodyTokens[2] is Token.Number)


    }

    fun `parse i assign`() {
        """
            i := long 42
        """.trimIndent()
        TODO()
    }

    @Test
    fun `parse call from label and return`() {
        val program = """
            : shuffle-base  v0 := 0 ; # SMC
            : main
              shuffle-base
        """.trimIndent()
        val parsed = parse(program)
        assertEquals(5, parsed.parsedTokens.size)
        val label = parsed.parsedTokens[0]
        val call = parsed.parsedTokens[4]
        assertEquals(ParsedTokenType.Label, label.type)
        assertEquals(ParsedTokenType.Call, call.type)
        assertEquals("shuffle-base", (call.tokens[0] as Token.Identifier).name)
    }

    @Test
    fun `parse if-begin statements`() {
        val program = """
        : shuffle-base v0 := 0 ;
        if v3 != v4 begin
			# swap a[v4] / a[v3]...
			shuffle-base
			i += v3
			load v1 - v1 # has a[v3]
			shuffle-base
			i += v4
			load v2 - v2 # has a[v4]
			save v1 - v1
			shuffle-base
			i += v3
			save v2 - v2
		end
        """.trimIndent()

        val parsed = parse(program)
        assertEquals(15, parsed.parsedTokens.size)
        assertTrue { parsed.parsedTokens.none { it.type == ParsedTokenType.Error } }


    }

    @Test
    fun `parse nested if statements`() {
        val program = """
            if v0 == 0 begin
				if v1 key begin
					v0 := 1
					i := 42
				end
			else
				if v1 -key begin
					v0 := 0
					i := 42
				end
			end
        """.trimIndent()

        val parsed = parse(program)
        assertEquals(11, parsed.parsedTokens.size)
        parsed.parsedTokens[0] as ParsedConditionalToken
        assertTrue { parsed.parsedTokens.none { it.type == ParsedTokenType.Error } }

    }

    @Test
    fun `parse if begin else statements`() {
        val program = """
            if v0 == 0x2 begin
                v1 += 2
            else
                v2 := 0x2
                v3 := -32
            end
        """.trimIndent()

        val parsed = parse(program)
        assertEquals(6, parsed.parsedTokens.size)
        assertTrue { parsed.parsedTokens.none { it.type == ParsedTokenType.Error } }
        assertTrue { parsed.parsedTokens[2].type == ParsedTokenType.Else }
        assertTrue { parsed.parsedTokens[5].type == ParsedTokenType.End }


    }

    @Test
    fun `string mode should allow additions to the alphabet`() {


        val program = """
            :stringmode print " " {
            	v0 += 4
            }
            
            :stringmode print "ADEGHILMNOPRSTVY" {
            	:calc S { let-A + VALUE * 4 }
            	i := S
            	sprite v0 v1 4
            	# compute the width of each glyph statically:
            	:calc p { ( @ S ) | ( @ 1 + S ) | ( @ 2 + S ) | ( @ 3 + S ) }
            	:calc w { 9 - ( log p & - p ) / log 2 }
            	v0 += w
            }
        """.trimIndent()

        val parsed = parse(program)

        assertEquals(2, parsed.parsedTokens.size)
        assertNotNull(parsed.stringModes["print"])

    }


    @Test
    fun testMultiLineMacros() {
        val program = """
         
            :macro steps-show-digits SRC {
            	i := SRC
            	load v0
            	i := step-bcd
            	bcd v0
            	load v2
            	i := hex v1
            	sprite v3 v4 5
            	v3 += 5
            	i := hex v2
            	sprite v3 v4 5
            	v3 += 5
            }
        """

        val parsed = parse(program)
        val tokens = parsed.parsedTokens

        assertEquals(1, tokens.size)
        assertTrue(parsed.macros["steps-show-digits"]!!.tokens[1] is Token.Identifier)
        assertEquals("SRC", (parsed.macros["steps-show-digits"]!!.params.first() as Token.Identifier).name)
        assertTrue { parsed.macros["steps-show-digits"]!!.body.last() is Token.RBrace }
        assertFalse { parsed.macros["steps-show-digits"]!!.tokens.any { it is Token.Error } }
    }

    @Test
    fun testMacroWithCommentAndNestedRBrace() {
        val program = """
            :macro steps-show { # at v3,v4
            	steps-show-digits step-level
            	:calc low-digits { 1 + step-level }
            	steps-show-digits low-digits
            }
        """

        val parsed = parse(program)
        val tokens = parsed.parsedTokens
        assertTrue(parsed.macros["steps-show"]!!.body.first() is Token.LBrace)
        assertTrue(parsed.macros["steps-show"]!!.body.last() is Token.RBrace)
        assertEquals(15, parsed.macros["steps-show"]!!.tokens.size)


        assertEquals(ParsedTokenType.Macro, tokens[0].type)
        assertTrue(parsed.macros["steps-show"]!!.body.last() is Token.RBrace)

        assertEquals(1, tokens.size)
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
        assertEquals(3, parsed.size)
        assertEquals(3, parsed[0].tokens.size)
        assertEquals(ParsedTokenType.Unpack, parsed[0].type)
        assertEquals(2, parsed[1].tokens.size)
        assertEquals(ParsedTokenType.Unpack, parsed[1].type)
        assertEquals(ParsedTokenType.Error, parsed[2].type)
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
        assertEquals(4, parsed.size)
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
        assertEquals(1, parsed.parsedTokens.size)
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

    @Test
    fun `loop again test`() {
        val program = """
                loop
                    sprite v0 v1 1
                    i  += v3
                    v2 += 1
                    v0 += 8
                    if v0 == 64 then v1 += 1
                    if v0 == 64 then v0 := 0
                    if v1 != 32 then
                again
        """.trimIndent()

        val parsed = parse(program)
        assertEquals(11, parsed.parsedTokens.size)
        assertEquals(ParsedTokenType.Loop, parsed.parsedTokens[0].type)
        assertTrue(parsed.parsedTokens.none { it.type == ParsedTokenType.Error })
    }

    @Test
    fun `loop while again test`() {
        val program = """
            loop
		    while v0 != 1
                v0 += -1
                vf := 76
                i  += vf
            again
        """.trimIndent()
        val parsed = parse(program)
        assertEquals(6, parsed.parsedTokens.size)
        assertEquals(ParsedTokenType.Loop, parsed.parsedTokens[0].type)
        assertEquals(ParsedTokenType.While, parsed.parsedTokens[1].type)
        assertEquals(ParsedTokenType.Again, parsed.parsedTokens[5].type)
        assertTrue(parsed.parsedTokens.none { it.type == ParsedTokenType.Error })

    }

    @Test
    fun `nested loop again test`() {
        val program = """
            	loop
            		i := 0x42
            		i += v3
            		i += v4
            		load v0
            		v5 := v0 # column
            		v2 := 0  # y tile
            		

            		loop
            			i := 0x444
            			v0 := random 0b1100
            			i += v0
            			v5 >>= v5
            			if vf != 0 then i := 0x42
            			sprite v1 v2 4
            			v2 += 4
            			if v2 != 32 then
            		again

            		v1 += 4
            		v4 += 1
            		if v4 != 16 then
            	again
        """.trimIndent()
        val parsed = parse(program)
        assertEquals(23, parsed.parsedTokens.size)
        assertTrue(parsed.parsedTokens.none { it.type == ParsedTokenType.Error })
    }

    @Test
    fun `test many more things`() {
        TODO("Test scrolls, test planes, test iAssign, test raw number values, test macro expand...")
    }

    @Test
    fun `parse macros`() {
        val program = """
            :macro foo SIZE {
                v0 := SIZE
                i := 0x42
            }
            foo 0x42
        """

        val parsed = parse(program)
        assertEquals(2, parsed.parsedTokens.size)
        assertEquals(ParsedTokenType.Macro, parsed.parsedTokens[0].type)
        val macroToken = parsed.parsedTokens[1] as ParsedMacroExpandToken
        assertEquals(1, macroToken.params.size)
        assertTrue(macroToken.params[0] is Token.Number)

    }


    @Test
    fun `parse macros should error on bad params`() {
        val program = """
            :macro foo SIZE {
                v0 := SIZE
                i := 0x42
            }
            foo #missing param
        """

        val parsed = parse(program)
        assertEquals(2, parsed.parsedTokens.size)
        assertEquals(ParsedTokenType.Error, parsed.parsedTokens[1].type)


    }

    @Test
    fun `big file should parse without errors`() {

            val programUri = URI.create(Res.getUri("files/big.8o"))
            val program = Paths.get(programUri).toFile().readText()
            val tokens = parse(program)
            tokens.parsedTokens.filter {it.type == ParsedTokenType.Error}.forEach { println(it); println(it.tokens.joinToString(" ") { it.toString()})}
            assertTrue { tokens.parsedTokens.none { it.type ==  ParsedTokenType.Error } }

    }

}