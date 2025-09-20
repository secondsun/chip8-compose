package dev.secondsun.chip8.compose.assembler

sealed interface Token {
    val line: Int
    val column: Int
    val type: TokenType

    data class StringToken(val value: String, override val line: Int, override val column: Int) : Token {
        override val type: TokenType
            get() = TokenType.StringToken

        override fun toString(): String {
            return "String \"$value\" ${line}:${column}"
        }
    }

    data class Identifier(val name: String, override val line: Int, override val column: Int) : Token {
        override val type: TokenType
            get() = TokenType.Identifier

        override fun toString(): String {
            return "Identifier \"$name\" ${line}:${column}"
        }
    }

    data class Number(val value: Int, override val line: Int, override val column: Int) : Token {
        override val type: TokenType
            get() = TokenType.Number


        override fun toString(): String {
            return "Number \"$value\" ${line}:${column}"
        }
    }

    data class Error(val message: String, override val line: Int, override val column: Int) : Token {
        override val type: TokenType
            get() = TokenType.Error


        override fun toString(): String {
            return "Error \"$message\" ${line}:${column}"
        }
    }

    data class Register(val register: Registers, override val line: Int, override val column: Int) : Token {
        override val type: TokenType
            get() = TokenType.Register
    }


    data class Multiply(override val line: Int, override val column: Int) : Token {
        override val type: TokenType
            get() = TokenType.Multiply
    }

    data class BinaryOr(override val line: Int, override val column: Int) : Token {
        override val type: TokenType
            get() = TokenType.BinaryOr
    }

    data class BinaryAnd(override val line: Int, override val column: Int) : Token {
        override val type: TokenType
            get() = TokenType.BinaryAnd
    }

    data class Exit(override val line: Int, override val column: Int) : Token {
        override val type: TokenType
            get() = TokenType.Exit
    }

    data class LParen(override val line: Int, override val column: Int) : Token {
        override val type: TokenType
            get() = TokenType.LParen
    }

    data class RParen(override val line: Int, override val column: Int) : Token {
        override val type: TokenType
            get() = TokenType.RParen
    }

    data class At(override val line: Int, override val column: Int) : Token {
        override val type: TokenType
            get() = TokenType.At
    }

    data class Tilde(override val line: Int, override val column: Int) : Token {
        override val type: TokenType
            get() = TokenType.Tilde
    }

    data class Exclaimation(override val line: Int, override val column: Int) : Token {
        override val type: TokenType
            get() = TokenType.Exclaimation
    }

    data class Caret(override val line: Int, override val column: Int) : Token {
        override val type: TokenType
            get() = TokenType.Caret
    }

    data class Percent(override val line: Int, override val column: Int) : Token {
        override val type: TokenType
            get() = TokenType.Percent
    }

    data class Semicolon(override val line: Int, override val column: Int) : Token {
        override val type: TokenType
            get() = TokenType.Semicolon
    }


    data class Divide(override val line: Int, override val column: Int) : Token {
        override val type: TokenType
            get() = TokenType.Divide
    }

    data class Colon(override val line: Int, override val column: Int) : Token {
        override val type: TokenType
            get() = TokenType.Colon
    }

    data class LBrace(override val line: Int, override val column: Int) : Token {
        override val type: TokenType
            get() = TokenType.LBrace
    }

    data class RBrace(override val line: Int, override val column: Int) : Token {
        override val type: TokenType
            get() = TokenType.RBrace
    }

    data class Return(override val line: Int, override val column: Int) : Token {
        override val type: TokenType
            get() = TokenType.Return
    }

    data class Call(override val line: Int, override val column: Int) : Token {
        override val type: TokenType
            get() = TokenType.Call
    }

    data class Key(override val line: Int, override val column: Int) : Token {
        override val type: TokenType
            get() = TokenType.Key
    }

    data class MinusKey(override val line: Int, override val column: Int) : Token {
        override val type: TokenType
            get() = TokenType.MinusKey
    }

    data class Hex(override val line: Int, override val column: Int) : Token {
        override val type: TokenType
            get() = TokenType.Hex
    }

    data class BigHex(override val line: Int, override val column: Int) : Token {
        override val type: TokenType
            get() = TokenType.BigHex
    }

    data class Random(override val line: Int, override val column: Int) : Token {
        override val type: TokenType
            get() = TokenType.Random
    }

    data class Delay(override val line: Int, override val column: Int) : Token {
        override val type: TokenType
            get() = TokenType.Delay
    }

