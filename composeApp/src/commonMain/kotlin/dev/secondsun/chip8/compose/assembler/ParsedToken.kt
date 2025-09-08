package dev.secondsun.chip8.compose.assembler

import java.net.http.HttpResponse

open class ParsedToken(val type : ParsedTokenType, val tokens: List<Token>) {
    override fun toString(): String {
        return "${type}, tokens size : ${tokens.size}, lines ${tokens.map { it.line }.toSet()}"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ParsedToken

        if (type != other.type) return false
        if (tokens != other.tokens) return false

        return true
    }

    override fun hashCode(): Int {
        var result = type.hashCode()
        result = 31 * result + tokens.hashCode()
        return result
    }


}

class ParsedConditionalToken(type : ParsedTokenType, tokens: List<Token>, val condition: List<ParsedToken>) : ParsedToken(type, tokens) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as ParsedConditionalToken

        return condition == other.condition
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + condition.hashCode()
        return result
    }
}

class ParsedMacroExpandToken(tokens: List<Token>, val params: List<Token>) : ParsedToken(ParsedTokenType.MacroExpand, tokens) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as ParsedMacroExpandToken

        return params == other.params
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + params.hashCode()
        return result
    }
}

class ParsedMacroToken(tokens: List<Token>, val params: List<Token>, val body: List<Token>, var calls: Int = 0) : ParsedToken(ParsedTokenType.Macro, tokens) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as ParsedMacroToken

        if (params != other.params) return false
        if (body != other.body) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + params.hashCode()
        result = 31 * result + body.hashCode()
        return result
    }
}

enum class ParsedTokenType {
    Error,
    Label,
    Constant,
    Number,
    Next,
    Unpack,
    Alias,
    Breakpoint,
    Monitor,
    Org,
    Macro,
    StringMode,
    Calc,
    Byte,
    Pointer,
    Assert,
    Return,
    Clear,
    BCD,
    Save,
    Load,
    Delay,
    Buzzer,
    Pitch,
    If,
    Then,

    Else,
    Begin,
    End,
    Jump,
    Jump0,
    Native,
    Sprite,
    Loop,
    While,
    Again,
    Plane,
    Audio,
    ScrollDown,
    ScrollRight,
    ScrollLeft,
    ScrollUp,
    Exit,
    Lores,
    Hires,
    SaveFlags,
    I,
    Call,
    Condition,
    Assignment,
    LoadFlags,
    IAdditionAssign,
    IAssign,
    MacroExpand,
    Immediate


}
