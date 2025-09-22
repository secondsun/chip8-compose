package dev.secondsun.chip8.compose.assembler

sealed interface Token {
    val line: Int
    val column: Int
    val length: Int
    val type: TokenType

    data class StringToken(val value: String, override val line: Int, override val column: Int) : Token {
        override val type: TokenType
            get() = TokenType.StringToken

        override val length: Int
            get() = value.length + 2

        override fun toString(): String {
            return "String \"$value\" ${line}:${column}"
        }
    }

    data class Identifier(val name: String, override val line: Int, override val column: Int) : Token {
        override val type: TokenType
            get() = TokenType.Identifier


        override val length: Int
            get() = name.length

        override fun toString(): String {
            return "Identifier \"$name\" ${line}:${column}"
        }
    }

    data class Number(val value: Int, override val line: Int, override val column: Int, override val length: Int) :
        Token {
        override val type: TokenType
            get() = TokenType.Number


        override fun toString(): String {
            return "Number \"$value\" ${line}:${column}"
        }
    }

    data class Error(val message: String, override val line: Int, override val column: Int, override val length: Int) :
        Token {
        override val type: TokenType
            get() = TokenType.Error


        override fun toString(): String {
            return "Error \"$message\" ${line}:${column}"
        }
    }
//
//    data class Register(val register: Registers, override val line: Int, override val column: Int) : Token {
//        override val type: TokenType
//            get() = TokenType.Register
//
//        override val length: Int
//            get() = if (register == Registers.i) 1 else 2
//    }


    data class Multiply(override val line: Int, override val column: Int) : Token {
        override val type: TokenType
            get() = TokenType.Multiply

        override val length: Int
            get() = 1

    }

    data class BinaryOr(override val line: Int, override val column: Int) : Token {
        override val type: TokenType
            get() = TokenType.BinaryOr

        override val length: Int
            get() = 1
    }

    data class BinaryAnd(override val line: Int, override val column: Int) : Token {
        override val type: TokenType
            get() = TokenType.BinaryAnd

        override val length: Int
            get() = 1
    }

    data class Exit(override val line: Int, override val column: Int) : Token {
        override val type: TokenType
            get() = TokenType.Exit

        override val length: Int
            get() = 4
    }

    data class LParen(override val line: Int, override val column: Int) : Token {
        override val type: TokenType
            get() = TokenType.LParen

        override val length: Int
            get() = 1
    }

    data class RParen(override val line: Int, override val column: Int) : Token {
        override val type: TokenType
            get() = TokenType.RParen

        override val length: Int
            get() = 1
    }

    data class At(override val line: Int, override val column: Int) : Token {
        override val type: TokenType
            get() = TokenType.At

        override val length: Int
            get() = 1
    }

    data class Tilde(override val line: Int, override val column: Int) : Token {
        override val type: TokenType
            get() = TokenType.Tilde

        override val length: Int
            get() = 1
    }

    data class Exclaimation(override val line: Int, override val column: Int) : Token {
        override val type: TokenType
            get() = TokenType.Exclaimation

        override val length: Int
            get() = 1
    }

    data class Caret(override val line: Int, override val column: Int) : Token {
        override val type: TokenType
            get() = TokenType.Caret

        override val length: Int
            get() = 1
    }

    data class Percent(override val line: Int, override val column: Int) : Token {
        override val type: TokenType
            get() = TokenType.Percent

        override val length: Int
            get() = 1
    }

    data class Semicolon(override val line: Int, override val column: Int) : Token {
        override val type: TokenType
            get() = TokenType.Semicolon

        override val length: Int
            get() = 1
    }


    data class Divide(override val line: Int, override val column: Int) : Token {
        override val type: TokenType
            get() = TokenType.Divide

        override val length: Int
            get() = 1
    }

    data class Colon(override val line: Int, override val column: Int) : Token {
        override val type: TokenType
            get() = TokenType.Colon

        override val length: Int
            get() = 1
    }

    data class LBrace(override val line: Int, override val column: Int) : Token {
        override val type: TokenType
            get() = TokenType.LBrace

        override val length: Int
            get() = 1
    }

    data class RBrace(override val line: Int, override val column: Int) : Token {
        override val type: TokenType
            get() = TokenType.RBrace

        override val length: Int
            get() = 1
    }

    data class Return(override val line: Int, override val column: Int) : Token {
        override val type: TokenType
            get() = TokenType.Return

        override val length: Int
            get() = 6
    }

    data class Call(override val line: Int, override val column: Int) : Token {
        override val type: TokenType
            get() = TokenType.Call
        override val length: Int
            get() = 4
    }

