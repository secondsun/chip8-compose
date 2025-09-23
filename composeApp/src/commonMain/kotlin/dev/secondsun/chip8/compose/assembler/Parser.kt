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
    val macros: Map<String, ParsedMacroToken>
    val stringModes: Map<String, StringMode>
}

class ParserContext(program: List<Token>) : ParserOutput {

    override val constants = mutableMapOf<String, IntExpression>().apply {
        put("OCTO_KEY_1", IntExpression(listOf(Token.Number(0x1, 0, 0, 1))))
        put("OCTO_KEY_2", IntExpression(listOf(Token.Number(0x2, 0, 0, 1))))
        put("OCTO_KEY_3", IntExpression(listOf(Token.Number(0x3, 0, 0, 1))))
        put("OCTO_KEY_4", IntExpression(listOf(Token.Number(0xC, 0, 0, 1))))
        put("OCTO_KEY_Q", IntExpression(listOf(Token.Number(0x4, 0, 0, 1))))
        put("OCTO_KEY_W", IntExpression(listOf(Token.Number(0x5, 0, 0, 1))))
        put("OCTO_KEY_E", IntExpression(listOf(Token.Number(0x6, 0, 0, 1))))
        put("OCTO_KEY_R", IntExpression(listOf(Token.Number(0xD, 0, 0, 1))))
        put("OCTO_KEY_A", IntExpression(listOf(Token.Number(0x7, 0, 0, 1))))
        put("OCTO_KEY_S", IntExpression(listOf(Token.Number(0x8, 0, 0, 1))))
        put("OCTO_KEY_D", IntExpression(listOf(Token.Number(0x9, 0, 0, 1))))
        put("OCTO_KEY_F", IntExpression(listOf(Token.Number(0xE, 0, 0, 1))))
        put("OCTO_KEY_Z", IntExpression(listOf(Token.Number(0xA, 0, 0, 1))))
        put("OCTO_KEY_X", IntExpression(listOf(Token.Number(0x0, 0, 0, 1))))
        put("OCTO_KEY_C", IntExpression(listOf(Token.Number(0xB, 0, 0, 1))))
        put("OCTO_KEY_V", IntExpression(listOf(Token.Number(0xF, 0, 0, 1))))
    }
    override val labels = mutableMapOf<String, IntExpression>()
    override val aliases = mutableMapOf<String, IntExpression>()
    override val mutables = mutableMapOf<String, IntExpression>()
    val tokenProvider = TokenProvider(program)
    override val parsedTokens = mutableListOf<ParsedToken>()
    override val macros = mutableMapOf<String, ParsedMacroToken>()
    override val stringModes = mutableMapOf<String, StringMode>()
    val branches = ArrayDeque<Triple<ParsedToken, Int, String>>()
    val loops = ArrayDeque<Pair<ParsedToken, Int>>()
    val whiles = ArrayDeque<ParsedToken?>()
    val forwards = mutableMapOf<String, MutableList<Token.ForwardIdentifier>>()

}

fun parse(program: String): ParserOutput {
    val tokens = tokenize(program)
    return parse(tokens)
}

fun parse(program: List<Token>): ParserOutput {
    val context = ParserContext(program)
    context.parseTokens()
    return context

}

private fun ParserContext.parseTokens() {
    while (tokenProvider.hasMore()) {
        val token = tokenProvider.peek()
        if (token is Token.Proto) {
            //Deprecated
            tokenProvider.consume<Token.Proto>()
        } else {
            parsedTokens.add(parseOne())
        }
    }

    for (key in forwards.keys) {
        if (!defined(key)) {
            forwards[key]!!.forEach {
                parsedTokens.add(
                    ParsedToken(
                        ParsedTokenType.Error,
                        listOf(Token.Error("Undefined Token ${it.name}, ${it.line}", it.line, it.column, it.length))
                    )
                )
            }
        }
    }
}


