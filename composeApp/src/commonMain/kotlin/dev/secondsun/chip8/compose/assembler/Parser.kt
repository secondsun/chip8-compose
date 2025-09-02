package dev.secondsun.chip8.compose.assembler

/**
 * Once a program is parsed, the parserOutput includes
 *  : A list of instructions that can be assembled into machine code
 *  : A list of labels and their associated line numbers
 *  : A list of constants and their associated line numbers
 *  : A list of aliases and their associated line numbers
 */
interface ParserOutput {
    val constants: Map<String, IntExpression>
    val labels: Map<String, IntExpression>
    val aliases: Map<String, IntExpression>
    val mutables: Map<String, IntExpression>
    val parsedTokens: List<ParsedToken>
    val macros: Map<String, List<Token>>
    val stringModes: Map<String, StringMode>
}

class ParserContext(program: List<Token>) : ParserOutput {

    override val constants = mutableMapOf<String, IntExpression>()
    override val labels = mutableMapOf<String, IntExpression>()
    override val aliases = mutableMapOf<String, IntExpression>()
    override val mutables = mutableMapOf<String, IntExpression>()
    val tokenProvider = TokenProvider(program)
    override val parsedTokens = mutableListOf<ParsedToken>()
    override val macros = mutableMapOf<String, List<Token>>()
    override val stringModes = mutableMapOf<String, StringMode>()
    val branches = ArrayDeque<Triple<ParsedToken, Int, String>>()
    val loops = ArrayDeque<Pair<ParsedToken, Int>>()
    val whiles = ArrayDeque<ParsedToken?>()


}

fun parse(program: String): ParserOutput {
    val tokens = tokenize(program)
    return parse(tokens)
}

fun parse(program: List<Token>): ParserOutput {
    val context = ParserContext(program)


    with(context) {
        while (tokenProvider.hasMore()) {
            val token = tokenProvider.peek()
            if (token is Token.Proto) {
                //Deprecated
                tokenProvider.consume<Token.Proto>()
            } else {
                parsedTokens.add(parseOne())
            }
        }
    }
    return context

}

private fun ParserContext.parseOne(): ParsedToken {
    return when (val token = tokenProvider.peek()) {
        is Token.AdditionAssignment -> TODO()
        is Token.Again -> {
            consumeAgain()
        }

        is Token.Alias -> defineAlias()
        is Token.AndAssignment -> TODO()
        is Token.Assert -> handleAssert()
        is Token.Assignment -> TODO()
        is Token.Audio -> TODO()
        is Token.BCD -> handleBCD()
        is Token.Begin -> {
            tokenProvider.consume<Token.Begin>()
            return (ParsedToken(ParsedTokenType.Begin, listOf(token)))
        }

        is Token.BigHex -> TODO()
        is Token.Breakpoint -> defineBreakpoint()
        is Token.Buzzer -> defineBuzzer()
        is Token.Byte -> handleByte()
        is Token.Calc -> handleCalc()
        is Token.Call -> TODO()
        is Token.Clear -> {
            tokenProvider.consume<Token.Clear>()
            return (ParsedToken(ParsedTokenType.Clear, listOf(token)))
        }

        is Token.Colon -> defineLabel()
        is Token.Const -> defineConstant()
        is Token.Delay -> defineDelay()
        is Token.Else -> {
            handleElse()
        }

        is Token.End -> {
            handleEnd()
        }

        is Token.Equal -> TODO()
        is Token.Error -> {
            tokenProvider.consume<Token.Error>()
            return (ParsedToken(ParsedTokenType.Error, listOf(token)))
        }

        is Token.GreaterThan -> TODO()
        is Token.GreaterThanOrEqual -> TODO()
        is Token.Hex -> TODO()
        is Token.Hires -> TODO()
        is Token.I -> TODO()
        is Token.Identifier -> {
            val name = token.name
            if (macros.containsKey(name)) {
                consumeMacroExpand()
            } else {
                consumeCall()
            }
        }

        is Token.If -> consumeIf()
        is Token.Jump -> consumeOperatorWithWideParameter(ParsedTokenType.Jump)
        is Token.Jump0 -> consumeOperatorWithWideParameter(ParsedTokenType.Jump0)
        is Token.Key -> TODO()
        is Token.MinusKey -> TODO()
        is Token.LBrace -> TODO()
        is Token.LessThan -> TODO()
        is Token.LessThanOrEqual -> TODO()
        is Token.Load -> handleLoad()
        is Token.LoadFlags -> TODO()
        is Token.Loop -> consumeLoop()
        is Token.Lores -> TODO()
        is Token.Macro -> defineMacro()
        is Token.Moniter -> defineMonitor()
        is Token.Native -> consumeNative()
        is Token.Next -> defineNext()
        is Token.NotEqual -> {
            TODO()
        }

        is Token.Number -> consumeNumber()
        is Token.OrAssignment -> TODO()
        is Token.Org -> handleOrg()
        is Token.Exclaimation -> TODO()
        is Token.Pitch -> definePitch()
        is Token.Plane -> TODO()
        is Token.Pointer -> handlePointer()


        is Token.RBrace -> TODO()
        is Token.Random -> TODO()
        is Token.Register -> consumeAssign()
        is Token.Return -> {
            tokenProvider.consume<Token.Return>()
            return (ParsedToken(ParsedTokenType.Return, listOf(token)))
        }

        is Token.Save -> handleSave()
        is Token.SaveFlags -> TODO()
        is Token.ScrollDown -> TODO()
        is Token.ScrollLeft -> TODO()
        is Token.ScrollRight -> TODO()
        is Token.ScrollUp -> TODO()
        is Token.ShiftLeft -> TODO()
        is Token.ShiftRight -> TODO()
        is Token.Sprite -> consumeSprite()
        is Token.StringMode -> defineStringMode()
        is Token.SubtractionAssignment -> TODO()
        is Token.Then -> {
            tokenProvider.consume<Token.Then>()
            return (ParsedToken(ParsedTokenType.Then, listOf(token)))
        }

        is Token.Unpack -> consumeUnpack()
        is Token.While -> consumeWhile()


        is Token.XorAssignment -> TODO()
        is Token.Minus -> TODO()
        is Token.Plus -> TODO()
        is Token.StringToken -> TODO()
        is Token.Divide -> TODO()
        is Token.Multiply -> TODO()
        is Token.BinaryAnd -> TODO()
        is Token.BinaryOr -> TODO()
        is Token.LParen -> TODO()
        is Token.RParen -> TODO()
        is Token.At -> TODO()
        is Token.Tilde -> TODO()
        is Token.Semicolon -> {
            tokenProvider.consume<Token.Semicolon>()
            return (ParsedToken(ParsedTokenType.Return, listOf(token)))
        }

        is Token.ShiftLeftAssign -> TODO()
        is Token.ShiftRightAssign -> TODO()
        is Token.Caret -> TODO()
        is Token.Percent -> TODO()
        else -> {
            throw IllegalStateException("Unexpected token $token")
        }
    }
}