    data class Key(override val line: Int, override val column: Int) : Token {
        override val type: TokenType
            get() = TokenType.Key
        override val length: Int
            get() = 3
    }

    data class MinusKey(override val line: Int, override val column: Int) : Token {
        override val type: TokenType
            get() = TokenType.MinusKey
        override val length: Int
            get() = 4
    }

    data class Hex(override val line: Int, override val column: Int) : Token {
        override val type: TokenType
            get() = TokenType.Hex
        override val length: Int
            get() = 3
    }

    data class BigHex(override val line: Int, override val column: Int) : Token {
        override val type: TokenType
            get() = TokenType.BigHex
        override val length: Int
            get() = 6
    }

    data class Random(override val line: Int, override val column: Int) : Token {
        override val type: TokenType
            get() = TokenType.Random
        override val length: Int
            get() = 6
    }

    data class Delay(override val line: Int, override val column: Int) : Token {
        override val type: TokenType
            get() = TokenType.Delay
        override val length: Int
            get() = 5
    }

    data class Clear(override val line: Int, override val column: Int) : Token {
        override val type: TokenType
            get() = TokenType.Clear
        override val length: Int
            get() = 5
    }

    data class BCD(override val line: Int, override val column: Int) : Token {
        override val type: TokenType
            get() = TokenType.BCD
        override val length: Int
            get() = 3
    }

    data class Save(override val line: Int, override val column: Int) : Token {
        override val type: TokenType
            get() = TokenType.Save

        override val length: Int
            get() = 4
    }

    data class Load(override val line: Int, override val column: Int) : Token {
        override val type: TokenType
            get() = TokenType.Load

        override val length: Int
            get() = 4
    }

    data class Buzzer(override val line: Int, override val column: Int) : Token {
        override val type: TokenType
            get() = TokenType.Buzzer

        override val length: Int
            get() = 6
    }

    data class If(override val line: Int, override val column: Int) : Token {
        override val type: TokenType
            get() = TokenType.If

        override val length: Int
            get() = 2
    }

    data class Then(override val line: Int, override val column: Int) : Token {
        override val type: TokenType
            get() = TokenType.Then

        override val length: Int
            get() = 4
    }

    data class Begin(override val line: Int, override val column: Int) : Token {
        override val type: TokenType
            get() = TokenType.Begin

        override val length: Int
            get() = 5
    }

    data class Else(override val line: Int, override val column: Int) : Token {
        override val type: TokenType
            get() = TokenType.Else

        override val length: Int
            get() = 4
    }

    data class End(override val line: Int, override val column: Int) : Token {
        override val type: TokenType
            get() = TokenType.End
        override val length: Int
            get() = 3
    }

    data class Jump(override val line: Int, override val column: Int) : Token {
        override val type: TokenType
            get() = TokenType.Jump
        override val length: Int
            get() = 4
    }

    data class Jump0(override val line: Int, override val column: Int) : Token {
        override val type: TokenType
            get() = TokenType.Jump0
        override val length: Int
            get() = 5
    }

    data class Native(override val line: Int, override val column: Int) : Token {
        override val type: TokenType
            get() = TokenType.Native
        override val length: Int
            get() = 6
    }

    data class Sprite(override val line: Int, override val column: Int) : Token {
        override val type: TokenType
            get() = TokenType.Sprite
        override val length: Int
            get() = 5
    }

    data class Loop(override val line: Int, override val column: Int) : Token {
        override val type: TokenType
            get() = TokenType.Loop
        override val length: Int
            get() = 4
    }

    data class While(override val line: Int, override val column: Int) : Token {
        override val type: TokenType
            get() = TokenType.While

        override val length: Int
            get() = 5
    }

    data class Again(override val line: Int, override val column: Int) : Token {
        override val type: TokenType
            get() = TokenType.Again
        override val length: Int
            get() = 5
    }

    data class ScrollDown(override val line: Int, override val column: Int) : Token {
        override val type: TokenType
            get() = TokenType.ScrollDown
        override val length: Int
            get() = 11
    }

    data class ScrollRight(override val line: Int, override val column: Int) : Token {
        override val type: TokenType
            get() = TokenType.ScrollRight
        override val length: Int
            get() = 12
    }

    data class ScrollLeft(override val line: Int, override val column: Int) : Token {
        override val type: TokenType
            get() = TokenType.ScrollLeft
        override val length: Int
            get() = 11
    }

    data class Lores(override val line: Int, override val column: Int) : Token {
        override val type: TokenType
            get() = TokenType.Lores

        override val length: Int
            get() = 5
    }

    data class Hires(override val line: Int, override val column: Int) : Token {
        override val type: TokenType
            get() = TokenType.Hires

        override val length: Int
            get() = 5
    }

