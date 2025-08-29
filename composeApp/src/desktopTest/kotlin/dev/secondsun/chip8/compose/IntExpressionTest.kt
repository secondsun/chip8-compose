package dev.secondsun.chip8.compose

import dev.secondsun.chip8.compose.assembler.IntExpression
import dev.secondsun.chip8.compose.assembler.tokenize
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class IntExpressionTest {
    @Test
    fun `test simple expressions`() {
        var expression = IntExpression(tokenize("{ 42 }"))
        assertEquals(42, expression.evaluate())

        expression = IntExpression(tokenize("{ ( 42 ) }"))
        assertEquals(42, expression.evaluate())

        expression = IntExpression(tokenize("{ 40 + 2 }"))
        assertEquals(42, expression.evaluate())
    }
    
    @Test
    fun `test arithmetic operators`() {
        var expression = IntExpression(tokenize("{ 5 + 7 }"))
        assertEquals(12, expression.evaluate())

        expression = IntExpression(tokenize("{ 5 - 7 }"))
        assertEquals(-2, expression.evaluate())

        expression = IntExpression(tokenize("{ 6 * 7 }"))
        assertEquals(42, expression.evaluate())

        // Integer division semantics via toInt() truncation
        expression = IntExpression(tokenize("{ 5 / 2 }"))
        assertEquals(2, expression.evaluate())

        expression = IntExpression(tokenize("{ 7 % 5 }"))
        assertEquals(2, expression.evaluate())
    }

    @Test
    fun `test bitwise and shift operators`() {
        var expression = IntExpression(tokenize("{ 6 & 3 }"))
        assertEquals(2, expression.evaluate())

        expression = IntExpression(tokenize("{ 6 | 3 }"))
        assertEquals(7, expression.evaluate())

        expression = IntExpression(tokenize("{ 6 ^ 3 }"))
        assertEquals(5, expression.evaluate())

        expression = IntExpression(tokenize("{ 1 << 3 }"))
        assertEquals(8, expression.evaluate())

        expression = IntExpression(tokenize("{ 8 >> 2 }"))
        assertEquals(2, expression.evaluate())
    }

    @Test
    fun `test comparisons`() {
        var expression = IntExpression(tokenize("{ 2 < 3 }"))
        assertEquals(1, expression.evaluate())

        expression = IntExpression(tokenize("{ 3 > 2 }"))
        assertEquals(1, expression.evaluate())

        expression = IntExpression(tokenize("{ 3 <= 3 }"))
        assertEquals(1, expression.evaluate())

        expression = IntExpression(tokenize("{ 2 >= 3 }"))
        assertEquals(0, expression.evaluate())

        expression = IntExpression(tokenize("{ 3 == 3 }"))
        assertEquals(1, expression.evaluate())

        expression = IntExpression(tokenize("{ 3 != 4 }"))
        assertEquals(1, expression.evaluate())
    }

    @Test
    fun `test parentheses and precedence`() {
        var expression = IntExpression(tokenize("{ ( 2 + 3 ) * 4 }"))
        assertEquals(20, expression.evaluate())

        expression = IntExpression(tokenize("{ 2 + ( 3 * 4 ) }"))
        assertEquals(14, expression.evaluate())
    }

    @Test
    fun `test numeric bases`() {
        var expression = IntExpression(tokenize("{ 0xff + 0b1 }"))
        assertEquals(256, expression.evaluate())

        expression = IntExpression(tokenize("{ 010 + 1 }")) // octal 8 + 1
        assertEquals(9, expression.evaluate())
    }

    @Test
    fun `test unary operators`() {
        var expression = IntExpression(tokenize("{ - 3 }"))
        assertEquals(-3, expression.evaluate())

        expression = IntExpression(tokenize("{ ~ 0 }"))
        assertEquals(-1, expression.evaluate())

        expression = IntExpression(tokenize("{ ~ 0xff }"))
        assertEquals(-256, expression.evaluate())
    }

    @Test
    fun `test unary functions`() {
        var expression = IntExpression(tokenize("{ abs -5 }"))
        assertEquals(5, expression.evaluate())

        expression = IntExpression(tokenize("{ sqrt ( 9 ) }"))
        assertEquals(3, expression.evaluate())

        expression = IntExpression(tokenize("{ floor ( 2 ) }"))
        assertEquals(2, expression.evaluate())

        expression = IntExpression(tokenize("{ ceil ( 3 ) }"))
        assertEquals(3, expression.evaluate())

        expression = IntExpression(tokenize("{ sign -7 }"))
        assertEquals(-1, expression.evaluate())

        expression = IntExpression(tokenize("{ sin ( 0 ) }"))
        assertEquals(0, expression.evaluate())

        expression = IntExpression(tokenize("{ cos ( 0 ) }"))
        assertEquals(1, expression.evaluate())

        expression = IntExpression(tokenize("{ tan ( 0 ) }"))
        assertEquals(0, expression.evaluate())

        expression = IntExpression(tokenize("{ exp ( 0 ) }"))
        assertEquals(1, expression.evaluate())

        expression = IntExpression(tokenize("{ log ( 1 ) }"))
        assertEquals(0, expression.evaluate())
    }

    @Test
    fun `test min and max functions`() {
        var expression = IntExpression(tokenize("{ 2 max 3 }"))
        assertEquals(3, expression.evaluate())

        expression = IntExpression(tokenize("{ 2 min  3 }"))
        assertEquals(2, expression.evaluate())
    }
}