private fun ParserContext.consumeAgain(): ParsedToken {
    val token = tokenProvider.consume<Token.Again>()
    if (this.loops.isEmpty()) {
        val error = Token.Error("Again without loop", token.line, token.column)
        return (ParsedToken(ParsedTokenType.Error, listOf(error)))
    }
    this.loops.removeLast()

    while (this.whiles.last() != null) {
        this.whiles.removeLast()
    }

    this.whiles.removeLast()

    return (ParsedToken(ParsedTokenType.Again, listOf(token)))
}

private fun ParserContext.handleEnd(): ParsedToken {
    val token = tokenProvider.consume<Token.End>()
    if (this.branches.isEmpty()) {
        val error = Token.Error("End without if", token.line, token.column)
        return (ParsedToken(ParsedTokenType.Error, listOf(error)))
    }
    this.branches.removeLast()

    return (ParsedToken(ParsedTokenType.End, listOf(token)))
}

private fun ParserContext.handleElse(): ParsedToken {
    val token = tokenProvider.consume<Token.Else>()

    if (this.branches.isEmpty()) {
        val error = Token.Error("Else without if", token.line, token.column)
        return ParsedToken(ParsedTokenType.Error, listOf(error))
    }

    this.branches.removeLast()


    val toReturn = (ParsedToken(ParsedTokenType.Else, listOf(token)))
    this.branches.addLast(Triple(toReturn, parsedTokens.size, "else"))
    return toReturn
}

private fun ParserContext.consumeWhile(): ParsedToken {
    val whileToken = tokenProvider.consume<Token.While>()
    if (this.loops.isEmpty()) {
        val error = Token.Error("While without if", whileToken.line, whileToken.column)
        return ParsedToken(ParsedTokenType.Error, listOf(error))
    }
    val condition = consumeCondition()
    if (condition.type == ParsedTokenType.Error) {
        return (ParsedToken(ParsedTokenType.Error, buildList { add(whileToken); addAll(condition.tokens) }))
    } else {
        val toReturn = ParsedConditionalToken(
            type = ParsedTokenType.While,
            tokens = listOf(whileToken),
            condition = listOf(condition)
        )

        this.whiles.addLast(toReturn)

        return toReturn
    }
}

private fun ParserContext.consumeLoop(): ParsedToken {
    val loop = tokenProvider.consume<Token.Loop>()

    val toReturn = ParsedToken(type = ParsedTokenType.Loop, tokens = listOf(loop))
    this.loops.addLast(Pair(toReturn, parsedTokens.size))
    this.whiles.addLast(null)
    return toReturn
}

private fun ParserContext.consumeSprite(): ParsedToken {
    val sprite = tokenProvider.consume<Token.Sprite>()
    val register1 = tokenProvider.consume<Any>()

    if (register1 is Token.Error) {
        return (ParsedToken(ParsedTokenType.Error, listOf(sprite, register1)))
    } else if (register1 is Token.Identifier && !defined(register1.name)) {
        val errorToken = Token.Error("Label ${register1.name} not defined", register1.line, register1.column)
        return (ParsedToken(ParsedTokenType.Error, listOf(sprite, errorToken)))
    } else if (register1 !is Token.Register) {
        val errorToken = Token.Error("Expected Register", register1.line, register1.column)
        return (ParsedToken(ParsedTokenType.Error, listOf(sprite, errorToken)))
    }

    val register2 = tokenProvider.consume<Any>()

    if (register2 is Token.Error) {
        return (ParsedToken(ParsedTokenType.Error, listOf(sprite, register1)))
    } else if (register2 is Token.Identifier && !defined(register2.name)) {
        val errorToken = Token.Error("Label ${register2.name} not defined", register2.line, register2.column)
        return (ParsedToken(ParsedTokenType.Error, listOf(sprite, errorToken)))
    } else if (register2 !is Token.Register) {
        val errorToken = Token.Error("Expected Register", register2.line, register2.column)
        return (ParsedToken(ParsedTokenType.Error, listOf(sprite, errorToken)))
    }


    val size = tokenProvider.consume<Any>()
    if (size is Token.Error) {
        return (ParsedToken(ParsedTokenType.Error, listOf(sprite, register1)))
    } else if (size is Token.Identifier && !defined(size.name)) {
        val errorToken = Token.Error("Label ${size.name} not defined", size.line, size.column)
        return (ParsedToken(ParsedTokenType.Error, listOf(sprite, errorToken)))
    } else if (size is Token.Number) {
        if (size.value !in 0..15) {
            val errorToken = Token.Error("Sprite size must be between 0 and 15", size.line, size.column)
            return (ParsedToken(ParsedTokenType.Error, listOf(sprite, register1, register2, errorToken)))
        }
    }

    return (ParsedToken(ParsedTokenType.Sprite, listOf(sprite, register1, register2, size)))

}

private fun ParserContext.consumeNative(): ParsedToken {
    return consumeOperatorWithWideParameter(ParsedTokenType.Native)
}