private fun ParserContext.parseOne(): ParsedToken {
    return when (val token = tokenProvider.peek()) {
        is Token.Again -> {
            consumeAgain()
        }

        is Token.Alias -> defineAlias()
        is Token.Assert -> handleAssert()
        is Token.Audio -> {
            tokenProvider.consume<Token.Audio>()
            return (ParsedToken(ParsedTokenType.Audio, listOf(token)))
        }

        is Token.BCD -> handleBCD()
        is Token.Begin -> {
            tokenProvider.consume<Token.Begin>()
            return (ParsedToken(ParsedTokenType.Begin, listOf(token)))
        }

        is Token.Breakpoint -> defineBreakpoint()
        is Token.Buzzer -> defineBuzzer()
        is Token.Byte -> handleByte()
        is Token.Calc -> handleCalc()
        is Token.Call -> consumeCall()
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

        is Token.Error -> {
            tokenProvider.consume<Token.Error>()
            return (ParsedToken(ParsedTokenType.Error, listOf(token)))
        }

        is Token.Exit -> {
            return (ParsedToken(ParsedTokenType.Exit, listOf(tokenProvider.consume<Token.Exit>())))
        }

        is Token.Hires -> {
            return (ParsedToken(ParsedTokenType.Hires, listOf(tokenProvider.consume<Token.Hires>())))
        }



        is Token.Identifier -> {
            val name = token.name
            if (macros.containsKey(name)) {
                macroExpand()
            } else if (aliases.containsKey(name)) {
                consumeAssign()
            } else if (stringModes.containsKey(name)) {
                consumeStringMode()
            } else if (isRegister(name.lowercase())) {
                if (name.lowercase() == "i") {
                    consumeIAssign()
                } else {
                    consumeAssign()
                }
            }

            else {
                consumeCall()
            }
        }

        is Token.If -> consumeIf()
        is Token.Jump -> consumeOperatorWithWideParameter(ParsedTokenType.Jump)
        is Token.Jump0 -> consumeOperatorWithWideParameter(ParsedTokenType.Jump0)
        is Token.Load -> handleLoad()
        is Token.LoadFlags -> consumeLoadFlags()
        is Token.Loop -> consumeLoop()
        is Token.Lores -> {
            return (ParsedToken(ParsedTokenType.Lores, listOf(tokenProvider.consume<Token.Lores>())))
        }

        is Token.Macro -> defineMacro()
        is Token.Moniter -> defineMonitor()
        is Token.Native -> consumeNative()
        is Token.Next -> defineNext()
        is Token.Number -> consumeNumber()
        is Token.Org -> handleOrg()
        is Token.Pitch -> definePitch()
        is Token.Plane -> consumePlane()
        is Token.Pointer -> handlePointer()

        is Token.Return -> {
            tokenProvider.consume<Token.Return>()
            return (ParsedToken(ParsedTokenType.Return, listOf(token)))
        }

        is Token.Save -> handleSave()
        is Token.SaveFlags -> consumeSaveFlags()
        is Token.ScrollDown -> consumeScrollDown()
        is Token.ScrollLeft -> {
            return (ParsedToken(ParsedTokenType.ScrollLeft, listOf(tokenProvider.consume<Token.ScrollLeft>())))
        }

        is Token.ScrollRight -> {
            return (ParsedToken(ParsedTokenType.ScrollRight, listOf(tokenProvider.consume<Token.ScrollRight>())))
        }

        is Token.ScrollUp -> consumeScrollUp()
        is Token.Sprite -> consumeSprite()
        is Token.StringMode -> defineStringMode()
        is Token.Then -> {
            tokenProvider.consume<Token.Then>()
            return (ParsedToken(ParsedTokenType.Then, listOf(token)))
        }

        is Token.Unpack -> consumeUnpack()
        is Token.While -> consumeWhile()
        is Token.Semicolon -> {
            tokenProvider.consume<Token.Semicolon>()
            return (ParsedToken(ParsedTokenType.Return, listOf(token)))
        }

        else -> {
            handleNumberOrLabel(listOf(), ParsedTokenType.Immediate, true)
        }
    }
}

private fun ParserContext.consumeStringMode(): ParsedToken {
    val token = tokenProvider.consume<Token.Identifier>() as Token.Identifier
    val string = tokenProvider.consume<Token.StringToken>()
    if (string !is Token.StringToken) {
        val errorToken = Token.Error("Expected String Token", string.line, string.column, string.length)
        return (ParsedToken(ParsedTokenType.Error, listOf(token, errorToken)))
    }
    val stringMode = stringModes.get(token.name)

    if (stringMode == null) {
        val errorToken = Token.Error("Unknown string mode ${token.name}", token.line, token.column, token.length)
        return (ParsedToken(ParsedTokenType.Error, listOf(token)))
    }

    val output = stringMode.evaluate(string.value)
    tokenProvider.inject(output)
    return (ParsedToken(ParsedTokenType.StringMode, listOf(token)))
}

private fun ParserContext.consumeIAssign(): ParsedToken {
    val i = tokenProvider.consume<Token>()
    val operator = tokenProvider.consume<Any>()
    if (operator is Token.Assignment) {
        when (val next = tokenProvider.peek()) {
            is Token.Identifier -> {
                when (next.name) {

                    "long" -> {
                        tokenProvider.consume<Token.Identifier>()
                        return handleNumberOrLabel(listOf(i, operator, next), ParsedTokenType.IAssign, true)
                    }

                    else -> {
                        return handleNumberOrLabel(listOf(i, operator, next), ParsedTokenType.IAssign, true)
                    }
                }
            }

            is Token.Hex, is Token.BigHex -> {
                tokenProvider.consume<Any>()
                val register = tokenProvider.consume<Any>()
                if (isRegister(register)) {
                    return ParsedToken(ParsedTokenType.IAssign, listOf(i, operator, next, register))
                } else {
                    val errorToken = Token.Error("Expected Register", register.line, register.column, register.length)
                    return ParsedToken(ParsedTokenType.Error, listOf(i, operator, next, errorToken))
                }
            }

            is Token.Number -> {
                tokenProvider.consume<Token.Number>()
                return ParsedToken(ParsedTokenType.IAssign, listOf(i, operator, next))
            }

            else -> {
                tokenProvider.consume<Any>()
                val errorToken = Token.Error("Expected Identifier", next.line, next.column, next.length)
                return ParsedToken(ParsedTokenType.Error, listOf(i, operator, errorToken))
            }
        }
    } else if (operator is Token.AdditionAssignment) {
        val register = tokenProvider.consume<Any>()
        if (isRegister(register)) {
            return ParsedToken(ParsedTokenType.IAdditionAssign, listOf(i, operator, register))
        } else {
            val errorToken = Token.Error("Expected Register", register.line, register.column, register.length)
            return ParsedToken(ParsedTokenType.Error, listOf(i, operator, errorToken))
        }
    } else {
        val errorToken =
            Token.Error("Expected Assignment or AdditionAssignment", operator.line, operator.column, operator.length)
        return ParsedToken(ParsedTokenType.Error, listOf(i, operator, errorToken))
    }
}

