package dev.secondsun.chip8.compose


import org.junit.jupiter.api.Test
import kotlin.test.junit5.JUnit5Asserter.fail

class Chip8DirectiveTests {

    /**
     * Numeric constants can be defined with the :const directive followed by a name and
     * then a value, which may be a number, another constant or a (non forward-declared) label.
     */
    @Test
    fun testNumericConstants() {
        val program = """
            : main
                :const FIVE 5
                :const MAIN_LABEL main
                :const FIVE_CONST FIVE
        """.trimIndent()
        fail("Not yet implemented")
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
        """.trimIndent()
        fail("Not yet implemented")
    }
}