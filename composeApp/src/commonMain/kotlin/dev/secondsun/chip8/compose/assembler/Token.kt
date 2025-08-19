package dev.secondsun.chip8.compose.assembler

sealed interface Token {
    val line: Int
    val column: Int


    data class StringToken(val value: String, override val line: Int, override val column: Int) : Token


    data class Identifier(val name: String, override val line: Int, override val column: Int) : Token
    data class Number(val value: Int, override val line: Int, override val column: Int) : Token
    data class Error(val message: String, override val line: Int, override val column: Int) : Token

    data class Register(val register: Registers, override val line: Int, override val column: Int) : Token

    data class Colon(override val line: Int, override val column: Int) : Token
    data class LBrace(override val line: Int, override val column: Int) : Token
    data class RBrace(override val line: Int, override val column: Int) : Token
    data class Return(override val line: Int, override val column: Int) : Token
    data class Call(override val line: Int, override val column: Int) : Token
    data class Key(override val line: Int, override val column: Int) : Token
    data class Hex(override val line: Int, override val column: Int) : Token
    data class BigHex(override val line: Int, override val column: Int) : Token
    data class Random(override val line: Int, override val column: Int) : Token
    data class Delay(override val line: Int, override val column: Int) : Token
    data class Clear(override val line: Int, override val column: Int) : Token
    data class BCD(override val line: Int, override val column: Int) : Token
    data class Save(override val line: Int, override val column: Int) : Token
    data class Load(override val line: Int, override val column: Int) : Token
    data class Buzzer(override val line: Int, override val column: Int) : Token
    data class If(override val line: Int, override val column: Int) : Token
    data class Then(override val line: Int, override val column: Int) : Token
    data class Begin(override val line: Int, override val column: Int) : Token
    data class Else(override val line: Int, override val column: Int) : Token
    data class End(override val line: Int, override val column: Int) : Token
    data class Jump(override val line: Int, override val column: Int) : Token
    data class Jump0(override val line: Int, override val column: Int) : Token
    data class Native(override val line: Int, override val column: Int) : Token
    data class Sprite(override val line: Int, override val column: Int) : Token
    data class Loop(override val line: Int, override val column: Int) : Token
    data class While(override val line: Int, override val column: Int) : Token
    data class Again(override val line: Int, override val column: Int) : Token
    data class ScrollDown(override val line: Int, override val column: Int) : Token
    data class ScrollRight(override val line: Int, override val column: Int) : Token
    data class ScrollLeft(override val line: Int, override val column: Int) : Token
    data class Lores(override val line: Int, override val column: Int) : Token
    data class Hires(override val line: Int, override val column: Int) : Token
    data class LoadFlags(override val line: Int, override val column: Int) : Token
    data class SaveFlags(override val line: Int, override val column: Int) : Token
    data class I(override val line: Int, override val column: Int) : Token
    data class Audio(override val line: Int, override val column: Int) : Token
    data class Pitch(override val line: Int, override val column: Int) : Token
    data class Plane(override val line: Int, override val column: Int) : Token
    data class ScrollUp(override val line: Int, override val column: Int) : Token
    data class Unpack(override val line: Int, override val column: Int) : Token
    data class Breakpoint(override val line: Int, override val column: Int) : Token
    data class Proto(override val line: Int, override val column: Int) : Token
    data class Alias(override val line: Int, override val column: Int) : Token
    data class Const(override val line: Int, override val column: Int) : Token
    data class Org(override val line: Int, override val column: Int) : Token
    data class Macro(override val line: Int, override val column: Int) : Token
    data class Calc(override val line: Int, override val column: Int) : Token
    data class Byte(override val line: Int, override val column: Int) : Token
    data class StringMode(override val line: Int, override val column: Int) : Token
    data class Assert(override val line: Int, override val column: Int) : Token
    data class Moniter(override val line: Int, override val column: Int) : Token
    data class Pointer(override val line: Int, override val column: Int) : Token
    data class Next(override val line: Int, override val column: Int) : Token
    data class Assignment(override val line: Int, override val column: Int) : Token
    data class SubtractionAssignment(override val line: Int, override val column: Int) : Token

