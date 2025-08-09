package dev.secondsun.chip8.compose.assembler


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
     * Advance the tokenizer state to the next line. This should be called when a newline is consumed.
     * It advances the line, index, and column accordingly but does NOT handle whitespace or do any processing.
     */
    fun nextLine() {
        index++
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
            if (character == '\n') {
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

    fun consumeIdentier() {
        val identifierBuilder = StringBuilder()
        val startColumn = column
        var character = program[index]
        while (!character.isWhitespace()) {
            identifierBuilder.append(character)
            nextCharacter()
            if (index >= program.length) {
                break
            }
            character = program[index]
        }
        tokens.add(Token.Identifier(identifierBuilder.toString(), line, startColumn))
    }

    while (index < program.length) {
        val character = program[index]
        if (character == ':') { //Start directive
            colon()
        } else if (character.isWhitespace()) { //consume whitespace
            consumeWhitespace()
        } else { // consume identifier
            consumeIdentier()
        }

    }

    return tokens


}