    data class LoadFlags(override val line: Int, override val column: Int) : Token {
        override val type: TokenType
            get() = TokenType.LoadFlags

        override val length: Int
            get() = 9
    }

    data class SaveFlags(override val line: Int, override val column: Int) : Token {
        override val type: TokenType
            get() = TokenType.SaveFlags
        override val length: Int
            get() = 9
    }

//    data class I(override val line: Int, override val column: Int) : Token {
//        override val type: TokenType
//            get() = TokenType.I
//
//        override val length: Int
//            get() = 1
//    }

    data class Audio(override val line: Int, override val column: Int) : Token {
        override val type: TokenType
            get() = TokenType.Audio
        override val length: Int
            get() = 5
    }

    data class Pitch(override val line: Int, override val column: Int) : Token {
        override val type: TokenType
            get() = TokenType.Pitch
        override val length: Int
            get() = 5
    }

    data class Plane(override val line: Int, override val column: Int) : Token {
        override val type: TokenType
            get() = TokenType.Plane

        override val length: Int
            get() = 5
    }

    data class ScrollUp(override val line: Int, override val column: Int) : Token {
        override val type: TokenType
            get() = TokenType.ScrollUp


        override val length: Int
            get() = 9
    }

    data class Unpack(override val line: Int, override val column: Int) : Token {
        override val type: TokenType
            get() = TokenType.Unpack


        override val length: Int
            get() = 6
    }

    data class Breakpoint(override val line: Int, override val column: Int) : Token {
        override val type: TokenType
            get() = TokenType.Breakpoint
        override val length: Int
            get() = 10
    }

    data class Proto(override val line: Int, override val column: Int) : Token {
        override val type: TokenType
            get() = TokenType.Proto


        override val length: Int
            get() = 5
    }

    data class Alias(override val line: Int, override val column: Int) : Token {
        override val type: TokenType
            get() = TokenType.Alias


        override val length: Int
            get() = 5
    }

    data class Const(override val line: Int, override val column: Int) : Token {
        override val type: TokenType
            get() = TokenType.Const


        override val length: Int
            get() = 5
    }

    data class Org(override val line: Int, override val column: Int) : Token {
        override val type: TokenType
            get() = TokenType.Org
        override val length: Int
            get() = 3
    }

    data class Macro(override val line: Int, override val column: Int) : Token {
        override val type: TokenType
            get() = TokenType.Macro

        override val length: Int
            get() = 5
    }

    data class Calc(override val line: Int, override val column: Int) : Token {
        override val type: TokenType
            get() = TokenType.Calc
        override val length: Int
            get() = 4
    }

    data class Byte(override val line: Int, override val column: Int) : Token {
        override val type: TokenType
            get() = TokenType.Byte
        override val length: Int
            get() = 4
    }

    data class StringMode(override val line: Int, override val column: Int) : Token {
        override val type: TokenType
            get() = TokenType.StringMode
        override val length: Int
            get() = 11
    }

    data class Assert(override val line: Int, override val column: Int) : Token {
        override val type: TokenType
            get() = TokenType.Assert
        override val length: Int
            get() = 6
    }

    data class Moniter(override val line: Int, override val column: Int) : Token {
        override val type: TokenType
            get() = TokenType.Moniter

        override val length: Int
            get() = 7
    }

    data class Pointer(override val line: Int, override val column: Int) : Token {
        override val type: TokenType
            get() = TokenType.Pointer


        override val length: Int
            get() = 7
    }

    data class Next(override val line: Int, override val column: Int) : Token {
        override val type: TokenType
            get() = TokenType.Next


        override val length: Int
            get() = 4
    }

    data class Assignment(override val line: Int, override val column: Int) : Token {
        override val type: TokenType
            get() = TokenType.Assignment

        override val length: Int
            get() = 1
    }

    data class SubtractionAssignment(override val line: Int, override val column: Int) : Token {
        override val type: TokenType
            get() = TokenType.SubtractionAssignment


        override val length: Int
            get() = 2
    }

    data class ReverseSubtractionAssignment(override val line: Int, override val column: Int) : Token {
        override val type: TokenType
            get() = TokenType.ReverseSubtractionAssignment

        override val length: Int
            get() = 2
    }


    data class AdditionAssignment(override val line: Int, override val column: Int) : Token {
        override val type: TokenType
            get() = TokenType.AdditionAssignment

        override val length: Int
            get() = 2
    }

    data class OrAssignment(override val line: Int, override val column: Int) : Token {
        override val type: TokenType
            get() = TokenType.OrAssignment

        override val length: Int
            get() = 2
    }

    data class AndAssignment(override val line: Int, override val column: Int) : Token {
        override val type: TokenType
            get() = TokenType.AndAssignment