private fun ParserContext.consumeOperatorWithWideParameter(jumpType: ParsedTokenType): ParsedToken {
    val jump0 = tokenProvider.consume<Token.Jump0>()
    val next = tokenProvider.consume<Any>()
    if (next is Token.Identifier) {
        if (!defined(next.name)) {
            val errorToken = Token.Error("Label ${next.name} not defined", next.line, next.column)
            return (ParsedToken(ParsedTokenType.Error, listOf(jump0, errorToken)))
        } else {
            return (ParsedToken(jumpType, listOf(jump0, next)))
        }
    } else if (next is Token.Number) {
        if (next.value !in 0..0x0fff) {
            val errorToken = Token.Error("Jump address must be between 0x000 and 0xfff", next.line, next.column)
            return (ParsedToken(ParsedTokenType.Error, listOf(jump0, errorToken)))
        }
        return (ParsedToken(jumpType, listOf(jump0, next)))
    } else {
        val errorToken = Token.Error("Expected Identifier", next.line, next.column)
        return (ParsedToken(ParsedTokenType.Error, listOf(jump0, errorToken)))
    }
}

private fun ParserContext.consumeCall(): ParsedToken {
    when (val call = tokenProvider.consume<Any>()) {
        is Token.Call -> {
            val next = tokenProvider.peek()
            if (next is Token.Identifier) {
                tokenProvider.consume<Token.Identifier>()
                if (labels.containsKey(next.name)) {
                    return (ParsedToken(ParsedTokenType.Call, listOf(call, next)))
                } else {
                    val errorToken = Token.Error("Label ${next.name} not defined", next.line, next.column)
                    return (ParsedToken(ParsedTokenType.Error, listOf(call, errorToken)))
                }
            } else {
                val errorToken = Token.Error("Expected label", next.line, next.column)
                return (ParsedToken(ParsedTokenType.Error, listOf(call, errorToken)))
            }
        }

        is Token.Identifier -> {
            if (labels.containsKey(call.name)) {
                return (ParsedToken(ParsedTokenType.Call, listOf(call, call)))
            } else {
                val errorToken = Token.Error("Label ${call.name} not defined", call.line, call.column)
                return (ParsedToken(ParsedTokenType.Error, listOf(call, errorToken)))
            }
        }

        else -> {
            val errorToken = Token.Error("Expected Call or Label", call.line, call.column)
            return (ParsedToken(ParsedTokenType.Error, listOf(call, errorToken)))
        }
    }
}

private fun ParserContext.consumeMacroExpand(): ParsedToken {
    TODO()
}

private fun ParserContext.consumeAssign(): ParsedToken {
    val register = tokenProvider.consume<Token.Register>()
    when (val operator = tokenProvider.consume<Any>()) {
        is Token.Assignment,
        is Token.XorAssignment,
        is Token.OrAssignment,
        is Token.AndAssignment,
        is Token.AdditionAssignment,
        is Token.ShiftLeftAssign,
        is Token.ShiftRightAssign,
        is Token.SubtractionAssignment -> {
            val parameter = tokenProvider.consume<Any>()
            if (parameter is Token.Identifier) {
                if (!defined(parameter.name)) {
                    val errorToken =
                        Token.Error("Label ${parameter.name} not defined", parameter.line, parameter.column)
                    return (ParsedToken(ParsedTokenType.Error, listOf(register, operator, parameter, errorToken)))
                } else {
                    return ParsedToken(ParsedTokenType.Assignment, listOf(register, operator, parameter))
                }
            } else if (parameter is Token.Register || parameter is Token.Random || parameter is Token.Number || parameter is Token.Key || parameter is Token.Delay) {
                return ParsedToken(ParsedTokenType.Assignment, listOf(register, operator, parameter))
            } else {
                val errorToken =
                    Token.Error("Expected Register, Identifier, Number or Key", parameter.line, parameter.column)
                return (ParsedToken(ParsedTokenType.Error, listOf(register, operator, parameter, errorToken)))
            }
        }

        else -> {
            val errorToken = Token.Error("Expected Assignment", operator.line, operator.column)
            return (ParsedToken(ParsedTokenType.Error, listOf(register, operator, errorToken)))
        }
    }
}

private fun ParserContext.consumeIf(): ParsedToken {
    val tokens = mutableListOf<Token>()
    val ifToken = tokenProvider.consume<Token.If>()
    tokens.add(ifToken)
    val condition: ParsedToken = consumeCondition()
    tokens.addAll(condition.tokens)

    if (condition.type == ParsedTokenType.Error) {
        val errorToken = ParsedToken(ParsedTokenType.Error, buildList { add(ifToken); addAll(condition.tokens) })
        return (errorToken)

    }

    val toReturn = (ParsedConditionalToken(type = ParsedTokenType.If, tokens = tokens, condition = listOf(condition)))
    //handle begin or then
    when (val beginOrThen = tokenProvider.consume<Token>()) {
        is Token.Begin -> {
            branches.addLast(Triple(toReturn, parsedTokens.size, "begin"))
        }

        is Token.Then -> {}
        else -> {
            return (ParsedConditionalToken(
                type = ParsedTokenType.Error,
                tokens = buildList {
                    addAll(tokens); add(
                    Token.Error(
                        "Expected begin or Then",
                        beginOrThen.line,
                        beginOrThen.column
                    )
                )
                },
                condition = listOf(condition)
            ))
        }
    }

    return toReturn

}

private fun ParserContext.consumeCondition(): ParsedToken {
    val token = tokenProvider.consume<Token.Register>()

    if (token is Token.Register || token is Token.Identifier) {
        when (val check = tokenProvider.consume<Any>()) {
            is Token.Key, is Token.MinusKey -> {
                return ParsedToken(ParsedTokenType.Condition, listOf(token, check))
            }

            is Token.Equal,
            is Token.NotEqual,
            is Token.GreaterThan,
            is Token.GreaterThanOrEqual,
            is Token.LessThan,
            is Token.LessThanOrEqual -> {
                val nextToken = tokenProvider.consume<Any>()
                if (nextToken is Token.Register || nextToken is Token.Identifier || nextToken is Token.Number) {
                    return ParsedToken(ParsedTokenType.Condition, listOf(token, check, nextToken))
                } else {
                    val nextTokenError =
                        Token.Error("Expected Register or Identifier", nextToken.line, nextToken.column)
                    return ParsedToken(ParsedTokenType.Error, listOf(token, check, nextTokenError))
                }
            }

            else -> {
                val checkError =
                    Token.Error("Invalid condition check ${check.javaClass.simpleName}", token.line, token.column)
                return ParsedToken(ParsedTokenType.Error, listOf(token, checkError))
            }
        }
    } else {
        return ParsedToken(ParsedTokenType.Error, listOf(token))
    }


}


