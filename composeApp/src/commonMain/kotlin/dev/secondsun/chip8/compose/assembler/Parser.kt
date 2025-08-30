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
            when (token) {
                is Token.AdditionAssignment -> TODO()
                is Token.Again -> TODO()
                is Token.Alias -> defineAlias()
                is Token.AndAssignment -> TODO()
                is Token.Assert -> TODO()
                is Token.Assignment -> TODO()
                is Token.Audio -> TODO()
                is Token.BCD -> TODO()
                is Token.Begin -> TODO()
                is Token.BigHex -> TODO()
                is Token.Breakpoint -> defineBreakpoint()
                is Token.Buzzer -> TODO()
                is Token.Byte -> handleByte()
                is Token.Calc -> handleCalc()
                is Token.Call -> TODO()
                is Token.Clear -> TODO()
                is Token.Colon -> defineLabel()
                is Token.Const -> defineConstant()
                is Token.Delay -> TODO()
                is Token.Else -> TODO()
                is Token.End -> TODO()
                is Token.Equal -> TODO()
                is Token.Error -> TODO()
                is Token.GreaterThan -> TODO()
                is Token.GreaterThanOrEqual -> TODO()
                is Token.Hex -> TODO()
                is Token.Hires -> TODO()
                is Token.I -> TODO()
                is Token.Identifier -> TODO()
                is Token.If -> TODO()
                is Token.Jump -> TODO()
                is Token.Jump0 -> TODO()
                is Token.Key -> TODO()
                is Token.LBrace -> TODO()
                is Token.LessThan -> TODO()
                is Token.LessThanOrEqual -> TODO()
                is Token.Load -> TODO()
                is Token.LoadFlags -> TODO()
                is Token.Loop -> TODO()
                is Token.Lores -> TODO()
                is Token.Macro -> defineMacro()
                is Token.Moniter -> defineMonitor()
                is Token.Native -> TODO()
                is Token.Next -> defineNext()
                is Token.NotEqual -> TODO()
                is Token.Number -> consumeNumber()
                is Token.OrAssignment -> TODO()
                is Token.Org -> TODO()
                is Token.Exclaimation -> TODO()
                is Token.Pitch -> TODO()
                is Token.Plane -> TODO()
                is Token.Pointer -> handlePointer()
                is Token.Proto -> {
                    //Deprecated
                    tokenProvider.consume<Token.Proto>()
                }

                is Token.RBrace -> TODO()
                is Token.Random -> TODO()
                is Token.Register -> TODO()
                is Token.Return -> TODO()
                is Token.Save -> TODO()
                is Token.SaveFlags -> TODO()
                is Token.ScrollDown -> TODO()
                is Token.ScrollLeft -> TODO()
                is Token.ScrollRight -> TODO()
                is Token.ScrollUp -> TODO()
                is Token.ShiftLeft -> TODO()
                is Token.ShiftRight -> TODO()
                is Token.Sprite -> TODO()
                is Token.StringMode -> defineStringMode()
                is Token.SubtractionAssignment -> TODO()
                is Token.Then -> TODO()
                is Token.Unpack -> consumeUnpack()
                is Token.While -> TODO()
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
                is Token.Semicolon -> TODO()
                is Token.ShiftLeftAssign -> TODO()
                is Token.ShiftRightAssign -> TODO()
                is Token.Caret -> TODO()
                is Token.Percent -> TODO()
            }
        }
    }
    return context

}

private fun ParserContext.handlePointer() {
    val pointer = tokenProvider.consume<Token.Pointer>()
    val next = tokenProvider.peek()

    when(val next =tokenProvider.peek()) {
        is Token.LBrace -> {
            val expression = consumeMacroBody()
            parsedTokens.add(ParsedToken(ParsedTokenType.Byte, listOf(pointer) + expression))
        }
        is Token.Number -> {
            tokenProvider.consume<Token.Number>()
            if (next.value !in 0..0xFFFF) {
                parsedTokens.add(ParsedToken(ParsedTokenType.Pointer, listOf(pointer, next)))
            } else {
                val error = Token.Error("Wide value out of range", next.line, next.column)
                parsedTokens.add(ParsedToken(ParsedTokenType.Error, listOf(pointer, error)))
            }
        }
        is Token.Identifier -> {
            tokenProvider.consume<Token.Identifier>()
            if (!defined(next.name)) {
                val error = Token.Error("Constant not defined", next.line, next.column)
                parsedTokens.add(ParsedToken(ParsedTokenType.Error, listOf(pointer, error)))
            } else {
                parsedTokens.add(ParsedToken(ParsedTokenType.Pointer, listOf(pointer, next)))
            }
        }
        else -> {
            tokenProvider.consume<Any>()
            val error = Token.Error("Expected Number or Macro", next.line, next.column)
            parsedTokens.add(ParsedToken(ParsedTokenType.Error, listOf(pointer, error)))
        }
    }
}


