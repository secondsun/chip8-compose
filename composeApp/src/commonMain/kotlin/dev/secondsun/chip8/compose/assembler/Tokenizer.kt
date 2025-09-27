package dev.secondsun.chip8.compose.assembler

import java.lang.Character.isWhitespace
import java.lang.Integer.parseInt
import kotlin.math.max


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

    fun getCharacter() :Char{
        return if (canContinue()) {
            program[index]
        } else {
            Char.MIN_VALUE
        }
    }

    /**
     * Advance the tokenizer state to the next line. This should be called when a newline is consumed.
     * It advances the line, index, and column accordingly but does NOT handle whitespace or do any processing.
     */
    fun nextLine() {

        val newLine = getCharacter()

        index++

        //Handle windows \r\n
        if (canContinue() && ((getCharacter() == '\r' && newLine == '\n') || (getCharacter() == '\n' && newLine == '\r'))) {
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
        var character = getCharacter()
        while (character.isWhitespace()) {
            if (isNewline(character)) {
                nextLine()
            } else {
                nextCharacter()
            }
            if (index >= program.length) {
                break
            }
            character = getCharacter()
        }

    }

    fun consumeComment() {
        nextCharacter()//Consume #
        var character = getCharacter()
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
            character = getCharacter()
        }

    }

    fun consumeMinusKey() {
        val startColumn = column
        val buffer = StringBuffer()

        while(canContinue() && !getCharacter().isWhitespace()) {
            val character = getCharacter()
            nextCharacter()
            buffer.append(character)

        }

        val string = buffer.toString()
        if (string == "-key") {
            tokens.add(Token.MinusKey(line, startColumn))
        } else {
            tokens.add(Token.Error("Unexpected token $string at $line, $startColumn", line, startColumn, string.length))
        }

    }


    fun consumePlusMinus() {
        val startColumn = column
        val character = getCharacter()
        nextCharacter()

        if (character == '+') {
            tokens.add(Token.Plus(line, startColumn))
        } else if (character == '-') {
            tokens.add(Token.Minus(line, startColumn))
        } else {
            tokens.add(Token.Error("Unexpected token $character at $line, $startColumn", line, startColumn, 1))
        }

    }

    fun consumeString() {
        val startColumn = column
        val startLine = line
        val startIndex = index
        val stringBuilder = StringBuilder()

        nextCharacter()

        if (!canContinue()) {
            tokens.add(Token.Error("Unterminated string literal at $startLine, $startColumn", line, startColumn, max(1,column-startColumn)))
            return
        }

        while (canContinue()) {

            val c = getCharacter()
            when (c) {
                '"' -> {nextCharacter();break}
                '\\' -> {
                    nextCharacter()
                    if (!canContinue()) {
                        tokens.add(Token.Error("Unterminated string literal at $startLine, $startColumn", line, startColumn,max(1,column-startColumn)))
                        return
                    }
                    val esc = getCharacter()
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
                                    "Invalid escape character '$esc' in string literal at $startLine, $startColumn",line, column,1
                                )
                            )
                        }
                    }
                }
                else -> stringBuilder.append(c)
            }

            nextCharacter()
            if (!canContinue()) {
                tokens.add(Token.Error("Unterminated string literal at $startLine, $startColumn", line, startColumn,max(1,column-startColumn)))
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
        var character = getCharacter()

        if (character == '-') {
            sign = -1
            nextCharacter()
            if (canContinue()) {
                character = getCharacter()
            }
        } else if (character == '+') {
            sign = 1
            nextCharacter()
            if (canContinue()) {
                character = getCharacter()
            }
        }

        while (canContinue() && !character.isWhitespace()) {

            numberBuilder.append(character)
            nextCharacter()
            if (canContinue()) {
                character = getCharacter()
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
                        tokens.add(Token.Number(sign * parseInt(numberString, 10), line, startColumn, numberString.length ))
                    } else {
                        when (secondDigit) {
                            'x' -> tokens.add(
                                Token.Number(
                                    sign * parseInt(numberString.substring(2), 16),
                                    line,
                                    startColumn
                                    , numberString.length
                                )
                            )

                            'b' -> tokens.add(
                                Token.Number(
                                    sign * parseInt(numberString.substring(2), 2),
                                    line,
                                    startColumn, numberString.length
                                )
                            )

                            else -> throw IllegalStateException("Unexpected number format $numberString at $startLine:$startColumn")

                        }
                    }
                } else {//Number is exactly zero
                    tokens.add(Token.Number(0, line, startColumn,numberString.length))
                }
            } else {
                tokens.add(Token.Number(sign * parseInt(numberString), line, startColumn, numberString.length))
            }
        } catch (e: NumberFormatException) {
e.printStackTrace()
            throw IllegalStateException("Unexpected number format $numberString at $startLine:$startColumn")
        }

    }

    fun consumeIdentifierTokens(): String  {
        val identifierBuilder = StringBuilder()
        var character = getCharacter()
        while (!character.isWhitespace()) {
            identifierBuilder.append(character)
            nextCharacter()
            if (index >= program.length) {
                break
            }
            character = getCharacter()
        }

        return  identifierBuilder.toString()

    }


    fun consumeIdentifierOrDirective() {
        val startColumn = column
        val identifier = consumeIdentifierTokens();

        if (DIRECTIVES.contains(identifier)) {
            tokens.add(Token.makeDirective(identifier, line, startColumn))
        } else if (identifier == "exit") {
            tokens.add(Token.Exit( line, startColumn))
        }
        else {
            tokens.add(Token.Identifier(identifier, line, startColumn))
        }
    }


    fun consumeErrorToken() {
        val startColumn = column
        val unidentifierTokenBuilder = StringBuilder()
        var character = getCharacter()
        while (!character.isWhitespace()) {
            unidentifierTokenBuilder.append(character)
            nextCharacter()
            if (index >= program.length) {
                break
            }
            character = getCharacter()
        }
        tokens.add(
            Token.Error(
                "Unidentified token ${unidentifierTokenBuilder.toString()} at $line, $startColumn",
                line,
                startColumn,
                unidentifierTokenBuilder.length
            )
        )
    }

    fun consumeBinaryAssignment() {
        val startColumn = column
        val character = getCharacter()
        nextCharacter()
        val nextCharacter = getCharacter()

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
                    startColumn,
                    2
                )
            )
        }
        nextCharacter()
    }

    fun consumeAddSubAssignment() {
        val startColumn = column
        val character = getCharacter()
        nextCharacter()
        val nextCharacter = getCharacter()

        if (character == '-' && nextCharacter == '=') {
            tokens.add(Token.SubtractionAssignment(line, startColumn))
        } else if (character == '+' && nextCharacter == '=') {
            tokens.add(Token.AdditionAssignment(line, startColumn))
        } else {
            tokens.add(
                Token.Error(
                    "Unexpected token $character$nextCharacter at $line, $startColumn",
                    line,
                    startColumn,2
                )
            )
        }
        nextCharacter()
    }


    fun consumeBinaryOperator() {
        val startColumn = column
        val character = getCharacter()
        when (character) {
            '|' -> tokens.add(Token.BinaryOr(line, startColumn))
            '&' -> tokens.add(Token.BinaryAnd(line, startColumn))
            '^' -> tokens.add(Token.Caret(line, startColumn))
        }
        nextCharacter()
    }


    fun consumeBrace() {
        val startColumn = column
        var character = getCharacter()
        when (character) {
            '{' -> tokens.add(Token.LBrace(line, startColumn))
            '}' -> tokens.add(Token.RBrace(line, startColumn))

        }
        nextCharacter()

    }
    fun consumeParen() {
        val startColumn = column
        var character = getCharacter()
        when (character) {
            '(' -> tokens.add(Token.LParen(line, startColumn))
            ')' -> tokens.add(Token.RParen(line, startColumn))

        }
        nextCharacter()

    }

    fun consumeTilde() {
        val startColumn = column
        tokens.add(Token.Tilde(line, startColumn))
        nextCharacter()

    }



    fun consumeExclaimation() {
        val startColumn = column
        tokens.add(Token.Exclaimation(line, startColumn))
        nextCharacter()

    }

    fun consumeCaret() {
        val startColumn = column
        tokens.add(Token.Caret(line, startColumn))
        nextCharacter()
    }

    fun consumePercent() {
        val startColumn = column
        tokens.add(Token.Percent(line, startColumn))
        nextCharacter()

    }

    fun consumeAt() {
        val startColumn = column
        var character = getCharacter()
        tokens.add(Token.At(line, startColumn))
        nextCharacter()

    }

    fun consumeSemi() {
        val startColumn = column
        var character = getCharacter()
        tokens.add(Token.Semicolon(line, startColumn))

        nextCharacter()

    }


    fun consumeDivide() {
        val startColumn = column
        var character = getCharacter()
        when (character) {
            '/' -> tokens.add(Token.Divide(line, startColumn))

        }
        nextCharacter()

    }

    fun consumeMultiply() {
        val startColumn = column
        var character = getCharacter()
        when (character) {
            '*' -> tokens.add(Token.Multiply(line, startColumn))
        }
        nextCharacter()

    }


    fun consumeComparison() {
        val startColumn = column
        val character = getCharacter()
        nextCharacter()
        if (canContinue()) {
        val nextCharacter = getCharacter()

        when (character) {
            '=' -> if (nextCharacter == '=') {
                tokens.add(Token.Equal(line, startColumn))
                nextCharacter()
            } else if (nextCharacter == '-') {
                val startColumn = column
                tokens.add(Token.ReverseSubtractionAssignment(line, startColumn))
                nextCharacter();
            } else {
                consumeErrorToken()
            }

            '!' -> if (nextCharacter == '=') {
                tokens.add(Token.NotEqual(line, startColumn))
                nextCharacter()
            } else {
                tokens.add(Token.Exclaimation(line, startColumn))

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
                    startColumn,1
                )
            )
        }


    }

    fun consumeShift() {
        val startColumn = column
        val character = getCharacter()
        nextCharacter()
        val nextCharacter = getCharacter()

        if (character == '>' && nextCharacter == '>') {
            nextCharacter()
            if (getCharacter() == '=') {
                nextCharacter()
                tokens.add(Token.ShiftRightAssign(line, startColumn))
            } else {
                tokens.add(Token.ShiftRight(line, startColumn))
            }
        } else if (character == '<' && nextCharacter == '<') {
            nextCharacter()
            if (getCharacter() == '=') {
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
                    startColumn,2
                )
            )
        }


    }


    fun peekNextCharacter(): Char {
        return if ((index+1) < program.length) {
            program[index+1]
        } else {
            Char.MIN_VALUE
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

            var character = getCharacter() // return label
                val identifierBuilder = StringBuilder()
                while (!character.isWhitespace()) {
                    identifierBuilder.append(character)
                    nextCharacter()
                    if (index >= program.length) {
                        break
                    }
                    character = getCharacter()
                }
                toReturn = identifierBuilder.toString()

        }

        index = backupIndex
        column = backupColumn
        line = backupLine

        return toReturn

    }

    while (canContinue()) {
        val character = getCharacter()
        if (character == ':') { //Start directive
            val nextToken = peekNextWord()
            if (DIRECTIVES.contains(":$nextToken")) {
                consumeIdentifierOrDirective()
            } else {
                colon()
            }
        } else if (character == '#') { //Start directive
            consumeComment()
        } else if (character.isWhitespace()) { //consume whitespace
            consumeWhitespace()
        } else if (character.isLetter()) { // consume identifier
            consumeIdentifierOrDirective()
        } else if (character == '-' || character == '+') {
            val nextCharacter = peekNextCharacter()
            val next = peekNextWord()
            if (isWhitespace(nextCharacter)) {
                consumePlusMinus()
            } else if (next == "key") {
                consumeMinusKey()
            } else if (next.matches(Regex("[0-9][bx0-9a-fA-F]+"))) {
                consumeNumber()
            } else if (next.matches(Regex("[0-9]+"))) {
                consumeNumber()
            } else if (next.startsWith("=")) {
                consumeAddSubAssignment()
            } else {
                //plus and minus are allowed to be identifiers
                val startColumn = column;
                val identifier = consumeIdentifierTokens()
                tokens.add(Token.Identifier(identifier, line, startColumn));

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
        } else if (character == '~') {
            consumeTilde()
        } else if (character == '!') {
            consumeExclaimation()
        } else if (character == '/') {
            consumeDivide()
        } else if (character == '%') {
            consumePercent()
        } else if (character == '{' || character == '}') {
            consumeBrace()
        } else {
            val startColumn = column;
            val identifier = consumeIdentifierTokens()
            tokens.add(Token.Identifier(identifier, line, startColumn));
        }

    }

    return tokens


}



fun isNewline(character: Char): Boolean {
    return character == '\n' || character == '\r'

}