    data class AdditionAssignment(override val line: Int, override val column: Int) : Token
    data class OrAssignment(override val line: Int, override val column: Int) : Token
    data class AndAssignment(override val line: Int, override val column: Int) : Token
    data class XorAssignment(override val line: Int, override val column: Int) : Token
    data class ShiftLeft(override val line: Int, override val column: Int) : Token
    data class ShiftRight(override val line: Int, override val column: Int) : Token
    data class GreaterThan(override val line: Int, override val column: Int) : Token
    data class LessThan(override val line: Int, override val column: Int) : Token
    data class GreaterThanOrEqual(override val line: Int, override val column: Int) : Token
    data class LessThanOrEqual(override val line: Int, override val column: Int) : Token
    data class Equal(override val line: Int, override val column: Int) : Token
    data class NotEqual(override val line: Int, override val column: Int) : Token
    data class Plus(override val line: Int, override val column: Int) : Token
    data class Minus(override val line: Int, override val column: Int) : Token

    companion object {
        fun makeDirective(identifier: String, line: Int, startColumn: Int): Token {
            when (identifier) {
                "key" -> return Key(line, startColumn)
                ":=" -> return Assignment(line, startColumn)
                "hex" -> return Hex(line, startColumn)
                "bighex" -> return BigHex(line, startColumn)
                "random" -> return Random(line, startColumn)
                "delay" -> return Delay(line, startColumn)
                ";" -> return Return(line, startColumn)
                "return" -> return Return(line, startColumn)
                "clear" -> return Clear(line, startColumn)
                "bcd" -> return BCD(line, startColumn)
                "save" -> return Save(line, startColumn)
                "load" -> return Load(line, startColumn)
                "buzzer" -> return Buzzer(line, startColumn)
                "if" -> return If(line, startColumn)
                "then" -> return Then(line, startColumn)
                "begin" -> return Begin(line, startColumn)
                "else" -> return Else(line, startColumn)
                "end" -> return End(line, startColumn)
                "jump" -> return Jump(line, startColumn)
                "jump0" -> return Jump0(line, startColumn)
                "native" -> return Native(line, startColumn)
                "sprite" -> return Sprite(line, startColumn)
                "loop" -> return Loop(line, startColumn)
                "while" -> return While(line, startColumn)
                "again" -> return Again(line, startColumn)
                "scroll-down" -> return ScrollDown(line, startColumn)
                "scroll-right" -> return ScrollRight(line, startColumn)
                "scroll-left" -> return ScrollLeft(line, startColumn)
                "lores" -> return Lores(line, startColumn)
                "hires" -> return Hires(line, startColumn)
                "loadflags" -> return LoadFlags(line, startColumn)
                "saveflags" -> return SaveFlags(line, startColumn)
                "i" -> return I(line, startColumn)
                "audio" -> return Audio(line, startColumn)
                "pitch" -> return Pitch(line, startColumn)
                "plane" -> return Plane(line, startColumn)
                "scroll-up" -> return ScrollUp(line, startColumn)
                ":next" -> return Next(line, startColumn)
                ":unpack" -> return Unpack(line, startColumn)
                ":breakpoint" -> return Breakpoint(line, startColumn)
                ":proto" -> return Proto(line, startColumn)
                ":alias" -> return Alias(line, startColumn)
                ":const" -> return Const(line, startColumn)
                ":org" -> return Org(line, startColumn)
                ":macro" -> return Macro(line, startColumn)
                ":calc" -> return Calc(line, startColumn)
                ":byte" -> return Byte(line, startColumn)
                ":call" -> return Call(line, startColumn)
                ":stringmode" -> return StringMode(line, startColumn)
                ":assert" -> return Assert(line, startColumn)
                ":monitor" -> return Moniter(line, startColumn)
                ":pointer" -> return Pointer(line, startColumn)
                else -> throw IllegalArgumentException("Unknown directive: $identifier")
            }
        }

        fun makeRegister(identifier: String, line: Int, startColumn: Int): Token {
            return Register(Registers.valueOf(identifier), line, startColumn)
        }
    }
}