private fun ParserContext.definePitch()
        : ParsedToken {
    val pitch = tokenProvider.consume<Token.Pitch>()
    val assign = tokenProvider.consume<Token.Assignment>()

    if (assign is Token.Error) {
        return (ParsedToken(ParsedTokenType.Error, listOf(pitch, assign)))
    } else {
        val register = tokenProvider.consume<Token>()
        if (register is Token.Register) {
            return (ParsedToken(ParsedTokenType.Pitch, listOf(pitch, assign, register)))
        } else if (register is Token.Identifier) {
            if (!defined(register.name)) {
                val errorToken = Token.Error("Label ${register.name} not defined", register.line, register.column)
                return (ParsedToken(ParsedTokenType.Error, listOf(pitch, assign, errorToken)))
            } else {
                return (ParsedToken(ParsedTokenType.Pitch, listOf(pitch, assign, register)))
            }
        } else {
            val errorToken = Token.Error("Expected Register or Label", register.line, register.column)
            return (ParsedToken(ParsedTokenType.Error, listOf(pitch, assign, errorToken)))
        }
    }

}

private fun ParserContext.defineBuzzer()
        : ParsedToken {
    val buzzer = tokenProvider.consume<Token.Buzzer>()
    val assign = tokenProvider.consume<Token.Assignment>()

    if (assign is Token.Error) {
        return (ParsedToken(ParsedTokenType.Error, listOf(buzzer, assign)))
    } else {
        val register = tokenProvider.consume<Token>()
        if (register is Token.Register) {
            return (ParsedToken(ParsedTokenType.Buzzer, listOf(buzzer, assign, register)))
        } else if (register is Token.Identifier) {
            if (!defined(register.name)) {
                val errorToken = Token.Error("Label ${register.name} not defined", register.line, register.column)
                return (ParsedToken(ParsedTokenType.Error, listOf(buzzer, assign, errorToken)))
            } else {
                return (ParsedToken(ParsedTokenType.Buzzer, listOf(buzzer, assign, register)))
            }
        } else {
            val errorToken = Token.Error("Expected Register or Label", register.line, register.column)
            return (ParsedToken(ParsedTokenType.Error, listOf(buzzer, assign, errorToken)))
        }
    }

}


private fun ParserContext.defineDelay()
        : ParsedToken {
    val delay = tokenProvider.consume<Token.Delay>()
    val assign = tokenProvider.consume<Token.Assignment>()

    if (assign is Token.Error) {
        return (ParsedToken(ParsedTokenType.Error, listOf(delay, assign)))
    } else {
        val register = tokenProvider.consume<Token>()
        if (register is Token.Register) {
            return (ParsedToken(ParsedTokenType.Delay, listOf(delay, assign, register)))
        } else if (register is Token.Identifier) {
            if (!defined(register.name)) {
                val errorToken = Token.Error("Label ${register.name} not defined", register.line, register.column)
                return (ParsedToken(ParsedTokenType.Error, listOf(delay, assign, errorToken)))
            } else {
                return (ParsedToken(ParsedTokenType.Delay, listOf(delay, assign, register)))
            }
        } else {
            val errorToken = Token.Error("Expected Register or Label", register.line, register.column)
            return (ParsedToken(ParsedTokenType.Error, listOf(delay, assign, errorToken)))
        }
    }

}


private fun ParserContext.handleLoad(): ParsedToken {
    val load = tokenProvider.consume<Token.Load>()
    val reg = tokenProvider.peek()
    if (reg is Token.Register) {
        tokenProvider.consume<Token.Register>()
        if (tokenProvider.hasMore()) {
            val next = tokenProvider.peek()
            if (next is Token.Minus) {
                tokenProvider.consume<Token.Minus>()
                val reg2 = tokenProvider.peek()
                if (reg2 is Token.Register) {
                    tokenProvider.consume<Token.Register>()
                    return (ParsedToken(ParsedTokenType.Load, listOf(load, reg, reg2)))
                } else {
                    tokenProvider.consume<Any>()
                    val errorToken = Token.Error("Expected Register", reg2.line, reg2.column)
                    return (ParsedToken(ParsedTokenType.Error, listOf(load, reg, errorToken)))
                }
            } else {
                return (ParsedToken(ParsedTokenType.Load, listOf(load, reg)))
            }
        } else {
            return (ParsedToken(ParsedTokenType.Load, listOf(load, reg)))
        }
    } else {
        tokenProvider.consume<Any>()
        val errorToken = Token.Error("Expected Register", reg.line, reg.column)
        return (ParsedToken(ParsedTokenType.Error, listOf(load, errorToken)))
    }

}

private fun ParserContext.handleSave(): ParsedToken {
    val save = tokenProvider.consume<Token.Save>()
    val reg = tokenProvider.peek()
    if (reg is Token.Register) {
        tokenProvider.consume<Token.Register>()
        if (tokenProvider.hasMore()) {
            val next = tokenProvider.peek()
            if (next is Token.Minus) {
                tokenProvider.consume<Token.Minus>()
                val reg2 = tokenProvider.peek()
                if (reg2 is Token.Register) {
                    tokenProvider.consume<Token.Register>()
                    return (ParsedToken(ParsedTokenType.Save, listOf(save, reg, reg2)))
                } else {
                    tokenProvider.consume<Any>()
                    val errorToken = Token.Error("Expected Register", reg2.line, reg2.column)
                    return (ParsedToken(ParsedTokenType.Error, listOf(save, reg, errorToken)))
                }
            } else {
                return (ParsedToken(ParsedTokenType.Save, listOf(save, reg)))
            }
        } else {
            return (ParsedToken(ParsedTokenType.Save, listOf(save, reg)))
        }
    } else {
        tokenProvider.consume<Any>()
        val errorToken = Token.Error("Expected Register", reg.line, reg.column)
        return (ParsedToken(ParsedTokenType.Error, listOf(save, errorToken)))
    }

}

