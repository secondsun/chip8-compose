package dev.secondsun.chip8.compose.assembler

import java.math.BigInteger
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.floor
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sign
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tan

data class IntExpression(val expression: List<Token>) {
    fun evaluate(): Int {
        if (expression.isEmpty()) {
            return 0
        }

        if (expression.size == 1) {
            val token = expression[0]
            when(token) {
                is Token.Number -> return token.value
                is Token.Identifier -> {TODO()}
                is Token.Register -> return token.register.ordinal
                else -> {}
            }
        }

        if (expression[0] is Token.LBrace || expression[0] is Token.LParen) {
            return parseExpression(expression.toMutableList())
        }

        throw Exception("Unable to evaluate expression")

    }

    private fun parseCalc(inExpression: MutableList<Token>) : Double {
        val first = inExpression[0]
        if (first is Token.Identifier) {
            return when (first.name) {
                "strlen" -> {inExpression.removeFirst();(inExpression.removeFirst() as Token.StringToken).value.length.toDouble()}
                "sin" -> {
                    inExpression.removeFirst()
                    sin(parseCalc(inExpression))
                }
                "cos" -> {
                    inExpression.removeFirst()
                    cos(parseCalc(inExpression))
                }
                "tan" -> {
                    inExpression.removeFirst()
                    tan(parseCalc(inExpression))
                }
                "exp" -> {
                    inExpression.removeFirst()
                    exp(parseCalc(inExpression))
                }
                "log" -> {
                    inExpression.removeFirst()
                    ln(parseCalc(inExpression))
                }
                "abs" -> {
                    inExpression.removeFirst()
                    abs(parseCalc(inExpression))
                }
                "sqrt" -> {
                    inExpression.removeFirst()
                    sqrt(parseCalc(inExpression))
                }
                "sign" -> {
                    inExpression.removeFirst()
                    sign(parseCalc(inExpression))
                }
                "ceil" -> {
                    inExpression.removeFirst()
                    ceil(parseCalc(inExpression))
                }
                "floor" -> {
                    inExpression.removeFirst()
                    floor(parseCalc(inExpression))
                }
                "pow" -> {
                    inExpression.removeFirst()
                    exp(parseCalc(inExpression))
                }
                "min" -> {
                    inExpression.removeFirst()
                    min(parseTerminal(inExpression), parseCalc(inExpression))
                }
                "max" -> {
                    inExpression.removeFirst()
                    max(parseTerminal(inExpression), parseCalc(inExpression))
                }
                else -> {TODO("Add identifier to parseCalc for ${first.name} line ${first.line} column ${first.column}")}
            }
        } else if (first is Token.Minus) {
            inExpression.removeFirst()
            return -parseCalc(inExpression)
        } else if (first is Token.Tilde) {
            inExpression.removeFirst()
            return BigInteger.valueOf(parseCalc(inExpression).toLong()).not()
                .toDouble()
        } else if (first is Token.Exclaimation) {
            inExpression.removeFirst()
            return if (parseCalc(inExpression) == 0.0) {1.0} else {0.0}
        } else if (first is Token.At) {
            inExpression.removeFirst()
            TODO("Handle @ ${first.line} ${first.column}")
        }
        else {
            return parseTerminal(inExpression)
        }
    }

    private fun parseTerminal(inExpression: MutableList<Token>): Double {
        // NUMBER | CONSTANT | LABEL | VREGISTER | '(' expression ')'
        var first = inExpression[0];
        if (first is Token.Identifier) {
            val x = first.name
            if (x == "PI"  ) {  inExpression.removeFirst();return Math.PI; }
            else if (x == "E"   ) {  inExpression.removeFirst();return Math.E; }
            else if (x == "HERE") { /* return this.hereaddr;*/ inExpression.removeFirst();TODO("Implement HERE") }
            else {
                inExpression.removeFirst();
                TODO("Implement Identifier resolution")
            }
        } else if (first is Token.Register) {
            inExpression.removeFirst();
            return first.register.ordinal.toDouble()
        } else if (first is Token.Number) {
            inExpression.removeFirst();
            return first.value.toDouble()
        }
        else if (first is Token.LParen) {
            inExpression.removeFirst()
            val result = parseCalc(inExpression)
            first = inExpression.removeFirst()
            if (first is Token.RParen) {
                return result
            } else {
                throw Exception("Found invalid token ${first.type} on line ${first.line} at ${first.column} when parsing expression")
            }
        } else {
            throw Exception("Found undefined token '${first.type}' on line ${first.line} at ${first.column} when calculating constant")
        }



    }

    private fun parseExpression(inExpression: MutableList<Token>):Int {

        val lhs = inExpression[0]
        val closingToken = when(lhs.type) {
            TokenType.LBrace -> TokenType.RBrace
            TokenType.LParen -> TokenType.RParen
            else -> throw Exception("Invalid expression")
        }
        inExpression.removeFirst()
        val result = parseCalc(inExpression)

        val rhs = inExpression.removeFirst()
        if (rhs.type != closingToken) {
            throw Exception("Invalid expression")
        }
        return result.toInt()
    }
}