package dev.secondsun.chip8.compose.assembler

import java.math.BigInteger
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.floor
import kotlin.math.ln
import kotlin.math.sign
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tan

enum class UnaryFunc {
    MINUS {
        override fun apply(value: Double): Double {
            return -value
        }
        override fun match(token: Token): Boolean {
            return token is Token.Minus
        }
    },
    TILDE {
        override fun apply(value: Double): Double {
            return BigInteger.valueOf(value.toLong()).not().toDouble()
        }

        override fun match(token: Token): Boolean {
            return token is Token.Tilde
        }
    },
    EXCLAMATION {
        override fun apply(value: Double): Double {
            return if (value == 0.0) 1.0 else 0.0
        }

        override fun match(token: Token): Boolean {
            return token is Token.Exclaimation
        }
    },
    SIN {
        override fun apply(value: Double): Double {
            return sin(value)
        }

        override fun match(token: Token): Boolean {
            return token is Token.Identifier && token.name == "sin"
        }
    },
    COS {
        override fun apply(value: Double): Double {
            return cos(value)
        }

        override fun match(token: Token): Boolean {
            return token is Token.Identifier && token.name == "cos"
        }
    },
    TAN {
        override fun apply(value: Double): Double {
            return tan(value)
        }

        override fun match(token: Token): Boolean {
            return token is Token.Identifier && token.name == "tan"
        }
    },
    EXP {
        override fun apply(value: Double): Double {
            return exp(value)
        }

        override fun match(token: Token): Boolean {
            return token is Token.Identifier && token.name == "exp"
        }
    },
    LOG {
        override fun apply(value: Double): Double {
            return ln(value)
        }

        override fun match(token: Token): Boolean {
            return token is Token.Identifier && token.name == "log"
        }
    },
    ABS {
        override fun apply(value: Double): Double {
            return abs(value)
        }

        override fun match(token: Token): Boolean {
            return token is Token.Identifier && token.name == "abs"
        }
    },
    SQRT {
        override fun apply(value: Double): Double {
            return sqrt(value)
        }

        override fun match(token: Token): Boolean {
            return token is Token.Identifier && token.name == "sqrt"
        }
    },
    SIGN {
        override fun apply(value: Double): Double {
            return sign(value)
        }

        override fun match(token: Token): Boolean {
            return token is Token.Identifier && token.name == "sign"
        }
    },
    CEIL {
        override fun apply(value: Double): Double {
            return ceil(value)
        }

        override fun match(token: Token): Boolean {
            return token is Token.Identifier && token.name == "ceil"
        }
    },
    FLOOR {
        override fun apply(value: Double): Double {
            return floor(value)
        }

        override fun match(token: Token): Boolean {
            return token is Token.Identifier && token.name == "floor"
        }
    },
    AT {
        override fun match(token: Token): Boolean {
            return token is Token.At
        }

        override fun apply(value: Double): Double {
            throw NotImplementedError("AT is not implemented")
        }

        fun apply(value: Int, mem: ByteArray): Int {
            return mem[(0 or value) - 0x200].toInt()
        }

    };

    abstract fun match(token: Token): Boolean;
    abstract fun apply(value: Double): Double;
}