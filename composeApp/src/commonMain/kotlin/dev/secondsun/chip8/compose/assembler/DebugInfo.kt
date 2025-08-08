package dev.secondsun.chip8.compose.assembler

class DebugInfo(source: String) {
    private val lines: List<String> = source.split('\n')
    private val locs = mutableMapOf<Int, Int>() // map<addr, pos>

    fun mapAddr(addr: Int, pos: Int) {
        locs[addr] = pos
    }

    fun getLine(addr: Int): Int? {
        val i = locs[addr] ?: return null
        return posToLine(i)
    }

    fun getAddr(line: Int): Int? {
        for ((addr, pos) in locs) {
            if (posToLine(pos) == line) return addr
        }
        return null
    }

    fun posToLine(pos: Int): Int {
        var p = pos
        for (i in lines.indices) {
            p -= (lines[i].length + 1)
            if (p <= 0) return i
        }
        return lines.lastIndex
    }
}