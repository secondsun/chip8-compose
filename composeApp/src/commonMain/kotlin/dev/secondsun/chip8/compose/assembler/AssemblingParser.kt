package dev.secondsun.chip8.compose.assembler

data class IntExpression(val expression: List<Token>) {
    fun evaluate(): Int {
        if (expression.isEmpty()) {
            return 0
        }
        return expression.fold(0) { acc, token ->
            when (token) {
                is Token.Number -> acc + token.value
                else -> throw IllegalArgumentException("Invalid expression")
            }
        }
    }
}

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
    val parsedTokens: List<ParsedToken>
}

class ParserContext(program: List<Token>) : ParserOutput {

    override val constants = mutableMapOf<String, IntExpression>()
    override val labels = mutableMapOf<String, IntExpression>()
    override val aliases = mutableMapOf<String, IntExpression>()
    val tokenProvider = TokenProvider(program)
    override val parsedTokens = mutableListOf<ParsedToken>()

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
                is Token.Breakpoint -> TODO()
                is Token.Buzzer -> TODO()
                is Token.Byte -> TODO()
                is Token.Calc -> TODO()
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
                is Token.Macro -> TODO()
                is Token.Moniter -> TODO()
                is Token.Native -> TODO()
                is Token.Next -> defineNext()
                is Token.NotEqual -> TODO()
                is Token.Number -> consumeNumber()
                is Token.OrAssignment -> TODO()
                is Token.Org -> TODO()
                is Token.Pitch -> TODO()
                is Token.Plane -> TODO()
                is Token.Pointer -> TODO()
                is Token.Proto -> TODO()
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
                is Token.StringMode -> TODO()
                is Token.SubtractionAssignment -> TODO()
                is Token.Then -> TODO()
                is Token.Unpack -> consumeUnpack()
                is Token.While -> TODO()
                is Token.XorAssignment -> TODO()
                is Token.Minus -> TODO()
                is Token.Plus -> TODO()
            }
        }
    }
    return context

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
            val calulatedConstantResult : Pair<List<Token>, IntExpression> = calculateAlias()
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
            val calculateConstantResult : Pair<List<Token>, IntExpression> = calculateConstant()
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
    when(val next = tokenProvider.peek()) {
        is Token.Number -> {
            tokenProvider.consume<Token.Number>()
            if (next.value !in 0..15) {
                val errorToken = Token.Error("Number out of range", next.line, next.column)
                return Pair(listOf(errorToken), IntExpression(listOf(errorToken)))
            } else {
                return Pair(listOf(next), IntExpression(listOf(next)))
            }
        } is Token.Register -> {
            tokenProvider.consume<Token.Register>()
            if (next.register == Registers.i) {
                val errorToken = Token.Error("Cannot use i register in alias", next.line, next.column)
                return Pair(listOf(errorToken), IntExpression(listOf(errorToken)))
            } else {
                return Pair(listOf(next), IntExpression(listOf(next)))
            }
        } is Token.Identifier -> {
            tokenProvider.consume<Token.Identifier>()
            return Pair(listOf(next), IntExpression(listOf(next)))
        } is Token.LBrace -> {
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
    return when(val next = tokenProvider.peek()) {
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
    return labels.containsKey(label) || aliases.containsKey(label) || constants.containsKey(label)
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

    inline fun <reified  T> consume(): Token {
        if (!hasMore()) {
            return Token.Error("Unexpected end of program", program.last().line, program.last().column)
        }
        val token = program[index]
        index += 1
        return when(token) {
            is T-> token
            else -> Token.Error("Expected ${T::class.simpleName} but found ${token::class.simpleName}", token.line, token.column)
        }

    }

}
