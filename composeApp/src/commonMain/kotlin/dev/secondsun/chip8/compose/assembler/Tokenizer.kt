package dev.secondsun.chip8.compose.assembler

import java.lang.Integer.parseInt


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
                            'x' -> tokens.add(Token.Number(sign * parseInt(numberString.substring(2), 16), line, startColumn))
                            'b' -> tokens.add(Token.Number(sign * parseInt(numberString.substring(2), 2), line, startColumn))
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
    fun consumeIdentifierOrDirective() {
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

        if (DIRECTIVES.contains(identifier)) {
            tokens.add(Token.makeDirective(identifier, line, startColumn))
        } else {
            tokens.add(Token.Identifier(identifier, line, startColumn))
        }
    }

    fun consumerErrorToken() {
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
        tokens.add(Token.Error("Unidentified token ${unidentifierTokenBuilder.toString()} at $line, $startColumn", line, startColumn))
    }

    fun peekNextLabel(): String {
        val backupIndex = index
        val backupLine = line
        val backupColumn = column

        var toReturn = ""

        nextCharacter()
        var character = program[index]
        if (character.isLetter() || (character == '=')) { // return label
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
            val nextToken = peekNextLabel()
            if (DIRECTIVES.contains(":$nextToken")) {
                consumeIdentifierOrDirective()
            } else {
                colon()
            }
        } else if (character == '#') { //Start directive
            consumeComment()
        } else if (character.isWhitespace()) { //consume whitespace
            consumeWhitespace()
        } else if (character.isLetter()){ // consume identifier
            consumeIdentifierOrDirective()
        } else if (character.isDigit() || character == '-' || character == '+') {
            consumeNumber()
        } else {
            consumerErrorToken()
        }

    }

    return tokens


}




fun isNewline(character: Char): Boolean {
    return character == '\n' || character == '\r'

}