private fun ParserContext.consumeSaveFlags(): ParsedToken {
    val saveFlags = tokenProvider.consume<Token.SaveFlags>()
    val register = tokenProvider.consume<Any>()

    if (isRegister(register)) {
        return (ParsedToken(ParsedTokenType.SaveFlags, listOf(saveFlags, register)))
    } else {
        val errorToken = Token.Error("Expected Register", register.line, register.column, register.length)
        return (ParsedToken(ParsedTokenType.Error, listOf(saveFlags, errorToken)))
    }

}

private fun ParserContext.consumeLoadFlags(): ParsedToken {
    val saveFlags = tokenProvider.consume<Token.SaveFlags>()
    val register = tokenProvider.consume<Any>()

    if (isRegister(register)) {
        return (ParsedToken(ParsedTokenType.LoadFlags, listOf(saveFlags, register)))
    } else {
        val errorToken = Token.Error("Expected Register", register.line, register.column, register.length)
        return (ParsedToken(ParsedTokenType.Error, listOf(saveFlags, errorToken)))
    }

}

private fun ParserContext.isRegister(register: Token): Boolean {
    return if (register is Token.Identifier && REGISTERS.contains(register.name.lowercase())) {
        true
    } else if (register is Token.Identifier && aliases.containsKey(register.name)) {
        true
    } else {
        false
    }
}

private fun ParserContext.isRegister(register: String): Boolean {
    return REGISTERS.contains(register.lowercase())
}

private fun ParserContext.consumeScrollDown(): ParsedToken {
    val plane = tokenProvider.consume<Token.Plane>()
    val planeNumber = tokenProvider.consume<Any>()

    if (planeNumber is Token.Identifier && defined(planeNumber.name)) {
        return (ParsedToken(ParsedTokenType.ScrollDown, listOf(plane, planeNumber)))
    } else if (planeNumber is Token.Number && planeNumber.value in 0..15) {
        return (ParsedToken(ParsedTokenType.ScrollDown, listOf(plane, planeNumber)))
    } else {
        val errorToken =
            Token.Error("Expected Identifier or 4-bit Number", planeNumber.line, planeNumber.column, planeNumber.length)
        return (ParsedToken(ParsedTokenType.Error, listOf(plane, errorToken)))
    }
}

private fun ParserContext.consumeScrollUp(): ParsedToken {
    val plane = tokenProvider.consume<Token.Plane>()
    val planeNumber = tokenProvider.consume<Any>()

    if (planeNumber is Token.Identifier && defined(planeNumber.name)) {
        return (ParsedToken(ParsedTokenType.ScrollUp, listOf(plane, planeNumber)))
    } else if (planeNumber is Token.Number && planeNumber.value in 0..15) {
        return (ParsedToken(ParsedTokenType.ScrollUp, listOf(plane, planeNumber)))
    } else {
        val errorToken =
            Token.Error("Expected Identifier or 4-bit Number", planeNumber.line, planeNumber.column, planeNumber.length)
        return (ParsedToken(ParsedTokenType.Error, listOf(plane, errorToken)))
    }
}

private fun ParserContext.consumePlane(): ParsedToken {
    val plane = tokenProvider.consume<Token.Plane>()
    val planeNumber = tokenProvider.consume<Any>()

    if (planeNumber is Token.Identifier && defined(planeNumber.name)) {
        return (ParsedToken(ParsedTokenType.Plane, listOf(plane, planeNumber)))
    } else if (planeNumber is Token.Number && planeNumber.value in 0..15) {
        return (ParsedToken(ParsedTokenType.Plane, listOf(plane, planeNumber)))
    } else {
        val errorToken =
            Token.Error("Expected Identifier or 4-bit Number", planeNumber.line, planeNumber.column, planeNumber.length)
        return (ParsedToken(ParsedTokenType.Error, listOf(plane, errorToken)))
    }
}

private fun ParserContext.consumeAgain(): ParsedToken {
    val token = tokenProvider.consume<Token.Again>()
    if (this.loops.isEmpty()) {
        val error = Token.Error("Again without loop", token.line, token.column, token.length)
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
        val error = Token.Error("End without if", token.line, token.column, token.length)
        return (ParsedToken(ParsedTokenType.Error, listOf(error)))
    }
    this.branches.removeLast()

    return (ParsedToken(ParsedTokenType.End, listOf(token)))
}