    data class Clear(override val line: Int, override val column: Int) : Token {
        override val type: TokenType
            get() = TokenType.Clear
    }

    data class BCD(override val line: Int, override val column: Int) : Token {
        override val type: TokenType
            get() = TokenType.BCD
    }

    data class Save(override val line: Int, override val column: Int) : Token {
        override val type: TokenType
            get() = TokenType.Save
    }

    data class Load(override val line: Int, override val column: Int) : Token {
        override val type: TokenType
            get() = TokenType.Load
    }

    data class Buzzer(override val line: Int, override val column: Int) : Token {
        override val type: TokenType
            get() = TokenType.Buzzer
    }

    data class If(override val line: Int, override val column: Int) : Token {
        override val type: TokenType
            get() = TokenType.If
    }

    data class Then(override val line: Int, override val column: Int) : Token {
        override val type: TokenType
            get() = TokenType.Then
    }

    data class Begin(override val line: Int, override val column: Int) : Token {
        override val type: TokenType
            get() = TokenType.Begin
    }

    data class Else(override val line: Int, override val column: Int) : Token {
        override val type: TokenType
            get() = TokenType.Else
    }

    data class End(override val line: Int, override val column: Int) : Token {
        override val type: TokenType
            get() = TokenType.End
    }

    data class Jump(override val line: Int, override val column: Int) : Token {
        override val type: TokenType
            get() = TokenType.Jump
    }

    data class Jump0(override val line: Int, override val column: Int) : Token {
        override val type: TokenType
            get() = TokenType.Jump0
    }

    data class Native(override val line: Int, override val column: Int) : Token {
        override val type: TokenType
            get() = TokenType.Native
    }

    data class Sprite(override val line: Int, override val column: Int) : Token {
        override val type: TokenType
            get() = TokenType.Sprite
    }

    data class Loop(override val line: Int, override val column: Int) : Token {
        override val type: TokenType
            get() = TokenType.Loop
    }

    data class While(override val line: Int, override val column: Int) : Token {
        override val type: TokenType
            get() = TokenType.While
    }

    data class Again(override val line: Int, override val column: Int) : Token {
        override val type: TokenType
            get() = TokenType.Again
    }

    data class ScrollDown(override val line: Int, override val column: Int) : Token {
        override val type: TokenType
            get() = TokenType.ScrollDown
    }

    data class ScrollRight(override val line: Int, override val column: Int) : Token {
        override val type: TokenType
            get() = TokenType.ScrollRight
    }

    data class ScrollLeft(override val line: Int, override val column: Int) : Token {
        override val type: TokenType
            get() = TokenType.ScrollLeft
    }

    data class Lores(override val line: Int, override val column: Int) : Token {
        override val type: TokenType
            get() = TokenType.Lores
    }

    data class Hires(override val line: Int, override val column: Int) : Token {
        override val type: TokenType
            get() = TokenType.Hires
    }

    data class LoadFlags(override val line: Int, override val column: Int) : Token {
        override val type: TokenType
            get() = TokenType.LoadFlags
    }

    data class SaveFlags(override val line: Int, override val column: Int) : Token {
        override val type: TokenType
            get() = TokenType.SaveFlags
    }

    data class I(override val line: Int, override val column: Int) : Token {
        override val type: TokenType
            get() = TokenType.I
    }

    data class Audio(override val line: Int, override val column: Int) : Token {
        override val type: TokenType
            get() = TokenType.Audio
    }

    data class Pitch(override val line: Int, override val column: Int) : Token {
        override val type: TokenType
            get() = TokenType.Pitch
    }

    data class Plane(override val line: Int, override val column: Int) : Token {
        override val type: TokenType
            get() = TokenType.Plane
    }

    data class ScrollUp(override val line: Int, override val column: Int) : Token {
        override val type: TokenType
            get() = TokenType.ScrollUp
    }

    data class Unpack(override val line: Int, override val column: Int) : Token {
        override val type: TokenType
            get() = TokenType.Unpack
    }

    data class Breakpoint(override val line: Int, override val column: Int) : Token {
        override val type: TokenType
            get() = TokenType.Breakpoint
    }

    data class Proto(override val line: Int, override val column: Int) : Token {
        override val type: TokenType
            get() = TokenType.Proto
    }

    data class Alias(override val line: Int, override val column: Int) : Token {
        override val type: TokenType
            get() = TokenType.Alias
    }

    data class Const(override val line: Int, override val column: Int) : Token {
        override val type: TokenType
            get() = TokenType.Const
    }

