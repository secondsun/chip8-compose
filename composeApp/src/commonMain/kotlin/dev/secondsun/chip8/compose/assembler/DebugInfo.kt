package dev.secondsun.chip8.compose.assembler 

class DebugInfo(val source: String) {
    fun mapAddr(addr: Int, line: Int) {
        this._locs[addr] = line;
    }

    val lines = source.split("\n")
    val _locs = mutableMapOf<Int, Int>()//Address, Line
}