private fun ParserContext.handleBCD(): ParsedToken {
    val bcd = tokenProvider.consume<Token.BCD>()
    val next = tokenProvider.peek()
    if (next is Token.Register) {
        tokenProvider.consume<Token.Register>()
        return (ParsedToken(ParsedTokenType.BCD, listOf(bcd, next)))
    } else if (next is Token.Identifier) {
        tokenProvider.consume<Token.Identifier>()
        if (defined(next.name)) {
            return (ParsedToken(ParsedTokenType.BCD, listOf(bcd, next)))
        } else {
            val errorToken = Token.Error("Label ${next.name} not defined", next.line, next.column)
            return (ParsedToken(ParsedTokenType.Error, listOf(bcd, errorToken)))
        }
    } else {
        val errorToken = Token.Error("Expected Register or Label", next.line, next.column)
        return (ParsedToken(ParsedTokenType.Error, listOf(bcd, errorToken)))
    }
}

private fun ParserContext.handleAssert(): ParsedToken {
    val assert = tokenProvider.consume<Token.Assert>()
    val message = tokenProvider.peek()

    if (message is Token.Error) {
        tokenProvider.consume<Token.Error>()
        return (ParsedToken(ParsedTokenType.Error, listOf(assert) + message))

    }

    if (message is Token.StringToken) {
        tokenProvider.consume<Token.StringToken>()
    }

    val expression = consumeMacroBody()

    if (expression.any { it is Token.Error }) {
        if (message is Token.StringToken) {
            return (ParsedToken(ParsedTokenType.Error, listOf(assert) + message + expression))
        } else {
            return (ParsedToken(ParsedTokenType.Error, listOf(assert) + expression))
        }

    }

    if (message is Token.StringToken) {
        return (ParsedToken(ParsedTokenType.Assert, listOf(assert) + message + expression))
    } else {
        return (ParsedToken(ParsedTokenType.Assert, listOf(assert) + expression))
    }


}

private fun ParserContext.handleOrg(): ParsedToken {
    val pointer = tokenProvider.consume<Token.Pointer>()
    when (val next = tokenProvider.peek()) {
        is Token.LBrace -> {
            val expression = consumeMacroBody()
            return (ParsedToken(ParsedTokenType.Org, listOf(pointer) + expression))
        }

        is Token.Number -> {
            tokenProvider.consume<Token.Number>()
            if (next.value !in 0..0xFFFF) {
                return (ParsedToken(ParsedTokenType.Org, listOf(pointer, next)))
            } else {
                val error = Token.Error("Wide value out of range", next.line, next.column)
                return (ParsedToken(ParsedTokenType.Error, listOf(pointer, error)))
            }
        }

        is Token.Identifier -> {
            if (defined(next.name)) {
                tokenProvider.consume<Token.Identifier>()
                return (ParsedToken(ParsedTokenType.Org, listOf(pointer, next)))
            } else {
                val error = Token.Error("Label ${next.name} not defined", next.line, next.column)
                return (ParsedToken(ParsedTokenType.Error, listOf(pointer, error)))
            }
        }

        else -> {
            tokenProvider.consume<Any>()
            val error = Token.Error("Expected Number or Macro", next.line, next.column)
            return (ParsedToken(ParsedTokenType.Error, listOf(pointer, error)))
        }
    }
}

private fun ParserContext.handlePointer(): ParsedToken {
    val pointer = tokenProvider.consume<Token.Pointer>()
    when (val next = tokenProvider.peek()) {
        is Token.LBrace -> {
            val expression = consumeMacroBody()
            return (ParsedToken(ParsedTokenType.Pointer, listOf(pointer) + expression))
        }

        is Token.Number -> {
            tokenProvider.consume<Token.Number>()
            if (next.value !in 0..0xFFFF) {
                return (ParsedToken(ParsedTokenType.Pointer, listOf(pointer, next)))
            } else {
                val error = Token.Error("Wide value out of range", next.line, next.column)
                return (ParsedToken(ParsedTokenType.Error, listOf(pointer, error)))
            }
        }

        is Token.Identifier -> {
            //Pointer can have forward-references
            tokenProvider.consume<Token.Identifier>()
            return (ParsedToken(ParsedTokenType.Pointer, listOf(pointer, next)))
        }

        else -> {
            tokenProvider.consume<Any>()
            val error = Token.Error("Expected Number or Macro", next.line, next.column)
            return (ParsedToken(ParsedTokenType.Error, listOf(pointer, error)))
        }
    }
}


private fun ParserContext.handleByte(): ParsedToken {
    val byte = tokenProvider.consume<Token.Byte>()
    when (val next = tokenProvider.peek()) {
        is Token.LBrace -> {
            val expression = consumeMacroBody()
            return (ParsedToken(ParsedTokenType.Byte, listOf(byte) + expression))
        }

        is Token.Number -> {
            tokenProvider.consume<Token.Number>()
            if (next.value !in -128..255) {
                return (ParsedToken(ParsedTokenType.Byte, listOf(byte, next)))
            } else {
                val error = Token.Error("Byte value out of range", next.line, next.column)
                return (ParsedToken(ParsedTokenType.Error, listOf(byte, error)))
            }
        }

        else -> {
            val error = Token.Error("Expected Number or Macro", next.line, next.column)
            return (ParsedToken(ParsedTokenType.Error, listOf(byte, error)))
        }
    }
}