        override val length: Int
            get() = 2
    }

    data class XorAssignment(override val line: Int, override val column: Int) : Token {
        override val type: TokenType
            get() = TokenType.XorAssignment

        override val length: Int
            get() = 2
    }

    data class ShiftLeft(override val line: Int, override val column: Int) : Token {
        override val type: TokenType
            get() = TokenType.ShiftLeft

        override val length: Int
            get() = 2
    }

    data class ShiftRight(override val line: Int, override val column: Int) : Token {
        override val type: TokenType
            get() = TokenType.ShiftRight

        override val length: Int
            get() = 2
    }

    data class ShiftLeftAssign(override val line: Int, override val column: Int) : Token {
        override val type: TokenType
            get() = TokenType.ShiftLeftAssign

        override val length: Int
            get() = 3
    }

    data class ShiftRightAssign(override val line: Int, override val column: Int) : Token {
        override val type: TokenType
            get() = TokenType.ShiftRightAssign
        override val length: Int
            get() = 3
    }

    data class GreaterThan(override val line: Int, override val column: Int) : Token {
        override val type: TokenType
            get() = TokenType.GreaterThan
        override val length: Int
            get() = 1
    }

    data class LessThan(override val line: Int, override val column: Int) : Token {
        override val type: TokenType
            get() = TokenType.LessThan
        override val length: Int
            get() = 1
    }

    data class GreaterThanOrEqual(override val line: Int, override val column: Int) : Token {
        override val type: TokenType
            get() = TokenType.GreaterThanOrEqual
        override val length: Int
            get() = 2
    }

    data class LessThanOrEqual(override val line: Int, override val column: Int) : Token {
        override val type: TokenType
            get() = TokenType.LessThanOrEqual
        override val length: Int
            get() = 2
    }

    data class Equal(override val line: Int, override val column: Int) : Token {
        override val type: TokenType
            get() = TokenType.Equal
        override val length: Int
            get() = 2
    }

    data class NotEqual(override val line: Int, override val column: Int) : Token {
        override val type: TokenType
            get() = TokenType.NotEqual
        override val length: Int
            get() = 2
    }

    data class Plus(override val line: Int, override val column: Int) : Token {
        override val type: TokenType
            get() = TokenType.Plus
        override val length: Int
            get() = 1
    }

    data class Minus(override val line: Int, override val column: Int) : Token {
        override val type: TokenType
            get() = TokenType.Minus
        override val length: Int
            get() = 1
    }

    data class ForwardIdentifier(val name: String, override val line: Int, override val column: Int) : Token {
        override val type: TokenType
            get() = TokenType.ForwardIdentifier
        override val length: Int
            get() = name.length

        override fun toString(): String {
            return "ForwardIdentifier \"$name\" ${line}:${column}"
        }
    }


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
//                "i" -> return I(line, startColumn)
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

//        fun makeRegister(identifier: String, line: Int, startColumn: Int): Token {
//            return Register(Registers.valueOf(identifier.lowercase()), line, startColumn)
//        }
    }
}

enum class TokenType {
    StringToken,


    Identifier,
    Number,
    Error,

    //Register,
    Multiply,
    BinaryOr,
    BinaryAnd,
    LParen,
    RParen,
    At,
    Semicolon,

    Divide,
    Colon,
    LBrace,
    RBrace,
    Return,
    Call,
    Key,
    Hex,
    BigHex,
    Random,
    Delay,
    Clear,
    BCD,
    Save,
    Load,
    Buzzer,
    If,
    Then,
    Begin,
    Else,
    End,
    Exit,
    Jump,
    Jump0,
    Native,
    Sprite,
    Loop,
    While,
    Again,
    ScrollDown,
    ScrollRight,
    ScrollLeft,
    Lores,
    Hires,
    LoadFlags,
    SaveFlags,
    //I,
    Audio,
    Pitch,
    Plane,
    ScrollUp,
    Unpack,
    Breakpoint,
    Proto,
    Alias,
    Const,
    Org,
    Macro,
    Calc,
    Byte,
    MinusKey,
    StringMode,
    Assert,
    Moniter,
    Pointer,
    Next,
    Assignment,
    SubtractionAssignment,

    AdditionAssignment,
    OrAssignment,
    AndAssignment,
    XorAssignment,
    ShiftLeft,
    ShiftRight,
    ShiftLeftAssign,
    ShiftRightAssign,
    GreaterThan,
    LessThan,
    GreaterThanOrEqual,
    LessThanOrEqual,
    Equal,
    NotEqual,
    Plus,
    Minus,
    Tilde,
    Exclaimation,
    Caret,
    Percent,
    ForwardIdentifier,
    ReverseSubtractionAssignment

}