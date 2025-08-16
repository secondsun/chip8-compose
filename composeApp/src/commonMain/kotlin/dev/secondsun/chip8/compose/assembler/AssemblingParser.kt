package dev.secondsun.chip8.compose.assembler

class ParserContext(program: List<Token>) {

    val constants = mutableMapOf<String, Int>()
    val labels = mutableMapOf<String, Int>()
    val aliases = mutableMapOf<String, Int>()
    val tokenProvider = TokenProvider(program)
    val parsedTokens = mutableListOf<ParsedToken>()



}

fun parse(program: String): ParserContext {
    val tokens = tokenize(program)
    return parse(tokens)
}

fun parse(program: List<Token>): ParserContext {
    val context = ParserContext(program)


    with(context) {
        while (tokenProvider.hasMore()) {
            val token = tokenProvider.peek()
            when (token) {
                is Token.AdditionAssignment -> TODO()
                is Token.Again -> TODO()
                is Token.Alias -> TODO()
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
                is Token.Next -> TODO()
                is Token.NotEqual -> TODO()
                is Token.Number -> TODO()
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
                is Token.Unpack -> TODO()
                is Token.While -> TODO()
                is Token.XorAssignment -> TODO()
            }
        }
    }
    return context

}

private fun ParserContext.defineConstant() {
    val constToken = tokenProvider.consume<Token.Const>()
    val identifier = tokenProvider.consume<Token.Identifier>()

    if (identifier is Token.Identifier) {
        if (defined(identifier.name)) {
            val errorToken = Token.Error("Constant already defined", identifier.line, identifier.column)
            parsedTokens.add(ParsedToken(ParsedTokenType.Error, listOf(constToken, errorToken)))
        } else {
            val calulateConstantResult : Pair<List<Token>, Int> = calulateConstant()
            constants[identifier.name] = calulateConstantResult.second
            val tokens = mutableListOf<Token>()
            tokens.add(constToken)
            tokens.add(identifier)
            tokens.addAll(calulateConstantResult.first)
            parsedTokens.add(ParsedToken(ParsedTokenType.CreateConstant, tokens))
        }
    } else {
        val identifierError = Token.Error("Expected identifier", identifier.line, identifier.column)
        parsedTokens.add(ParsedToken(ParsedTokenType.Error, listOf(constToken, identifierError)))
    }

}

private fun ParserContext.calulateConstant(): Pair<List<Token>, Int> {
    return when(val next = tokenProvider.peek()) {
        is Token.Number -> {
            tokenProvider.consume<Token.Number>()
            return Pair(listOf(next), next.value)
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
                    return Pair(listOf(errorToken), 0)
                }
            } else {
                val errorToken = Token.Error("undefined identifier", next.line, next.column)
                return Pair(listOf(errorToken), 0)
            }
        }
        else -> {
            tokenProvider.consume<Any>()
            val errorToken = Token.Error("Expected number", next.line, next.column)
            return Pair(listOf(errorToken), 0)
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
            labels[label] = identifier.line
            parsedTokens.add(ParsedToken(ParsedTokenType.DefineLabel, tokens))
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