private fun ParserContext.defineStringMode(): ParsedToken {
    val stringMode = tokenProvider.consume<Token.StringMode>()
    val stringModeName = tokenProvider.consume<Token.Identifier>()


    if (stringModeName is Token.Identifier) {
        val stringModeAlphabet = tokenProvider.consume<Token.StringToken>()
        if (stringModeAlphabet is Token.StringToken) {
            val stringModeBody = consumeMacroBody()
            if (!stringModeBody.any { it is Token.Error }) {
                try {
                    val stmName = stringModeName.name

                    val mode = stringModes.computeIfAbsent(stmName) { StringMode(stmName) }
                    mode.addAlphabet(stringModeAlphabet.value, stringModeBody)
                    return (ParsedToken(
                        ParsedTokenType.StringMode,
                        buildList { add(stringMode); add(stringModeName); add(stringModeAlphabet); addAll(stringModeBody) }))
                } catch (e: Exception) {
                    val alphabetError = Token.Error(
                        "Invalid Alphabet : ${e.message}",
                        stringModeAlphabet.line,
                        stringModeAlphabet.column
                    )
                    return (ParsedToken(
                        ParsedTokenType.Error,
                        buildList { add(stringMode); add(stringModeName); add(alphabetError); addAll(stringModeBody) }))
                }


            } else {
                return (ParsedToken(
                    ParsedTokenType.Error,
                    listOf(stringMode, stringModeName, stringModeAlphabet) + stringModeBody
                ))
            }
        } else {
            if (stringModeAlphabet is Token.Error)
                return (ParsedToken(ParsedTokenType.Error, listOf(stringMode, stringModeName, stringModeAlphabet)))
            else {
                val errorToken = Token.Error("Expected StringToken", stringModeAlphabet.line, stringModeAlphabet.column)
                return (ParsedToken(ParsedTokenType.Error, listOf(stringMode, stringModeName, errorToken)))
            }

        }

    } else {
        if (stringModeName is Token.Error)
            return (ParsedToken(ParsedTokenType.Error, listOf(stringMode, stringModeName)))
        else {
            val errorToken = Token.Error("Expected Identifier", stringModeName.line, stringModeName.column)
            return (ParsedToken(ParsedTokenType.Error, listOf(stringMode, errorToken)))
        }
    }

}

private fun ParserContext.defineMacro(): ParsedToken {
    val macro = tokenProvider.consume<Token.Macro>()
    val identifier = tokenProvider.consume<Token.Identifier>()
    if (identifier is Token.Identifier) {
        if (!defined(identifier.name)) {
            val arguments: List<Token> = consumeMacroDefArguments()
            if (!arguments.any { it is Token.Error }) {
                val body: List<Token> = consumeMacroBody()
                if (!body.any { it is Token.Error }) {
                    val macroTokens = mutableListOf<Token>()

                    macroTokens.addAll(arguments)
                    macroTokens.addAll(body)
                    macros[identifier.name] = macroTokens
                    return (ParsedToken(
                        ParsedTokenType.Macro,
                        buildList { add(macro); add(identifier); addAll(arguments); addAll(body) }))
                } else {
                    val errorWithArguments = buildList<Token> {
                        add(macro)
                        add(identifier)
                        addAll(arguments)
                        addAll(body)
                    }
                    return (ParsedToken(ParsedTokenType.Error, errorWithArguments))
                }
            } else {
                val errorWithArguments = mutableListOf<Token>(macro, identifier)
                errorWithArguments.addAll(arguments)
                return (ParsedToken(ParsedTokenType.Error, errorWithArguments))
            }
        } else {
            val errorToken = Token.Error("${identifier.name} is already defined", identifier.line, identifier.column)
            return (ParsedToken(ParsedTokenType.Error, listOf(macro, errorToken)))
        }
    } else {
        return (ParsedToken(ParsedTokenType.Error, listOf(macro, identifier)))
    }
}

private fun ParserContext.consumeMacroBody(): List<Token> {
    var token = tokenProvider.consume<Token.LBrace>()
    val toReturn = mutableListOf<Token>()
    var depth = 0


    toReturn.add(token)
    token = tokenProvider.consume<Any>()


    while (token !is Token.Error && (token !is Token.RBrace || depth > 0)) {

        if (token is Token.LBrace) {
            depth++
        } else if (token is Token.RBrace) {
            depth--
        }

        toReturn.add(token)
        token = tokenProvider.consume<Any>()


    }

    toReturn.add(token)

    return toReturn
}

private fun ParserContext.consumeMacroDefArguments(): List<Token> {
    var token = tokenProvider.peek()
    val toReturn = mutableListOf<Token>()
    while (token !is Token.Error && token !is Token.LBrace) {
        tokenProvider.consume<Any>()
        if (token is Token.Identifier) {
            toReturn.add(token)
        } else {
            toReturn.add(Token.Error("Expected Identifier", token.line, token.column))
        }
        token = tokenProvider.peek()
    }

    if (token is Token.Error) {
        tokenProvider.consume<Any>()
        toReturn.add(token)
    }

    return toReturn
}

private fun ParserContext.defineMonitor(): ParsedToken {
    val monitor = tokenProvider.consume<Token.Moniter>()
    val identifier = tokenProvider.consume<Token>()

    if (identifier is Token.Identifier) {
        if (defined(identifier.name)) {
            val errorToken = Token.Error("Monitor already defined", identifier.line, identifier.column)
            return (ParsedToken(ParsedTokenType.Error, listOf(monitor, errorToken)))

        }
    } else if (identifier !is Token.Register) {
        val errorToken = Token.Error("Expected Identifier or Register", identifier.line, identifier.column)
        return (ParsedToken(ParsedTokenType.Error, listOf(monitor, errorToken)))

    }

    val value = tokenProvider.consume<Token>()

    if (value is Token.Number) {
        return (ParsedToken(ParsedTokenType.Monitor, listOf(monitor, identifier, value)))
    } else if (value is Token.StringToken) {
        return (ParsedToken(ParsedTokenType.Monitor, listOf(monitor, identifier, value)))
    } else {
        val errorToken = Token.Error("Expected String or Number", value.line, value.column)

        return (ParsedToken(ParsedTokenType.Error, listOf(monitor, identifier, errorToken)))
    }

}

private fun ParserContext.defineBreakpoint(): ParsedToken {
    val breakpoint = tokenProvider.consume<Token.Breakpoint>()
    val identifier = tokenProvider.consume<Token.Identifier>()
    if (identifier is Token.Identifier) {
        if (defined(identifier.name)) {
            val errorToken = Token.Error("Breakpoint already defined", identifier.line, identifier.column)
            return (ParsedToken(ParsedTokenType.Error, listOf(breakpoint, errorToken)))
        } else {
            val tokens = mutableListOf<Token>()
            tokens.add(breakpoint)
            tokens.add(identifier)
            return (ParsedToken(ParsedTokenType.Breakpoint, listOf(breakpoint, identifier)))
        }
    } else {
        val identifierError = Token.Error("Expected identifier", identifier.line, identifier.column)
        return (ParsedToken(ParsedTokenType.Error, listOf(breakpoint, identifierError)))
    }
}

