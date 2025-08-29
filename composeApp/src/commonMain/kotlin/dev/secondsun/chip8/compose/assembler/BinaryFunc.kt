package dev.secondsun.chip8.compose.assembler

import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

enum class BinaryFunc {
    SUBTRACT {
        override fun apply(x: Double, y: Double): Double = x - y
        override fun match(token: Token): Boolean = token is Token.Minus
    },
    ADD {
        override fun apply(x: Double, y: Double): Double = x + y
        override fun match(token: Token): Boolean = token is Token.Plus
    },
    MULTIPLY {
        override fun apply(x: Double, y: Double): Double = x * y
        override fun match(token: Token): Boolean = token is Token.Multiply
    },
    DIVIDE {
        override fun apply(x: Double, y: Double): Double = x / y
        override fun match(token: Token): Boolean = token is Token.Divide
    },
    MOD {
        override fun apply(x: Double, y: Double): Double = x % y
        // Fallback to identifier "%" if a dedicated token isn't present
        override fun match(token: Token): Boolean = token is Token.Percent
    },
    BIT_AND {
        override fun apply(x: Double, y: Double): Double = (x.toLong() and y.toLong()).toDouble()
        override fun match(token: Token): Boolean = token is Token.BinaryAnd
    },
    BIT_OR {
        override fun apply(x: Double, y: Double): Double = (x.toLong() or y.toLong()).toDouble()
        override fun match(token: Token): Boolean = token is Token.BinaryOr
    },
    BIT_XOR {
        override fun apply(x: Double, y: Double): Double = (x.toLong() xor y.toLong()).toDouble()
        // Fallback to identifier "^" if a dedicated token isn't present
        override fun match(token: Token): Boolean = token is Token.Caret
    },
    SHL {
        override fun apply(x: Double, y: Double): Double = (x.toLong() shl y.toInt()).toDouble()
        // Fallback to identifier "<<" if a dedicated token isn't present
        override fun match(token: Token): Boolean = token is Token.ShiftLeft
    },
    SHR {
        override fun apply(x: Double, y: Double): Double = (x.toLong() shr y.toInt()).toDouble()
        // Fallback to identifier ">>" if a dedicated token isn't present
        override fun match(token: Token): Boolean = token is Token.ShiftRight
    },
    POW {
        override fun apply(x: Double, y: Double): Double = x.pow(y)
        override fun match(token: Token): Boolean = token is Token.Identifier && token.name == "pow"
    },
    MIN {
        override fun apply(x: Double, y: Double): Double = min(x, y)
        override fun match(token: Token): Boolean = token is Token.Identifier && token.name == "min"
    },
    MAX {
        override fun apply(x: Double, y: Double): Double = max(x, y)
        override fun match(token: Token): Boolean = token is Token.Identifier && token.name == "max"
    },
    LT {
        override fun apply(x: Double, y: Double): Double = if (x < y) 1.0 else 0.0
        override fun match(token: Token): Boolean = token is Token.LessThan
    },
    GT {
        override fun apply(x: Double, y: Double): Double = if (x > y) 1.0 else 0.0
        override fun match(token: Token): Boolean = token is Token.GreaterThan
    },
    LTE {
        override fun apply(x: Double, y: Double): Double = if (x <= y) 1.0 else 0.0
        override fun match(token: Token): Boolean = token is Token.LessThanOrEqual
    },
    GTE {
        override fun apply(x: Double, y: Double): Double = if (x >= y) 1.0 else 0.0
        override fun match(token: Token): Boolean = token is Token.GreaterThanOrEqual
    },
    EQ {
        override fun apply(x: Double, y: Double): Double = if (x == y) 1.0 else 0.0
        override fun match(token: Token): Boolean = token is Token.Equal
    },
    NEQ {
        override fun apply(x: Double, y: Double): Double = if (x != y) 1.0 else 0.0
        override fun match(token: Token): Boolean = token is Token.NotEqual
    };

    abstract fun match(token: Token): Boolean
    abstract fun apply(x: Double, y: Double): Double
}