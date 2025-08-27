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
}