private fun ParserContext.consumeUnpack(): ParsedToken {
    val unpack = tokenProvider.consume<Token.Unpack>()
    val next = tokenProvider.peek()

    if (next is Token.Identifier) {
        when (next.name) {
            "long" -> {
                //consume long
                tokenProvider.consume<Token.Identifier>()
                //handle number or label
                return (handleNumberOrLabel(listOf(unpack, next), ParsedTokenType.Unpack))
            }

            else -> {
                //handle number or label
                tokenProvider.consume<Token.Identifier>()
                return (handleNumberOrLabel(listOf(unpack, next), ParsedTokenType.Unpack))
            }
        }
    } else if (next is Token.Number) {
        return (handleNumberOrLabel(listOf(unpack), ParsedTokenType.Unpack))
    } else {
        tokenProvider.consume<Any>()
        return (ParsedToken(ParsedTokenType.Error, listOf(unpack, next)))
    }

}

/**
 * Consume the current token and parse it. If it is a label, confirm the label has been defined.
 * If it is a number, add it to the parsed tokens. Returns an error token if it is not a number or label.
 *
 * @param tokens The list of tokens to add to
 * @param parsedTokenType The type of the parsed token to create
 */
private fun ParserContext.handleNumberOrLabel(tokens: List<Token>, parsedTokenType: ParsedTokenType): ParsedToken {
    val next = tokenProvider.peek()
    val tokensList = tokens.toMutableList()
    return when (next) {
        is Token.Identifier -> {
            tokenProvider.consume<Token.Identifier>()
            if (defined(next.name)) {
                tokensList.add(next)
                ParsedToken(parsedTokenType, tokensList)
            } else {
                val errorToken = Token.Error("Label ${next.name} not defined", next.line, next.column)
                tokensList.add(errorToken)
                ParsedToken(ParsedTokenType.Error, tokensList)
            }
        }

        is Token.Number -> {
            tokenProvider.consume<Token.Number>()
            tokensList.add(next)
            ParsedToken(parsedTokenType, tokensList)
        }

        else -> {
            tokenProvider.consume<Any>()
            val errorToken = Token.Error("Invalid Token", next.line, next.column)
            tokensList.add(errorToken)
            ParsedToken(ParsedTokenType.Error, tokensList)
        }
    }
}

private fun ParserContext.consumeNumber(): ParsedToken {
    val number = tokenProvider.consume<Token.Number>()
    if (number is Token.Number) {
        val value = number.value
        if (value < -128 || value > 255) {
            val errorToken = Token.Error("Number out of range", number.line, number.column)
            return (ParsedToken(ParsedTokenType.Error, listOf(errorToken)))
        } else {
            return (ParsedToken(ParsedTokenType.Number, listOf(number)))
        }
    } else {
        val errorToken = Token.Error("Expected number", number.line, number.column)
        return (ParsedToken(ParsedTokenType.Error, listOf(errorToken)))
    }
}

private fun ParserContext.defineAlias(): ParsedToken {
    val aliasToken = tokenProvider.consume<Token.Alias>()
    val identifier = tokenProvider.consume<Token.Identifier>()
    if (identifier is Token.Identifier) {
        if (defined(identifier.name)) {
            val errorToken = Token.Error("Constant already defined", identifier.line, identifier.column)
            return (ParsedToken(ParsedTokenType.Error, listOf(aliasToken, errorToken)))
        } else {
            val calulatedConstantResult: Pair<List<Token>, IntExpression> = calculateAlias()
            constants[identifier.name] = calulatedConstantResult.second
            val tokens = mutableListOf<Token>()
            tokens.add(aliasToken)
            tokens.add(identifier)
            tokens.addAll(calulatedConstantResult.first)
            if (tokens.any { it is Token.Error }) {
                return (ParsedToken(ParsedTokenType.Error, tokens))
            } else {
                return (ParsedToken(ParsedTokenType.Alias, tokens))
            }
        }
    } else {
        val identifierError = Token.Error("Expected identifier", identifier.line, identifier.column)
        return (ParsedToken(ParsedTokenType.Error, listOf(aliasToken, identifierError)))
    }

}

private fun ParserContext.defineConstant(): ParsedToken {
    val constToken = tokenProvider.consume<Token.Const>()
    val identifier = tokenProvider.consume<Token.Identifier>()

    if (identifier is Token.Identifier) {
        if (defined(identifier.name)) {
            val errorToken = Token.Error("Constant already defined", identifier.line, identifier.column)
            return (ParsedToken(ParsedTokenType.Error, listOf(constToken, errorToken)))
        } else {
            val calculateConstantResult: Pair<List<Token>, IntExpression> = calculateConstant()
            constants[identifier.name] = calculateConstantResult.second
            val tokens = mutableListOf<Token>()
            tokens.add(constToken)
            tokens.add(identifier)
            tokens.addAll(calculateConstantResult.first)
            return (ParsedToken(ParsedTokenType.Constant, tokens))
        }
    } else {
        val identifierError = Token.Error("Expected identifier", identifier.line, identifier.column)
        return (ParsedToken(ParsedTokenType.Error, listOf(constToken, identifierError)))
    }

}

private fun ParserContext.calculateAlias(): Pair<List<Token>, IntExpression> {
    when (val next = tokenProvider.peek()) {
        is Token.Number -> {
            tokenProvider.consume<Token.Number>()
            if (next.value !in 0..15) {
                val errorToken = Token.Error("Number out of range", next.line, next.column)
                return Pair(listOf(errorToken), IntExpression(listOf(errorToken)))
            } else {
                return Pair(listOf(next), IntExpression(listOf(next)))
            }
        }

        is Token.Register -> {
            tokenProvider.consume<Token.Register>()
            if (next.register == Registers.i) {
                val errorToken = Token.Error("Cannot use i register in alias", next.line, next.column)
                return Pair(listOf(errorToken), IntExpression(listOf(errorToken)))
            } else {
                return Pair(listOf(next), IntExpression(listOf(next)))
            }
        }

        is Token.Identifier -> {
            tokenProvider.consume<Token.Identifier>()
            return Pair(listOf(next), IntExpression(listOf(next)))
        }

        is Token.LBrace -> {
            tokenProvider.consume<Token.LBrace>()
            val expressionTokens = mutableListOf<Token>()
            expressionTokens.add(next)
            while (tokenProvider.peek() !is Token.RBrace) {
                val expressionToken = tokenProvider.consume<Any>()
                if (expressionToken is Token.Error) {
                    break
                }
                expressionTokens.add(expressionToken)
            }
            expressionTokens.add(tokenProvider.consume<Token.RBrace>())
            return Pair(listOf(next), IntExpression(expressionTokens))
        }

        else -> {
            tokenProvider.consume<Any>()
            val errorToken = Token.Error("Expected number or expression", next.line, next.column)
            return Pair(listOf(errorToken), IntExpression(listOf(errorToken)))
        }
    }
}

