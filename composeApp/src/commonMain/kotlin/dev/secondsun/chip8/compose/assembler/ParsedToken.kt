package dev.secondsun.chip8.compose.assembler

import java.net.http.HttpResponse

open class ParsedToken(val type : ParsedTokenType, val tokens: List<Token>) {
    override fun toString(): String {
        return "${type}, tokens size : ${tokens.size}, lines ${tokens.map { it.line }.toSet()}"
    }
}

class ParsedConditionalToken(type : ParsedTokenType, tokens: List<Token>, val condition: List<ParsedToken>) : ParsedToken(type, tokens)

class ParsedMacroExpandToken(tokens: List<Token>, val params: List<Token>) : ParsedToken(ParsedTokenType.MacroExpand, tokens)

class ParsedMacroToken(tokens: List<Token>, val params: List<Token>, val body: List<Token>) : ParsedToken(ParsedTokenType.Macro, tokens)

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
    MacroExpand


}
