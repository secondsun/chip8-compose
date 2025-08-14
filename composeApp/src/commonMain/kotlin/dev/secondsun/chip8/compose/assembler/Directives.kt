package dev.secondsun.chip8.compose.assembler

val DIRECTIVES = setOf("key",
    ":=",
    "hex",
    "bighex",
    "random",
    "delay",
    ";",
    "return",
    "clear",
    "bcd",
    "save",
    "load",
    "buzzer",
    "if",
    "then",
    "begin",
    "else",
    "end",
    "jump",
    "jump0",
    "native",
    "sprite",
    "loop",
    "while",
    "again",
    "scroll-down",
    "scroll-right",
    "scroll-left",
    "lores",
    "hires",
    "loadflags",
    "saveflags",
    "i",
    "audio","pitch",
    "plane",
    "scroll-up",
    ":next",
    ":unpack",
    ":breakpoint",
    ":proto",
    ":alias",
    ":const",
    ":org",
    ":macro",
    ":calc",
    ":byte",
    ":call",
    ":stringmode",
    ":assert",
    ":monitor",
    ":pointer")


/**
 * 	":="
 * 	"|="
 * 	"&="
 * 	"^="
 * 	"-="
 * 	"=-"
 * 	"+="
 * 	">>="
 * 	"<<=" "=="
 * 	"!="
 * 	"<"
 * 	">"
 * 	"<="
 * 	">="
 * 	"-key"


 *
 * 	":"

 *
 */