private fun ParserContext.calculateConstant(): Pair<List<Token>, IntExpression> {
    when (val next = tokenProvider.peek()) {
        is Token.Number -> {
            tokenProvider.consume<Token.Number>()
            return Pair(listOf(next), IntExpression(listOf(next)))
        }

        is Token.Identifier -> {
            tokenProvider.consume<Token.Identifier>()
            if (defined(next.name)) {
                if (constants.containsKey(next.name)) {
                    return Pair(listOf(next), constants[next.name]!!)
                } else if (labels.containsKey(next.name)) {
                    return Pair(listOf(next), labels[next.name]!!)
                } else {
                    val errorToken = Token.Error("undefined identifier", next.line, next.column)
                    return Pair(listOf(errorToken), IntExpression(listOf(errorToken)))
                }
            } else {
                val errorToken = Token.Error("undefined identifier", next.line, next.column)
                return Pair(listOf(errorToken), IntExpression(listOf(errorToken)))
            }
        }

        else -> {
            tokenProvider.consume<Any>()
            val errorToken = Token.Error("Expected number", next.line, next.column)
            return Pair(listOf(errorToken), IntExpression(listOf(errorToken)))
        }
    }
}

private fun ParserContext.defineNext(): ParsedToken {
    val next = tokenProvider.consume<Token.Next>()
    val identifier = tokenProvider.consume<Token.Identifier>()

    val tokens = listOf(next, identifier)
    if (tokens.any { it is Token.Error }) {
        return (ParsedToken(ParsedTokenType.Error, tokens))
    } else {
        val label = (identifier as Token.Identifier).name
        if (defined(label)) {
            val errorToken = Token.Error("Label already defined", identifier.line, identifier.column)
            return (ParsedToken(ParsedTokenType.Error, listOf(next, errorToken)))
        } else {
            labels[label] = IntExpression(listOf(identifier))
            return (ParsedToken(ParsedTokenType.Next, tokens))
        }
    }
}

private fun ParserContext.handleCalc(): ParsedToken {
    val calc = tokenProvider.consume<Token.Calc>()
    val next = tokenProvider.peek()

    if (next is Token.Identifier) {
        val identifier = tokenProvider.consume<Token.Identifier>() as Token.Identifier
        val bodyStart = tokenProvider.peek()

        if (!defined(identifier.name)) {
            when (bodyStart) {
                is Token.LBrace -> {
                    val body = consumeMacroBody()
                    if (!body.any { it is Token.Error }) {
                        mutables[identifier.name] = IntExpression(body)
                        return (ParsedToken(ParsedTokenType.Calc, listOf(calc, identifier) + body))
                    } else {
                        return (ParsedToken(ParsedTokenType.Error, listOf(calc, identifier) + body))
                    }
                }

                is Token.Number -> {
                    val number = tokenProvider.consume<Token.Number>()
                    return (ParsedToken(ParsedTokenType.Calc, listOf(calc, identifier, number)))
                }

                is Token.Identifier -> {
                    val label = tokenProvider.consume<Token.Identifier>() as Token.Identifier
                    return (ParsedToken(ParsedTokenType.Calc, listOf(calc, identifier, label)))
                }

                else -> {
                    tokenProvider.consume<Any>()
                    val errorToken = Token.Error("Expected number or expression", bodyStart.line, bodyStart.column)
                    return (ParsedToken(ParsedTokenType.Error, listOf(calc, identifier, errorToken)))

                }
            }
        } else {
            //identifier is already defined
            val errorToken = Token.Error("Identifier already defined", identifier.line, identifier.column)
            return (ParsedToken(ParsedTokenType.Error, listOf(calc, errorToken)))
        }

    } else {
        return (ParsedToken(ParsedTokenType.Error, listOf(calc, next)))
    }

}

private fun ParserContext.defineLabel(): ParsedToken {
    val colon = tokenProvider.consume<Token.Colon>()
    val identifier = tokenProvider.consume<Token.Identifier>()

    val tokens = listOf(colon, identifier)
    if (tokens.any { it is Token.Error }) {
        return (ParsedToken(ParsedTokenType.Error, tokens))
    } else {
        val label = (identifier as Token.Identifier).name
        if (defined(label)) {
            val errorToken = Token.Error("Label already defined", identifier.line, identifier.column)
            return (ParsedToken(ParsedTokenType.Error, listOf(colon, errorToken)))
        } else {
            labels[label] = IntExpression(listOf(identifier))
            return (ParsedToken(ParsedTokenType.Label, tokens))
        }
    }
}

private fun ParserContext.defined(label: String): Boolean {
    return labels.containsKey(label) || aliases.containsKey(label) ||
            constants.containsKey(label) || macros.containsKey(label) ||
            stringModes.containsKey(label)
}

class TokenProvider(val program: List<Token>) {
    var index = 0

    init {
        if (program.isEmpty()) {
            throw IllegalArgumentException("Program is empty")
        }
    }

    fun peek(): Token {
        if (index >= program.size) {
            return Token.Error("Unexpected end of program", program.last().line, program.last().column)
        }
        return program[index]
    }

    fun hasMore(): Boolean {
        return index < program.size
    }

    inline fun <reified T> consume(): Token {
        if (!hasMore()) {
            return Token.Error("Unexpected end of program", program.last().line, program.last().column)
        }
        val token = program[index]
        index += 1
        return when (token) {
            is T -> token
            else -> Token.Error(
                "Expected ${T::class.simpleName} but found ${token::class.simpleName}",
                token.line,
                token.column
            )
        }

    }

}