private fun ParserContext.handleElse(): ParsedToken {
    val token = tokenProvider.consume<Token.Else>()

    if (this.branches.isEmpty()) {
        val error = Token.Error("Else without if", token.line, token.column, token.length)
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
        val error = Token.Error("While without if", whileToken.line, whileToken.column, whileToken.length)
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
    } else if (register1 is Token.Identifier) {
        if (!(aliases.containsKey(register1.name) || isRegister(register1.name))) {
            val errorToken =
                Token.Error("Label ${register1.name} not defined", register1.line, register1.column, register1.length)
            return (ParsedToken(ParsedTokenType.Error, listOf(sprite, errorToken)))
        }
    }
    val register2 = tokenProvider.consume<Any>()

    if (register2 is Token.Error) {
        return (ParsedToken(ParsedTokenType.Error, listOf(sprite, register1)))
    } else if (register2 is Token.Identifier) {
        if (!(aliases.containsKey(register2.name)||isRegister(register2))) {
            val errorToken =
                Token.Error("Label ${register2.name} not defined", register2.line, register2.column, register2.length)
            return (ParsedToken(ParsedTokenType.Error, listOf(sprite, errorToken)))
        }
    }


    val size = tokenProvider.consume<Any>()
    if (size is Token.Error) {
        return (ParsedToken(ParsedTokenType.Error, listOf(sprite, register1)))
    } else if (size is Token.Identifier && !defined(size.name)) {
        val errorToken = Token.Error("Label ${size.name} not defined", size.line, size.column, size.length)
        return (ParsedToken(ParsedTokenType.Error, listOf(sprite, errorToken)))
    } else if (size is Token.Number) {
        if (size.value !in 0..15) {
            val errorToken = Token.Error("Sprite size must be between 0 and 15", size.line, size.column, size.length)
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
    val next = tokenProvider.peek()

    if (next is Token.Number) {
        if (next.value !in 0..0x0fff) {
            tokenProvider.consume<Token.Number>()
            val errorToken =
                Token.Error("Jump address must be between 0x000 and 0xfff", next.line, next.column, next.length)
            return (ParsedToken(ParsedTokenType.Error, listOf(jump0, errorToken)))
        }
    }
    return handleNumberOrLabel(listOf(jump0), jumpType, true)
}

private fun ParserContext.consumeCall(): ParsedToken {
    when (val call = tokenProvider.peek()) {
        is Token.Call -> {
            tokenProvider.consume<Token.Call>()
            val next = tokenProvider.consume<Any>()
            if (next is Token.Identifier) {
                if (labels.containsKey(next.name)) {
                    return (ParsedToken(ParsedTokenType.Call, listOf(call, next)))
                } else {
                    val forwardToken = Token.ForwardIdentifier(next.name, next.line, next.column)
                    forwards.computeIfAbsent(next.name) { mutableListOf() }.add(forwardToken)
                    return (ParsedToken(ParsedTokenType.Call, listOf(call, next)))

                }
            } else {
                val errorToken = Token.Error("Expected label", next.line, next.column, next.length)
                return (ParsedToken(ParsedTokenType.Error, listOf(call, errorToken)))
            }
        }

        is Token.Identifier -> {
            return handleNumberOrLabel(listOf(), ParsedTokenType.Call, true)
        }

        else -> {
            val errorToken = Token.Error("Expected Call or Label", call.line, call.column, call.length)
            return (ParsedToken(ParsedTokenType.Error, listOf(call, errorToken)))
        }
    }
}

private fun ParserContext.macroExpand(): ParsedToken {
    val macroName = tokenProvider.consume<Token.Identifier>() as Token.Identifier
    val macro = this.macros.get(macroName.name)!!
    val paramTokens = mutableListOf<Token>()
    macro.calls++

    if (!macro.params.isEmpty()) {
        for (token in macro.params) {
            paramTokens.add(tokenProvider.consume<Any>())
        }
        if (paramTokens.any { it is Token.Error }) {
            val error =
                Token.Error("Error parsing macro ${macroName.name}", macroName.line, macroName.column, macroName.length)
            return ParsedToken(ParsedTokenType.Error, listOf(macroName) + paramTokens + listOf(error))
        }
    }

    val expandedMacros = mutableListOf<Token>()
    for (macroToken in macro.body) {
        val params = macro.params.map { (it as Token.Identifier).name }
        if (macroToken is Token.Identifier) {
            if (macroToken.name in params) {
                expandedMacros.add(paramTokens[params.indexOf(macroToken.name)])
            } else if (macroToken.name == "CALLS") {
                expandedMacros.add(Token.Number(macro.calls, 0, 0, 1))
            } else {
                expandedMacros.add(macroToken)
            }
        } else {
            expandedMacros.add(macroToken)
        }
    }

    tokenProvider.inject(expandedMacros)

    return ParsedMacroExpandToken(listOf(macroName) + paramTokens, paramTokens)

}

private fun ParserContext.consumeAssign(): ParsedToken {
    val maybeI = tokenProvider.peek()
    if (maybeI is Token.Identifier && maybeI.name.lowercase() == "i") {
        return consumeIAssign()
    }
    val register = tokenProvider.consume<Any>()
    when (val operator = tokenProvider.consume<Any>()) {
        is Token.Assignment,
        is Token.XorAssignment,
        is Token.OrAssignment,
        is Token.AndAssignment,
        is Token.AdditionAssignment,
        is Token.ShiftLeftAssign,
        is Token.ShiftRightAssign,
        is Token.ReverseSubtractionAssignment,
        is Token.SubtractionAssignment -> {
            val parameter = tokenProvider.consume<Any>()
            if (parameter is Token.Identifier) {
                if (!(defined(parameter.name) || isRegister(parameter.name))) {
                    val errorToken =
                        Token.Error(
                            "Label ${parameter.name} not defined",
                            parameter.line,
                            parameter.column,
                            parameter.length
                        )
                    return (ParsedToken(ParsedTokenType.Error, listOf(register, operator, parameter, errorToken)))
                } else {
                    return ParsedToken(ParsedTokenType.Assignment, listOf(register, operator, parameter))
                }
            } else if (parameter is Token.Random || parameter is Token.Number || parameter is Token.Key || parameter is Token.Delay) {
                return ParsedToken(ParsedTokenType.Assignment, listOf(register, operator, parameter))
            } else {
                val errorToken =
                    Token.Error(
                        "Expected Register, Identifier, Number or Key",
                        parameter.line,
                        parameter.column,
                        parameter.length
                    )
                return (ParsedToken(ParsedTokenType.Error, listOf(register, operator, parameter, errorToken)))
            }
        }

        else -> {
            val errorToken = Token.Error("Expected Assignment", operator.line, operator.column, operator.length)
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
                        beginOrThen.column,
                        beginOrThen.length
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
    val token = tokenProvider.consume<Token>()

    if (token is Token.Identifier) {
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
                if (nextToken is Token.Identifier || nextToken is Token.Number) {
                    return ParsedToken(ParsedTokenType.Condition, listOf(token, check, nextToken))
                } else {
                    val nextTokenError =
                        Token.Error(
                            "Expected Register or Identifier",
                            nextToken.line,
                            nextToken.column,
                            nextToken.length
                        )
                    return ParsedToken(ParsedTokenType.Error, listOf(token, check, nextTokenError))
                }
            }

            else -> {
                val checkError =
                    Token.Error(
                        "Invalid condition check ${check.javaClass.simpleName}",
                        token.line,
                        token.column,
                        token.length
                    )
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
         if (register is Token.Identifier) {
            if (!(defined(register.name) || isRegister(register))) {
                val errorToken =
                    Token.Error("Label ${register.name} not defined", register.line, register.column, register.length)
                return (ParsedToken(ParsedTokenType.Error, listOf(pitch, assign, errorToken)))
            } else {
                return (ParsedToken(ParsedTokenType.Pitch, listOf(pitch, assign, register)))
            }
        } else {
            val errorToken = Token.Error("Expected Register or Label", register.line, register.column, register.length)
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
        if (register is Token.Identifier && isRegister(register.name)) {
            return (ParsedToken(ParsedTokenType.Buzzer, listOf(buzzer, assign, register)))
        } else if (register is Token.Identifier) {
            if (!defined(register.name)) {
                val errorToken =
                    Token.Error("Label ${register.name} not defined", register.line, register.column, register.length)
                return (ParsedToken(ParsedTokenType.Error, listOf(buzzer, assign, errorToken)))
            } else {
                return (ParsedToken(ParsedTokenType.Buzzer, listOf(buzzer, assign, register)))
            }
        } else {
            val errorToken = Token.Error("Expected Register or Label", register.line, register.column, register.length)
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
        if (register is Token.Identifier) {
            if (!(isRegister(register) || defined(register.name))) {
                val errorToken =
                    Token.Error("Label ${register.name} not defined", register.line, register.column, register.length)
                return (ParsedToken(ParsedTokenType.Error, listOf(delay, assign, errorToken)))
            } else {
                return (ParsedToken(ParsedTokenType.Delay, listOf(delay, assign, register)))
            }
        } else {
            val errorToken = Token.Error("Expected Register or Label", register.line, register.column, register.length)
            return (ParsedToken(ParsedTokenType.Error, listOf(delay, assign, errorToken)))
        }
    }

}


private fun ParserContext.handleLoad(): ParsedToken {
    val load = tokenProvider.consume<Token.Load>()
    val reg = tokenProvider.peek()
    var register1 = if (reg is Token.Identifier && isRegister(reg.name)) {
        tokenProvider.consume<Token.Identifier>()
        Registers.valueOf(reg.name.lowercase())
    } else if (reg is Token.Identifier) {
        tokenProvider.consume<Token.Identifier>()
        if (reg.name in aliases.keys) {
            Registers.entries[aliases[reg.name]!!.evaluate()]
        } else {
            val errorToken = Token.Error("Label ${reg.name} not defined", reg.line, reg.column, reg.length)
            return (ParsedToken(ParsedTokenType.Error, listOf(load, errorToken)))
        }
    } else {
        val errorToken = Token.Error("Expected Register or Label", reg.line, reg.column, reg.length)
        return (ParsedToken(ParsedTokenType.Error, listOf(load, errorToken)))
    }


    if (tokenProvider.hasMore()) {
        val next = tokenProvider.peek()
        if (next is Token.Minus) {
            val minus = tokenProvider.consume<Token.Minus>()
            val reg2 = tokenProvider.peek()
            var register2 = if (reg2 is Token.Identifier && isRegister(reg2.name)) {
                tokenProvider.consume<Token.Identifier>()

                Registers.valueOf(reg2.name.lowercase())
            } else if (reg2 is Token.Identifier) {
                tokenProvider.consume<Token.Identifier>()
                if (reg2.name in aliases.keys) {
                    Registers.entries[aliases[reg2.name]!!.evaluate()]
                } else {
                    val errorToken = Token.Error("Label ${reg2.name} not defined", reg.line, reg.column, reg.length)
                    return (ParsedToken(ParsedTokenType.Error, listOf(load, errorToken)))
                }
            } else {
                val errorToken = Token.Error("Expected Register or Label", reg.line, reg.column, reg.length)
                return (ParsedToken(ParsedTokenType.Error, listOf(load, errorToken)))
            }
            return (ParsedToken(ParsedTokenType.Load, listOf(load, reg, minus, reg2)))

        }
        return (ParsedToken(ParsedTokenType.Load, listOf(load, reg)))
    }
    return (ParsedToken(ParsedTokenType.Load, listOf(load, reg)))

}

private fun ParserContext.handleSave(): ParsedToken {
    val save = tokenProvider.consume<Token.Save>()
    val reg = tokenProvider.peek()
    var register1 = if (reg is Token.Identifier && isRegister(reg.name)) {
        tokenProvider.consume<Token.Identifier>()
        Registers.valueOf(reg.name.lowercase())
    } else if (reg is Token.Identifier) {
        tokenProvider.consume<Token.Identifier>()
        if (reg.name in aliases.keys) {
            Registers.entries[aliases[reg.name]!!.evaluate()]
        } else {
            val errorToken = Token.Error("Label ${reg.name} not defined", reg.line, reg.column, reg.length)
            return (ParsedToken(ParsedTokenType.Error, listOf(save, errorToken)))
        }
    } else {
        val errorToken = Token.Error("Expected Register or Label", reg.line, reg.column, reg.length)
        return (ParsedToken(ParsedTokenType.Error, listOf(save, errorToken)))
    }


    if (tokenProvider.hasMore()) {
        val next = tokenProvider.peek()
        if (next is Token.Minus) {
            val minus = tokenProvider.consume<Token.Minus>()
            val reg2 = tokenProvider.peek()
            var register2 = if (reg2 is Token.Identifier && isRegister(reg2.name)) {
                tokenProvider.consume<Token.Identifier>()
                Registers.valueOf(reg2.name.lowercase())
            } else if (reg2 is Token.Identifier) {
                tokenProvider.consume<Token.Identifier>()
                if (reg2.name in aliases.keys) {
                    Registers.entries[aliases[reg2.name]!!.evaluate()]
                } else {
                    val errorToken = Token.Error("Label ${reg2.name} not defined", reg.line, reg.column, reg.length)
                    return (ParsedToken(ParsedTokenType.Error, listOf(save, errorToken)))
                }
            } else {
                val errorToken = Token.Error("Expected Register or Label", reg.line, reg.column, reg.length)
                return (ParsedToken(ParsedTokenType.Error, listOf(save, errorToken)))
            }
            return (ParsedToken(ParsedTokenType.Save, listOf(save, reg, minus, reg2)))

        }
        return (ParsedToken(ParsedTokenType.Save, listOf(save, reg)))
    }
    return (ParsedToken(ParsedTokenType.Save, listOf(save, reg)))
}

private fun ParserContext.handleBCD(): ParsedToken {
    val bcd = tokenProvider.consume<Token.BCD>()
    val next = tokenProvider.peek()
    if (next is Token.Identifier && isRegister(next.name)) {
        tokenProvider.consume<Token.Identifier>()
        return (ParsedToken(ParsedTokenType.BCD, listOf(bcd, next)))
    } else if (next is Token.Identifier) {
        tokenProvider.consume<Token.Identifier>()
        if (defined(next.name)) {
            return (ParsedToken(ParsedTokenType.BCD, listOf(bcd, next)))
        } else {
            val errorToken = Token.Error("Label ${next.name} not defined", next.line, next.column, next.length)
            return (ParsedToken(ParsedTokenType.Error, listOf(bcd, errorToken)))
        }
    } else {
        val errorToken = Token.Error("Expected Register or Label", next.line, next.column, next.length)
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
    val pointer = tokenProvider.consume<Token.Org>()
    when (val next = tokenProvider.peek()) {
        is Token.LBrace -> {
            val expression = consumeMacroBody()
            return (ParsedToken(ParsedTokenType.Org, listOf(pointer) + expression))
        }

        is Token.Number -> {
            tokenProvider.consume<Token.Number>()
            if (next.value in 0..0xFFFF) {
                return (ParsedToken(ParsedTokenType.Org, listOf(pointer, next)))
            } else {
                val error = Token.Error("Wide value out of range", next.line, next.column, next.length)
                return (ParsedToken(ParsedTokenType.Error, listOf(pointer, error)))
            }
        }

        is Token.Identifier -> {
            if (defined(next.name)) {
                tokenProvider.consume<Token.Identifier>()
                return (ParsedToken(ParsedTokenType.Org, listOf(pointer, next)))
            } else {
                val error = Token.Error("Label ${next.name} not defined", next.line, next.column, next.length)
                return (ParsedToken(ParsedTokenType.Error, listOf(pointer, error)))
            }
        }

        else -> {
            tokenProvider.consume<Any>()
            val error = Token.Error("Expected Number or Macro", next.line, next.column, next.length)
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
            if (next.value in 0..0xFFFF) {
                return (ParsedToken(ParsedTokenType.Pointer, listOf(pointer, next)))
            } else {
                val error = Token.Error("Wide value out of range", next.line, next.column, next.length)
                return (ParsedToken(ParsedTokenType.Error, listOf(pointer, error)))
            }
        }

        is Token.Identifier -> {
            //Pointer can have forward-references
            return handleNumberOrLabel(listOf(pointer), ParsedTokenType.Pointer, true)
        }

        else -> {
            tokenProvider.consume<Any>()
            val error = Token.Error("Expected Number or Macro", next.line, next.column, next.length)
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
                val error = Token.Error("Byte value out of range", next.line, next.column, next.length)
                return (ParsedToken(ParsedTokenType.Error, listOf(byte, error)))
            }
        }

        is Token.Identifier -> {
            tokenProvider.consume<Token.Identifier>()
            if (defined(next.name)) {
                return (ParsedToken(ParsedTokenType.Byte, listOf(byte, next)))
            } else {
                val error = Token.Error("Constant value not found", next.line, next.column, next.length)
                return (ParsedToken(ParsedTokenType.Error, listOf(byte, error)))
            }
        }

        else -> {
            val error = Token.Error("Expected Number or Macro", next.line, next.column, next.length)
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
                    mode.addAlphabet(stringModeAlphabet.value, stringModeBody.subList(1, stringModeBody.size - 1))
                    return (ParsedToken(
                        ParsedTokenType.StringMode,
                        buildList { add(stringMode); add(stringModeName); add(stringModeAlphabet); addAll(stringModeBody) }))
                } catch (e: Exception) {
                    val alphabetError = Token.Error(
                        "Invalid Alphabet : ${e.message}",
                        stringModeAlphabet.line,
                        stringModeAlphabet.column,
                        stringModeAlphabet.length
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
                val errorToken = Token.Error(
                    "Expected StringToken",
                    stringModeAlphabet.line,
                    stringModeAlphabet.column,
                    stringModeAlphabet.length
                )
                return (ParsedToken(ParsedTokenType.Error, listOf(stringMode, stringModeName, errorToken)))
            }

        }

    } else {
        if (stringModeName is Token.Error)
            return (ParsedToken(ParsedTokenType.Error, listOf(stringMode, stringModeName)))
        else {
            val errorToken =
                Token.Error("Expected Identifier", stringModeName.line, stringModeName.column, stringModeName.length)
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
                    val toReturn = ParsedMacroToken(
                        buildList { add(macro); add(identifier); addAll(arguments); addAll(body) },
                        arguments,
                        body.subList(1, body.size - 1)
                    )
                    macros[identifier.name] = toReturn
                    return toReturn
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
            val errorToken = Token.Error(
                "${identifier.name} is already defined",
                identifier.line,
                identifier.column,
                identifier.length
            )
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
            toReturn.add(Token.Error("Expected Identifier", token.line, token.column, token.length))
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
    val identifier = tokenProvider.consume<Token.Identifier>()

    val value = tokenProvider.consume<Token>()

    if (value is Token.Number) {
        return (ParsedToken(ParsedTokenType.Monitor, listOf(monitor, identifier, value)))
    } else if (value is Token.StringToken) {
        return (ParsedToken(ParsedTokenType.Monitor, listOf(monitor, identifier, value)))
    } else if (value is Token.Identifier){
        if (defined(value.name)) {
            return (ParsedToken(ParsedTokenType.Monitor, listOf(monitor, identifier, value)))
        } else {
            val errorToken = Token.Error("Constant value not found", value.line, value.column, value.length)
            return (ParsedToken(ParsedTokenType.Error, listOf(monitor, identifier, errorToken)))
        }
    } else {
        val errorToken = Token.Error("Expected String or Number", value.line, value.column, value.length)
        return (ParsedToken(ParsedTokenType.Error, listOf(monitor, identifier, errorToken)))
    }

}

private fun ParserContext.defineBreakpoint(): ParsedToken {
    val breakpoint = tokenProvider.consume<Token.Breakpoint>()
    val identifier = tokenProvider.consume<Token.Identifier>()
    if (identifier is Token.Identifier) {
        if (defined(identifier.name)) {
            val errorToken =
                Token.Error("Breakpoint already defined", identifier.line, identifier.column, identifier.length)
            return (ParsedToken(ParsedTokenType.Error, listOf(breakpoint, errorToken)))
        } else {
            val tokens = mutableListOf<Token>()
            tokens.add(breakpoint)
            tokens.add(identifier)
            return (ParsedToken(ParsedTokenType.Breakpoint, listOf(breakpoint, identifier)))
        }
    } else {
        val identifierError = Token.Error("Expected identifier", identifier.line, identifier.column, identifier.length)
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
                return (handleNumberOrLabel(listOf(unpack, next), ParsedTokenType.Unpack, true))
            }

            else -> {
                //handle number or label
                tokenProvider.consume<Token.Identifier>()
                return (handleNumberOrLabel(listOf(unpack, next), ParsedTokenType.Unpack, true))
            }
        }
    } else if (next is Token.Number) {
        tokenProvider.consume<Token.Number>()
        return (handleNumberOrLabel(listOf(unpack, next), ParsedTokenType.Unpack, true))
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
private fun ParserContext.handleNumberOrLabel(
    tokens: List<Token>,
    parsedTokenType: ParsedTokenType,
    forward: Boolean = false
): ParsedToken {
    val next = tokenProvider.peek()
    val tokensList = tokens.toMutableList()

    return when (next) {
        is Token.Identifier -> {
            tokenProvider.consume<Token.Identifier>()
            if (defined(next.name)) {
                tokensList.add(next)
                ParsedToken(parsedTokenType, tokensList)
            } else if (forward) {
                val forwardToken = Token.ForwardIdentifier(next.name, next.line, next.column)
                forwards.computeIfAbsent(next.name) { mutableListOf() }.add(forwardToken)
                tokensList.add(forwardToken)
                ParsedToken(parsedTokenType, tokensList)
            } else {
                val errorToken = Token.Error("Label ${next.name} not defined", next.line, next.column, next.length)
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
            val errorToken = Token.Error("Invalid Token", next.line, next.column, next.length)
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
            val errorToken = Token.Error("Number out of range", number.line, number.column, number.length)
            return (ParsedToken(ParsedTokenType.Error, listOf(errorToken)))
        } else {
            return (ParsedToken(ParsedTokenType.Number, listOf(number)))
        }
    } else {
        val errorToken = Token.Error("Expected number", number.line, number.column, number.length)
        return (ParsedToken(ParsedTokenType.Error, listOf(errorToken)))
    }
}

private fun ParserContext.defineAlias(): ParsedToken {
    val aliasToken = tokenProvider.consume<Token.Alias>()
    val identifier = tokenProvider.consume<Token.Identifier>()
    if (identifier is Token.Identifier) {
        if (defined(identifier.name)) {
            val errorToken =
                Token.Error("Constant already defined", identifier.line, identifier.column, identifier.length)
            return (ParsedToken(ParsedTokenType.Error, listOf(aliasToken, errorToken)))
        } else {
            val calulatedConstantResult: Pair<List<Token>, IntExpression> = calculateAlias()
            aliases[identifier.name] = calulatedConstantResult.second
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
        val identifierError = Token.Error("Expected identifier", identifier.line, identifier.column, identifier.length)
        return (ParsedToken(ParsedTokenType.Error, listOf(aliasToken, identifierError)))
    }

}

private fun ParserContext.defineConstant(): ParsedToken {
    val constToken = tokenProvider.consume<Token.Const>()
    val identifier = tokenProvider.consume<Token.Identifier>()

    if (identifier is Token.Identifier) {
        if (defined(identifier.name)) {
            val errorToken =
                Token.Error("Constant already defined", identifier.line, identifier.column, identifier.length)
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
        val identifierError = Token.Error("Expected identifier", identifier.line, identifier.column, identifier.length)
        return (ParsedToken(ParsedTokenType.Error, listOf(constToken, identifierError)))
    }

}

private fun ParserContext.calculateAlias(): Pair<List<Token>, IntExpression> {
    when (val next = tokenProvider.peek()) {
        is Token.Number -> {
            tokenProvider.consume<Token.Number>()
            if (next.value !in 0..15) {
                val errorToken = Token.Error("Number out of range", next.line, next.column, next.length)
                return Pair(listOf(errorToken), IntExpression(listOf(errorToken)))
            } else {
                return Pair(listOf(next), IntExpression(listOf(next)))
            }
        }


        is Token.Identifier -> {
            tokenProvider.consume<Token.Identifier>()
            if (isRegister(next)) {
                if (next.name.lowercase() == "i") {
                    val errorToken = Token.Error("Cannot use i register in alias", next.line, next.column, next.length)
                    return Pair(listOf(errorToken), IntExpression(listOf(errorToken)))
                } else {
                    return Pair(listOf(next), IntExpression(listOf(next)))
                }
            } else if (!defined(next.name)) {
                val forwardToken = Token.ForwardIdentifier(next.name, next.line, next.column)
                forwards.computeIfAbsent(next.name) { mutableListOf() }.add(forwardToken)
                return Pair(listOf(forwardToken), IntExpression(listOf(next)))
            } else {
                return Pair(listOf(next), IntExpression(listOf(next)))
            }
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
            val errorToken = Token.Error("Expected number or expression", next.line, next.column, next.length)
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
                    val errorToken = Token.Error("undefined identifier", next.line, next.column, next.length)
                    return Pair(listOf(errorToken), IntExpression(listOf(errorToken)))
                }
            } else {
                val errorToken = Token.Error("undefined identifier", next.line, next.column, next.length)
                return Pair(listOf(errorToken), IntExpression(listOf(errorToken)))
            }
        }

        else -> {
            tokenProvider.consume<Any>()
            val errorToken = Token.Error("Expected number", next.line, next.column, next.length)
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
            val errorToken = Token.Error("Label already defined", identifier.line, identifier.column, identifier.length)
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
        when (val bodyStart = tokenProvider.peek()) {
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
                mutables[identifier.name] = IntExpression(listOf(number))
                return (ParsedToken(ParsedTokenType.Calc, listOf(calc, identifier, number)))
            }

            is Token.Identifier -> {
                val label = tokenProvider.consume<Token.Identifier>() as Token.Identifier
                mutables[identifier.name] = IntExpression(listOf(label))
                return (ParsedToken(ParsedTokenType.Calc, listOf(calc, identifier, label)))
            }

            else -> {
                tokenProvider.consume<Any>()
                val errorToken =
                    Token.Error("Expected number or expression", bodyStart.line, bodyStart.column, bodyStart.length)
                return (ParsedToken(ParsedTokenType.Error, listOf(calc, identifier, errorToken)))

            }
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
            val errorToken = Token.Error("Label already defined", identifier.line, identifier.column, identifier.length)
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
            stringModes.containsKey(label) || mutables.containsKey(label)
}

class TokenProvider(val programIn: List<Token>) {

    val program = programIn.toMutableList()

    var index = 0

    init {
        if (program.isEmpty()) {
            throw IllegalArgumentException("Program is empty")
        }
    }

    fun peek(): Token {
        if (index >= program.size) {
            return Token.Error("Unexpected end of program", program.last().line, program.last().column, 1)
        }
        return program[index]
    }

    fun inject(tokens: List<Token>) {
        program.addAll(index, tokens)
    }

    fun hasMore(): Boolean {
        return index < program.size
    }

    inline fun <reified T> consume(): Token {
        if (!hasMore()) {
            return Token.Error("Unexpected end of program", program.last().line, program.last().column, 1)
        }
        val token = program[index]
        index += 1
        return when (token) {
            is T -> token
            else -> Token.Error(
                "Expected ${T::class.simpleName} but found ${token::class.simpleName}",
                token.line,
                token.column,
                token::class.simpleName!!.length
            )
        }

    }

}
