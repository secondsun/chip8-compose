package dev.secondsun.chip8.compose.assembler

import java.lang.Integer.parseInt
import kotlin.text.get


fun tokenize(program: String): List<Token> {


    /**
     * The current index into the program string.
     */
    var index = 0

    /**
     * The current line number.
     */
    var line = 0

    /**
     * The current column number.
     */
    var column = 0

    val tokens = mutableListOf<Token>()

    /**
     * This is a convenience method for determining if we have reached the end of the program.
     * It will also check if the thread has been interrupted.
     *
     * @return true if we can continue parsing, false if we should stop.
     */
    fun canContinue(): Boolean {
        if (Thread.interrupted()) {
            throw InterruptedException("Aborted by interruption")
        }
        return index < program.length
    }

    /**
     * Advance the tokenizer state to the next line. This should be called when a newline is consumed.
     * It advances the line, index, and column accordingly but does NOT handle whitespace or do any processing.
     */
    fun nextLine() {

        index++

        //Handle windows \r\n
        if (canContinue() && (program[index] == '\r' || program[index] == '\n')) {
            index++
        }

        column = 0
        line++
    }

    /**
     * Advance the tokenizer state to the next character. This should be called when a character is consumers.
     * It advances the column and index accordingly but does NOT handle newlines or do any processing.
     */
    fun nextCharacter() {
        column++
        index++
    }


    fun colon() {
        tokens.add(Token.Colon(line, column))
        nextCharacter()
    }

    fun consumeWhitespace() {
        var character = program[index]
        while (character.isWhitespace()) {
            if (isNewline(character)) {
                nextLine()
            } else {
                nextCharacter()
            }
            if (index >= program.length) {
                break
            }
            character = program[index]
        }

    }

    fun consumeComment() {
        nextCharacter()//Consume #
        var character = program[index]
        while (canContinue()) {
            if (isNewline(character)) {
                nextLine()
                break;
            } else {
                nextCharacter()
            }
            if (!canContinue()) {
                break
            }
            character = program[index]
        }

    }

    fun consumePlusMinus() {
        val startColumn = column
        val character = program[index]
        nextCharacter()

        if (character == '+') {
            tokens.add(Token.Plus(line, startColumn))
        } else if (character == '-') {
            tokens.add(Token.Minus(line, startColumn))
        } else {
            tokens.add(Token.Error("Unexpected token $character at $line, $startColumn", line, startColumn))
        }

    }

    fun consumeString() {
        val startColumn = column
        val startLine = line
        val startIndex = index
        val stringBuilder = StringBuilder()

        nextCharacter()

        if (!canContinue()) {
            tokens.add(Token.Error("Unterminated string literal at $startLine, $startColumn", line, startColumn))
            return
        }

        while (canContinue()) {

            val c = program[index]
            when (c) {
                '"' -> {nextCharacter();break}
                '\\' -> {
                    nextCharacter()
                    if (!canContinue()) {
                        tokens.add(Token.Error("Unterminated string literal at $startLine, $startColumn", line, startColumn))
                        return
                    }
                    val esc = program[index]
                    when (esc) {
                        '"' -> stringBuilder.append('"')
                        't' -> stringBuilder.append('\t')
                        '0' -> stringBuilder.append("\\0")
                        'v' -> stringBuilder.append("\\v")
                        '\\' -> stringBuilder.append('\\')
                        'n' -> stringBuilder.append('\n')
                        'r' -> stringBuilder.append('\r')
                        else -> {
                            tokens.add(
                                Token.Error(
                                    "Invalid escape character '$esc' in string literal at $startLine, $startColumn",line, column
                                )
                            )
                        }
                    }
                }
                else -> stringBuilder.append(c)
            }

            nextCharacter()
            if (!canContinue()) {
                tokens.add(Token.Error("Unterminated string literal at $startLine, $startColumn", line, startColumn))
                return
            }
        }
        tokens.add(Token.StringToken(stringBuilder.toString(), line, startColumn))
    }
    fun consumeNumber() {
        val startColumn = column
        val startLine = line
        val numberBuilder = StringBuilder()
        var sign = 1
        var character = program[index]

        if (character == '-') {
            sign = -1
            nextCharacter()
            if (canContinue()) {
                character = program[index]
            }
        } else if (character == '+') {
            sign = 1
            nextCharacter()
            if (canContinue()) {
                character = program[index]
            }
        }

        while (canContinue() && !character.isWhitespace()) {

            numberBuilder.append(character)
            nextCharacter()
            if (canContinue()) {
                character = program[index]
                if (character.isWhitespace()) {
                    break;
                }
            }
        }

        val numberString = numberBuilder.toString()
        try {
            if (numberString.startsWith("0")) {
                if (numberString.length > 1) {
                    val secondDigit = numberString[1]
                    if (secondDigit.isDigit()) {
                        tokens.add(Token.Number(sign * parseInt(numberString, 8), line, startColumn))
                    } else {
                        when (secondDigit) {
                            'x' -> tokens.add(
                                Token.Number(
                                    sign * parseInt(numberString.substring(2), 16),
                                    line,
                                    startColumn
                                )
                            )

                            'b' -> tokens.add(
                                Token.Number(
                                    sign * parseInt(numberString.substring(2), 2),
                                    line,
                                    startColumn
                                )
                            )

                            else -> throw IllegalStateException("Unexpected number format $numberString at $startLine:$startColumn")

                        }
                    }
                } else {//Number is exactly zero
                    tokens.add(Token.Number(0, line, startColumn))
                }
            } else {
                tokens.add(Token.Number(sign * parseInt(numberString), line, startColumn))
            }
        } catch (e: NumberFormatException) {
            throw IllegalStateException("Unexpected number format $numberString at $startLine:$startColumn")
        }

    }

    fun consumeIdentifierOrDirectiveOrRegister() {
        val startColumn = column
        val identifierBuilder = StringBuilder()
        var character = program[index]
        while (!character.isWhitespace()) {
            identifierBuilder.append(character)
            nextCharacter()
            if (index >= program.length) {
                break
            }
            character = program[index]
        }

        val identifier = identifierBuilder.toString()

        if (REGISTERS.contains(identifier)) {
            tokens.add(Token.makeRegister(identifier, line, startColumn))
        } else if (DIRECTIVES.contains(identifier)) {
            tokens.add(Token.makeDirective(identifier, line, startColumn))
        } else {
            tokens.add(Token.Identifier(identifier, line, startColumn))
        }
    }


    fun consumeErrorToken() {
        val startColumn = column
        val unidentifierTokenBuilder = StringBuilder()
        var character = program[index]
        while (!character.isWhitespace()) {
            unidentifierTokenBuilder.append(character)
            nextCharacter()
            if (index >= program.length) {
                break
            }
            character = program[index]
        }
        tokens.add(
            Token.Error(
                "Unidentified token ${unidentifierTokenBuilder.toString()} at $line, $startColumn",
                line,
                startColumn
            )
        )
    }

    fun consumeBinaryAssignment() {
        val startColumn = column
        val character = program[index]
        nextCharacter()
        val nextCharacter = program[index]

        if (character == '&' && nextCharacter == '=') {
            tokens.add(Token.AndAssignment(line, startColumn))
        } else if (character == '|' && nextCharacter == '=') {
            tokens.add(Token.OrAssignment(line, startColumn))
        } else if (character == '^' && nextCharacter == '=') {
            tokens.add(Token.XorAssignment(line, startColumn))
        } else {
            tokens.add(
                Token.Error(
                    "Unexpected token $character$nextCharacter at $line, $startColumn",
                    line,
                    startColumn
                )
            )
        }
        nextCharacter()
    }

    fun consumeAddSubAssignment() {
        val startColumn = column
        val character = program[index]
        nextCharacter()
        val nextCharacter = program[index]

        if (character == '-' && nextCharacter == '=') {
            tokens.add(Token.SubtractionAssignment(line, startColumn))
        } else if (character == '+' && nextCharacter == '=') {
            tokens.add(Token.AdditionAssignment(line, startColumn))
        } else {
            tokens.add(
                Token.Error(
                    "Unexpected token $character$nextCharacter at $line, $startColumn",
                    line,
                    startColumn
                )
            )
        }
        nextCharacter()
    }


    fun consumeBinaryOperator() {
        val startColumn = column
        val character = program[index]
        when (character) {
            '|' -> tokens.add(Token.BinaryOr(line, startColumn))
            '&' -> tokens.add(Token.BinaryAnd(line, startColumn))

        }
        nextCharacter()
    }


    fun consumeBrace() {
        val startColumn = column
        var character = program[index]
        when (character) {
            '{' -> tokens.add(Token.LBrace(line, startColumn))
            '}' -> tokens.add(Token.RBrace(line, startColumn))

        }
        nextCharacter()

    }
    fun consumeParen() {
        val startColumn = column
        var character = program[index]
        when (character) {
            '(' -> tokens.add(Token.LParen(line, startColumn))
            ')' -> tokens.add(Token.RParen(line, startColumn))

        }
        nextCharacter()

    }

    fun consumeAt() {
        val startColumn = column
        var character = program[index]
        tokens.add(Token.At(line, startColumn))
        nextCharacter()

    }

    fun consumeSemi() {
        val startColumn = column
        var character = program[index]
        tokens.add(Token.Semicolon(line, startColumn))

        nextCharacter()

    }


    fun consumeDivide() {
        val startColumn = column
        var character = program[index]
        when (character) {
            '/' -> tokens.add(Token.Divide(line, startColumn))

        }
        nextCharacter()

    }

    fun consumeMultiply() {
        val startColumn = column
        var character = program[index]
        when (character) {
            '*' -> tokens.add(Token.Multiply(line, startColumn))
        }
        nextCharacter()

    }


    fun consumeComparison() {
        val startColumn = column
        val character = program[index]
        nextCharacter()
        if (canContinue()) {
        val nextCharacter = program[index]

        when (character) {
            '=' -> if (nextCharacter == '=') {
                tokens.add(Token.Equal(line, startColumn))
                nextCharacter()
            } else {
                consumeErrorToken()
            }

            '!' -> if (nextCharacter == '=') {
                tokens.add(Token.NotEqual(line, startColumn))
                nextCharacter()
            } else {
                consumeErrorToken()
            }

            '>' -> if (nextCharacter == '=') {
                tokens.add(Token.GreaterThanOrEqual(line, startColumn))
                nextCharacter()
            } else {
                tokens.add(Token.GreaterThan(line, startColumn))
            }

            '<' -> if (nextCharacter == '=') {
                tokens.add(Token.LessThanOrEqual(line, startColumn))
                nextCharacter()
            } else {
                tokens.add(Token.LessThan(line, startColumn))
            }
        }
        } else {
            tokens.add(
                Token.Error(
                    "Unexpected end of file after $character at $line, $startColumn",
                    line,
                    startColumn
                )
            )
        }


    }

    fun consumeShift() {
        val startColumn = column
        val character = program[index]
        nextCharacter()
        val nextCharacter = program[index]

        if (character == '>' && nextCharacter == '>') {
            nextCharacter()
            if (program[index] == '=') {
                nextCharacter()
                tokens.add(Token.ShiftRightAssign(line, startColumn))
            } else {
                tokens.add(Token.ShiftRight(line, startColumn))
            }
        } else if (character == '<' && nextCharacter == '<') {
            nextCharacter()
            if (program[index] == '=') {
                nextCharacter()
                tokens.add(Token.ShiftLeftAssign(line, startColumn))

            } else {
                tokens.add(Token.ShiftLeft(line, startColumn))
            }
        } else {
            nextCharacter()
            tokens.add(
                Token.Error(
                    "Unexpected token $character$nextCharacter at $line, $startColumn",
                    line,
                    startColumn
                )
            )
        }


    }

    /**
     * Return the next string of tokens until a whitespace is encountered.
     */
    fun peekNextWord(): String {

        val backupIndex = index
        val backupLine = line
        val backupColumn = column

        var toReturn = ""

        nextCharacter()
        if (canContinue()) {

            var character = program[index] // return label
                val identifierBuilder = StringBuilder()
                while (!character.isWhitespace()) {
                    identifierBuilder.append(character)
                    nextCharacter()
                    if (index >= program.length) {
                        break
                    }
                    character = program[index]
                }
                toReturn = identifierBuilder.toString()

        }

        index = backupIndex
        column = backupColumn
        line = backupLine

        return toReturn

    }

    while (canContinue()) {
        val character = program[index]
        if (character == ':') { //Start directive
            val nextToken = peekNextWord()
            if (DIRECTIVES.contains(":$nextToken")) {
                consumeIdentifierOrDirectiveOrRegister()
            } else {
                colon()
            }
        } else if (character == '#') { //Start directive
            consumeComment()
        } else if (character.isWhitespace()) { //consume whitespace
            consumeWhitespace()
        } else if (character.isLetter()) { // consume identifier
            consumeIdentifierOrDirectiveOrRegister()
        } else if (character == '-' || character == '+') {
            val next = peekNextWord()
            if (next.matches(Regex("[0-9][bx0-9a-fA-F]+"))) {
                consumeNumber()
            } else if (next.startsWith("=")) {
                consumeAddSubAssignment()
            } else {
                consumePlusMinus()
            }
        } else if (character == '|' || character == '&' || character == '^') {
            val next = peekNextWord()
            if (next == "=") {
                consumeBinaryAssignment()
            } else {
                consumeBinaryOperator()
            }
        } else if (character == '<' || character == '>') {
            val next = peekNextWord()
            if (next.startsWith('<') || next.startsWith('>')) {
                consumeShift()
            } else {
                consumeComparison()
            }
        } else if (character == '=' || character == '!') {
            consumeComparison()
        } else if (character.isDigit()) {
            consumeNumber()
        } else if (character == '"') {
            consumeString()
        } else if (character == '*') {
            consumeMultiply()
        } else if (character == '(' || character ==')') {
            consumeParen()
        } else if (character == ';') {
            consumeSemi()
        } else if (character == '@') {
            consumeAt()
        } else if (character == '/') {
            consumeDivide()
        } else if (character == '{' || character == '}') {
            consumeBrace()
        } else {
            consumeErrorToken()
        }

    }

    return tokens


}


fun isNewline(character: Char): Boolean {
    return character == '\n' || character == '\r'

}


