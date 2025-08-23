package dev.secondsun.chip8.compose.assembler

class StringMode(val name: String) {

    val bodies = mutableMapOf<Char, List<Token>>()
    val vals = mutableMapOf<Char, Int>()
    var calls = 0

    fun addAlphabet(alphabet: String, body: List<Token>) {
        for (c in alphabet) {
            if (bodies.containsKey(c)) {
                throw IllegalStateException("String mode '${name}' is already defined for the character '${c}'")
            }
            bodies[c] = body
            vals[c] = alphabet.indexOf(c)
        }
    }

    fun evaluate(str: String): List<Token> {
        val toReturn = mutableListOf<Token>()
        str.forEachIndexed { index, char ->
            if (!bodies.containsKey(char)) {
                throw IllegalStateException("String mode '${name}' is not defined for the character '${char}'")
            }
            val body = bodies[char]!!
            val bindings = buildMap {
                put("CALLS", calls++)
                put("CHAR", char.code)
                put("INDEX", index)
                put("VALUE", vals[char]!!)
            }

            for (x in 0..<body.size) {


                when (val chunk = body[x]) {
                    is Token.Identifier -> {
                        if (bindings.containsKey(chunk.name)) {
                            toReturn.add(Token.Number(bindings[chunk.name]!!, 0, 0))
                        } else {
                            toReturn.add(chunk)
                        }
                    }

                    else -> {
                        toReturn.add(chunk)
                    }
                }


            }

        }
        return toReturn
    }

}