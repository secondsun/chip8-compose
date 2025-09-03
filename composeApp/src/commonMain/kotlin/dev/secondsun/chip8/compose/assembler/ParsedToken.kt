package dev.secondsun.chip8.compose.assembler

open class ParsedToken(val type : ParsedTokenType, val tokens: List<Token>)

class ParsedConditionalToken(type : ParsedTokenType, tokens: List<Token>, val condition: List<ParsedToken>) : ParsedToken(type, tokens)

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
    IAssign


}