private fun ParserContext.handleByte() {
    val byte = tokenProvider.consume<Token.Byte>()
    when(val next =tokenProvider.peek()) {
        is Token.LBrace -> {
            val expression = consumeMacroBody()
            parsedTokens.add(ParsedToken(ParsedTokenType.Byte, listOf(byte) + expression))
        }
        is Token.Number -> {
            tokenProvider.consume<Token.Number>()
            if (next.value !in -128..255) {
                parsedTokens.add(ParsedToken(ParsedTokenType.Byte, listOf(byte, next)))
            } else {
                val error = Token.Error("Byte value out of range", next.line, next.column)
                parsedTokens.add(ParsedToken(ParsedTokenType.Error, listOf(byte, error)))
            }
        }
        else -> {
            val error = Token.Error("Expected Number or Macro", next.line, next.column)
            parsedTokens.add(ParsedToken(ParsedTokenType.Error, listOf(byte, error)))
        }
        }
    }



private fun ParserContext.defineStringMode() {
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
                    parsedTokens.add(ParsedToken(ParsedTokenType.StringMode, buildList { add(stringMode); add(stringModeName);add(stringModeAlphabet); addAll(stringModeBody) }))
                } catch (e: Exception) {
                    val alphabetError = Token.Error("Invalid Alphabet : ${e.message}", stringModeAlphabet.line, stringModeAlphabet.column)
                    parsedTokens.add(ParsedToken(ParsedTokenType.Error, buildList { add(stringMode); add(stringModeName);add(alphabetError); addAll(stringModeBody) }))
                }


            } else {
                parsedTokens.add(ParsedToken(ParsedTokenType.Error, listOf(stringMode, stringModeName, stringModeAlphabet) + stringModeBody))
            }
        } else {
            if(stringModeAlphabet is Token.Error)
                parsedTokens.add(ParsedToken(ParsedTokenType.Error, listOf(stringMode, stringModeName, stringModeAlphabet)))
            else {
                val errorToken = Token.Error("Expected StringToken", stringModeAlphabet.line, stringModeAlphabet.column)
                parsedTokens.add(ParsedToken(ParsedTokenType.Error, listOf(stringMode, stringModeName, errorToken)))
            }

        }

    } else {
        if (stringModeName is Token.Error)
            parsedTokens.add(ParsedToken(ParsedTokenType.Error, listOf(stringMode, stringModeName)))
        else {
            val errorToken = Token.Error("Expected Identifier", stringModeName.line, stringModeName.column)
            parsedTokens.add(ParsedToken(ParsedTokenType.Error, listOf(stringMode, errorToken)))
        }
    }

}

