package dev.secondsun.chip8.compose.assembler

import java.math.BigInteger
import kotlin.math.*

data class IntExpression(val expression: List<Token>) {


    fun evaluate(): Int {
        if (expression.isEmpty()) {
            return 0
        }

        if (expression.size == 1) {
            val token = expression[0]
            when(token) {
                is Token.Number -> return token.value
                is Token.Identifier -> {
                    if (REGISTERS.contains(token.name.lowercase())) {
                        return Registers.valueOf(token.name.lowercase()).ordinal
                    } else {
                        TODO()
                    }
                }
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
        if (first is Token.Identifier && first.name.equals("strlen")) {
            inExpression.removeFirst();
            return (inExpression.removeFirst() as Token.StringToken).value.length.toDouble()
        } else if (UnaryFunc.entries.any { it.match(first) }) {
            inExpression.removeFirst()
            val func = UnaryFunc.entries.first { it.match(first) }
            if (func == UnaryFunc.AT) {
                TODO("Implement AT")
            }    else {
                return func.apply(parseCalc(inExpression))
            }

        }
        val t = parseTerminal(inExpression)

        if (inExpression.isEmpty()) {
            return t
        } else {
            val next = inExpression[0]
            if (BinaryFunc.entries.any { it.match(next) }) {
                inExpression.removeFirst()
                return BinaryFunc.entries.first { it.match(next) }.apply(t, parseCalc(inExpression))
            } else {
                return t
            }
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
        } else if (first is Token.Identifier && REGISTERS.contains(first.name.lowercase())) {
            inExpression.removeFirst();
            return Registers.valueOf(first.name.lowercase()).ordinal.toDouble()
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