    data class Org(override val line: Int, override val column: Int) : Token {
        override val type: TokenType
            get() = TokenType.Org
    }

    data class Macro(override val line: Int, override val column: Int) : Token {
        override val type: TokenType
            get() = TokenType.Macro
    }

    data class Calc(override val line: Int, override val column: Int) : Token {
        override val type: TokenType
            get() = TokenType.Calc
    }

    data class Byte(override val line: Int, override val column: Int) : Token {
        override val type: TokenType
            get() = TokenType.Byte
    }

    data class StringMode(override val line: Int, override val column: Int) : Token {
        override val type: TokenType
            get() = TokenType.StringMode
    }

    data class Assert(override val line: Int, override val column: Int) : Token {
        override val type: TokenType
            get() = TokenType.Assert
    }

    data class Moniter(override val line: Int, override val column: Int) : Token {
        override val type: TokenType
            get() = TokenType.Moniter
    }

    data class Pointer(override val line: Int, override val column: Int) : Token {
        override val type: TokenType
            get() = TokenType.Pointer
    }

    data class Next(override val line: Int, override val column: Int) : Token {
        override val type: TokenType
            get() = TokenType.Next
    }

    data class Assignment(override val line: Int, override val column: Int) : Token {
        override val type: TokenType
            get() = TokenType.Assignment
    }

    data class SubtractionAssignment(override val line: Int, override val column: Int) : Token {
        override val type: TokenType
            get() = TokenType.SubtractionAssignment
    }

    data class ReverseSubtractionAssignment(override val line: Int, override val column: Int) : Token {
        override val type: TokenType
            get() = TokenType.ReverseSubtractionAssignment
    }


    data class AdditionAssignment(override val line: Int, override val column: Int) : Token {
        override val type: TokenType
            get() = TokenType.AdditionAssignment
    }

    data class OrAssignment(override val line: Int, override val column: Int) : Token {
        override val type: TokenType
            get() = TokenType.OrAssignment
    }

    data class AndAssignment(override val line: Int, override val column: Int) : Token {
        override val type: TokenType
            get() = TokenType.AndAssignment
    }

    data class XorAssignment(override val line: Int, override val column: Int) : Token {
        override val type: TokenType
            get() = TokenType.XorAssignment
    }

    data class ShiftLeft(override val line: Int, override val column: Int) : Token {
        override val type: TokenType
            get() = TokenType.ShiftLeft
    }

    data class ShiftRight(override val line: Int, override val column: Int) : Token {
        override val type: TokenType
            get() = TokenType.ShiftRight
    }

    data class ShiftLeftAssign(override val line: Int, override val column: Int) : Token {
        override val type: TokenType
            get() = TokenType.ShiftLeftAssign
    }

    data class ShiftRightAssign(override val line: Int, override val column: Int) : Token {
        override val type: TokenType
            get() = TokenType.ShiftRightAssign
    }

    data class GreaterThan(override val line: Int, override val column: Int) : Token {
        override val type: TokenType
            get() = TokenType.GreaterThan
    }

    data class LessThan(override val line: Int, override val column: Int) : Token {
        override val type: TokenType
            get() = TokenType.LessThan
    }

    data class GreaterThanOrEqual(override val line: Int, override val column: Int) : Token {
        override val type: TokenType
            get() = TokenType.GreaterThanOrEqual
    }

    data class LessThanOrEqual(override val line: Int, override val column: Int) : Token {
        override val type: TokenType
            get() = TokenType.LessThanOrEqual
    }

    data class Equal(override val line: Int, override val column: Int) : Token {
        override val type: TokenType
            get() = TokenType.Equal
    }

    data class NotEqual(override val line: Int, override val column: Int) : Token {
        override val type: TokenType
            get() = TokenType.NotEqual
    }

    data class Plus(override val line: Int, override val column: Int) : Token {
        override val type: TokenType
            get() = TokenType.Plus
    }

    data class Minus(override val line: Int, override val column: Int) : Token {
        override val type: TokenType
            get() = TokenType.Minus
    }

    data class ForwardIdentifier(val name: String, override val line: Int, override val column: Int) : Token {
        override val type: TokenType
            get() = TokenType.ForwardIdentifier

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
            return Register(Registers.valueOf(identifier.lowercase()), line, startColumn)
        }
    }
}

enum class TokenType {
    StringToken,


    Identifier,
    Number,
    Error,

    Register,
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
    I,
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