private fun ParserContext.defineMacro() {
    val macro = tokenProvider.consume<Token.Macro>()
    val identifier = tokenProvider.consume<Token.Identifier>()
    if (identifier is Token.Identifier) {
        if (!defined(identifier.name)) {
            val arguments: List<Token> = consumeMacroDefArguments();
            if (!arguments.any { it is Token.Error }) {
                val body: List<Token> = consumeMacroBody()
                if (!body.any { it is Token.Error }) {
                    val macroTokens = mutableListOf<Token>()

                    macroTokens.addAll(arguments)
                    macroTokens.addAll(body)
                    macros[identifier.name] = macroTokens
                    parsedTokens.add(ParsedToken(ParsedTokenType.Macro, buildList{add(macro); add(identifier); addAll(arguments); addAll(body)}))
                } else {
                    val errorWithArguments = buildList<Token> {
                        add(macro)
                        add(identifier)
                        addAll(arguments)
                        addAll(body)
                    }
                    parsedTokens.add(ParsedToken(ParsedTokenType.Error, errorWithArguments))
                }
            } else {
                val errorWithArguments = mutableListOf<Token>(macro, identifier)
                errorWithArguments.addAll(arguments)
                parsedTokens.add(ParsedToken(ParsedTokenType.Error, errorWithArguments))
            }
        } else {
            val errorToken = Token.Error("${identifier.name} is already defined", identifier.line, identifier.column)
            parsedTokens.add(ParsedToken(ParsedTokenType.Error, listOf(macro, errorToken)))
        }
    } else {
        parsedTokens.add(ParsedToken(ParsedTokenType.Error, listOf(macro, identifier)))
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

private fun ParserContext.defineMonitor() {
    val monitor = tokenProvider.consume<Token.Moniter>()
    val identifier = tokenProvider.consume<Token>()

    if (identifier is Token.Identifier) {
        if (defined(identifier.name)) {
            val errorToken = Token.Error("Monitor already defined", identifier.line, identifier.column)
            parsedTokens.add(ParsedToken(ParsedTokenType.Error, listOf(monitor, errorToken)))
            return
        }
    } else if (identifier !is Token.Register) {
        val errorToken = Token.Error("Expected Identifier or Register", identifier.line, identifier.column)
        parsedTokens.add(ParsedToken(ParsedTokenType.Error, listOf(monitor, errorToken)))
        return
    }

    val value = tokenProvider.consume<Token>()

    if (value is Token.Number) {
        parsedTokens.add(ParsedToken(ParsedTokenType.Monitor, listOf(monitor, identifier, value)))
    } else if (value is Token.StringToken) {
        parsedTokens.add(ParsedToken(ParsedTokenType.Monitor, listOf(monitor, identifier, value)))
    } else {
        val errorToken = Token.Error("Expected String or Number", value.line, value.column)

        parsedTokens.add(ParsedToken(ParsedTokenType.Error, listOf(monitor, identifier, errorToken)))
    }

}

private fun ParserContext.defineBreakpoint() {
    val breakpoint = tokenProvider.consume<Token.Breakpoint>()
    val identifier = tokenProvider.consume<Token.Identifier>()
    if (identifier is Token.Identifier) {
        if (defined(identifier.name)) {
            val errorToken = Token.Error("Breakpoint already defined", identifier.line, identifier.column)
            parsedTokens.add(ParsedToken(ParsedTokenType.Error, listOf(breakpoint, errorToken)))
        } else {
            val tokens = mutableListOf<Token>()
            tokens.add(breakpoint)
            tokens.add(identifier)
            parsedTokens.add(ParsedToken(ParsedTokenType.Breakpoint, listOf(breakpoint, identifier)))
        }
    } else {
        val identifierError = Token.Error("Expected identifier", identifier.line, identifier.column)
        parsedTokens.add(ParsedToken(ParsedTokenType.Error, listOf(breakpoint, identifierError)))
    }
}

private fun ParserContext.consumeUnpack() {
    val unpack = tokenProvider.consume<Token.Unpack>()
    val next = tokenProvider.peek()

    if (next is Token.Identifier) {
        when (next.name) {
            "long" -> {
                //consume long
                tokenProvider.consume<Token.Identifier>()
                //handle number or label
                parsedTokens.add(handleNumberOrLabel(listOf(unpack, next), ParsedTokenType.Unpack))
            }

            else -> {
                //handle number or label
                tokenProvider.consume<Token.Identifier>()
                parsedTokens.add(handleNumberOrLabel(listOf(unpack, next), ParsedTokenType.Unpack))
            }
        }
    } else if (next is Token.Number) {
        parsedTokens.add(handleNumberOrLabel(listOf(unpack), ParsedTokenType.Unpack))
    } else {
        tokenProvider.consume<Any>()
        parsedTokens.add(ParsedToken(ParsedTokenType.Error, listOf(unpack, next)))
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

private fun ParserContext.consumeNumber() {
    val number = tokenProvider.consume<Token.Number>()
    if (number is Token.Number) {
        val value = number.value
        if (value < -128 || value > 255) {
            val errorToken = Token.Error("Number out of range", number.line, number.column)
            parsedTokens.add(ParsedToken(ParsedTokenType.Error, listOf(errorToken)))
        } else {
            parsedTokens.add(ParsedToken(ParsedTokenType.Number, listOf(number)))
        }
    } else {
        val errorToken = Token.Error("Expected number", number.line, number.column)
        parsedTokens.add(ParsedToken(ParsedTokenType.Error, listOf(errorToken)))
    }
}

private fun ParserContext.defineAlias() {
    val aliasToken = tokenProvider.consume<Token.Alias>()
    val identifier = tokenProvider.consume<Token.Identifier>()
    if (identifier is Token.Identifier) {
        if (defined(identifier.name)) {
            val errorToken = Token.Error("Constant already defined", identifier.line, identifier.column)
            parsedTokens.add(ParsedToken(ParsedTokenType.Error, listOf(aliasToken, errorToken)))
        } else {
            val calulatedConstantResult: Pair<List<Token>, IntExpression> = calculateAlias()
            constants[identifier.name] = calulatedConstantResult.second
            val tokens = mutableListOf<Token>()
            tokens.add(aliasToken)
            tokens.add(identifier)
            tokens.addAll(calulatedConstantResult.first)
            if (tokens.any { it is Token.Error }) {
                parsedTokens.add(ParsedToken(ParsedTokenType.Error, tokens))
            } else {
                parsedTokens.add(ParsedToken(ParsedTokenType.Alias, tokens))
            }
        }
    } else {
        val identifierError = Token.Error("Expected identifier", identifier.line, identifier.column)
        parsedTokens.add(ParsedToken(ParsedTokenType.Error, listOf(aliasToken, identifierError)))
    }

}

private fun ParserContext.defineConstant() {
    val constToken = tokenProvider.consume<Token.Const>()
    val identifier = tokenProvider.consume<Token.Identifier>()

    if (identifier is Token.Identifier) {
        if (defined(identifier.name)) {
            val errorToken = Token.Error("Constant already defined", identifier.line, identifier.column)
            parsedTokens.add(ParsedToken(ParsedTokenType.Error, listOf(constToken, errorToken)))
        } else {
            val calculateConstantResult: Pair<List<Token>, IntExpression> = calculateConstant()
            constants[identifier.name] = calculateConstantResult.second
            val tokens = mutableListOf<Token>()
            tokens.add(constToken)
            tokens.add(identifier)
            tokens.addAll(calculateConstantResult.first)
            parsedTokens.add(ParsedToken(ParsedTokenType.Constant, tokens))
        }
    } else {
        val identifierError = Token.Error("Expected identifier", identifier.line, identifier.column)
        parsedTokens.add(ParsedToken(ParsedTokenType.Error, listOf(constToken, identifierError)))
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
                    break;
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
    return when (val next = tokenProvider.peek()) {
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

private fun ParserContext.defineNext() {
    val next = tokenProvider.consume<Token.Next>()
    val identifier = tokenProvider.consume<Token.Identifier>()

    val tokens = listOf(next, identifier)
    if (tokens.any { it is Token.Error }) {
        parsedTokens.add(ParsedToken(ParsedTokenType.Error, tokens))
    } else {
        val label = (identifier as Token.Identifier).name
        if (defined(label)) {
            val errorToken = Token.Error("Label already defined", identifier.line, identifier.column)
            parsedTokens.add(ParsedToken(ParsedTokenType.Error, listOf(next, errorToken)))
        } else {
            labels[label] = IntExpression(listOf(identifier))
            parsedTokens.add(ParsedToken(ParsedTokenType.Next, tokens))
        }
    }
}

private fun ParserContext.handleCalc() {
    val calc = tokenProvider.consume<Token.Calc>()
    val next = tokenProvider.peek()

    if (next is Token.Identifier) {
        val identifier = tokenProvider.consume<Token.Identifier>() as Token.Identifier
        val bodyStart = tokenProvider.peek()

        if (!defined(identifier.name)) {
            when(bodyStart) {
                 is Token.LBrace -> {
                    val body = consumeMacroBody()
                    if (!body.any { it is Token.Error }) {
                        mutables[identifier.name] = IntExpression(body)
                        parsedTokens.add(ParsedToken(ParsedTokenType.Calc, listOf(calc, identifier) + body))
                    } else {
                        parsedTokens.add(ParsedToken(ParsedTokenType.Error, listOf(calc, identifier) + body))
                    }
                 }
                is Token.Number -> {
                    val number = tokenProvider.consume<Token.Number>()
                    parsedTokens.add(ParsedToken(ParsedTokenType.Calc, listOf(calc, identifier, number)))
                }
                is Token.Identifier -> {
                    val label = tokenProvider.consume<Token.Identifier>() as Token.Identifier
                    parsedTokens.add(ParsedToken(ParsedTokenType.Calc, listOf(calc, identifier, label)))
                }
                else -> {
                    tokenProvider.consume<Any>()
                    val errorToken = Token.Error("Expected number or expression", bodyStart.line, bodyStart.column)
                    parsedTokens.add(ParsedToken(ParsedTokenType.Error, listOf(calc, identifier, errorToken)))
                    return
                }
            }
        } else {
            //identifier is already defined
            val errorToken = Token.Error("Identifier already defined", identifier.line, identifier.column)
            parsedTokens.add(ParsedToken(ParsedTokenType.Error, listOf(calc, errorToken)))
        }

    } else {
        parsedTokens.add(ParsedToken(ParsedTokenType.Error, listOf(calc, next)))
    }

}

private fun ParserContext.defineLabel() {
    val colon = tokenProvider.consume<Token.Colon>()
    val identifier = tokenProvider.consume<Token.Identifier>()

    val tokens = listOf(colon, identifier)
    if (tokens.any { it is Token.Error }) {
        parsedTokens.add(ParsedToken(ParsedTokenType.Error, tokens))
    } else {
        val label = (identifier as Token.Identifier).name
        if (defined(label)) {
            val errorToken = Token.Error("Label already defined", identifier.line, identifier.column)
            parsedTokens.add(ParsedToken(ParsedTokenType.Error, listOf(colon, errorToken)))
        } else {
            labels[label] = IntExpression(listOf(identifier))
            parsedTokens.add(ParsedToken(ParsedTokenType.Label, tokens))
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
