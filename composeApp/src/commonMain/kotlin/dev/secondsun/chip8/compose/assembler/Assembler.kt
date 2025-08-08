package dev.secondsun.chip8.compose.assembler

import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sign
import kotlin.math.sin
import kotlin.math.cos
import kotlin.math.tan

class Assembler private constructor(
    private val hasMainConfigured: Boolean,
    private val initialHere: Int
) {

    class Builder {
        var hasMain: Boolean = true
        var startAddress: Int = 0x200

        fun build(): Assembler = Assembler(hasMain, startAddress)
    }

    private data class RawToken(val value: Any, val start: Int, val end: Int)

    // State
    private lateinit var source: String
    private val rom = ArrayList<Int>() // int [0..255], later to UByteArray
    private lateinit var dbginfo: DebugInfo

    private val loops = ArrayDeque<Pair<Int, RawToken>>() // [addr, token-pos]
    private val branches = ArrayDeque<Triple<Int, RawToken, String>>() // [addr, pos, "begin"/"else"]
    private val whiles = ArrayDeque<Int?>()

    private val dict = linkedMapOf<String, Int>() // labels
    private val protos = linkedMapOf<String, MutableList<Int>>() // unresolved positions
    private val longProto = hashSetOf<Int>()
    private val aliases = linkedMapOf<String, Int>()
    private val constants = linkedMapOf<String, Int>(
        "OCTO_KEY_1" to 0x1,
        "OCTO_KEY_2" to 0x2,
        "OCTO_KEY_3" to 0x3,
        "OCTO_KEY_4" to 0xC,
        "OCTO_KEY_Q" to 0x4,
        "OCTO_KEY_W" to 0x5,
        "OCTO_KEY_E" to 0x6,
        "OCTO_KEY_R" to 0xD,
        "OCTO_KEY_A" to 0x7,
        "OCTO_KEY_S" to 0x8,
        "OCTO_KEY_D" to 0x9,
        "OCTO_KEY_F" to 0xE,
        "OCTO_KEY_Z" to 0xA,
        "OCTO_KEY_X" to 0x0,
        "OCTO_KEY_C" to 0xB,
        "OCTO_KEY_V" to 0xF,
    )
    private val mutables = hashSetOf<String>() // track :calc names
    private val macros = linkedMapOf<String, MacroBody>()
    private val stringModes = linkedMapOf<String, StringMode>()
    private var hasMain = hasMainConfigured
    private var schip = false
    private var xo = false
    private val breakpoints = linkedMapOf<Int, String>()
    private val monitors = linkedMapOf<String, Monitor>()
    private var hereaddr = initialHere

    // tokenizer/iteration
    private var pos: RawToken? = null
    private var currentToken = 0
    private lateinit var tokens: MutableList<RawToken>

    // ROM max size enforcement
    private fun maxRom(): Int {
        return when {
            xo -> 65024 // regardless of schip
            schip -> 3583
            else -> 3232
        }
    }

    // Public entry
    fun compile(text: String): AssemblerResult {
        source = normalizeNewlines(text)
        dbginfo = DebugInfo(source)
        aliases["unpack-hi"] = 0x0
        aliases["unpack-lo"] = 0x1

        tokens = tokenize(source).toMutableList()
        inst(0, 0) // reserve jump
        while (!end()) {
            val pk = peek()
            if (pk is Number) {
                val nn = next() as Number
                val nni = nn.toInt()
                if (nni < -128 || nni > 255) fail("Literal value '$nni' does not fit in a byte- must be in range [-128,255].")
                data(nni)
            } else {
                instruction(next())
            }
        }
        if (hasMain) {
            if (!constants.containsKey("main") && !dict.containsKey("main")) {
                pos = RawToken("<EOF>", source.length + 1, source.length + 2)
                fail("This program is missing a 'main' label.")
            }
            jump(0x200, wideValue("main"))
        }

        if (protos.isNotEmpty()) {
            // report first unresolved proto
            val missing = protos.keys.first()
            fail("Undefined forward reference: $missing")
        }
        if (loops.isNotEmpty()) {
            pos = loops.removeLast().second
            fail("This 'loop' does not have a matching 'again'.")
        }
        if (branches.isNotEmpty()) {
            val flow = branches.removeLast()
            pos = flow.second
            fail("This '${flow.third}' does not have a matching 'end'.")
        }

        // zero-fill
        for (i in 0 until rom.size) {
            if (rom[i] == Int.MIN_VALUE) rom[i] = 0x00
        }

        // bounds
        val sz = rom.size
        val max = maxRom()
        if (sz > max) {
            fail("ROM size $sz exceeds maximum allowed size $max bytes for current chip mode.")
        }

        val resultRom = UByteArray(rom.size) { (rom[it] and 0xFF).toUByte() }
        return AssemblerResult(
            rom = resultRom,
            debugInfo = dbginfo,
            hasMain = hasMain,
            schip = schip,
            xo = xo,
            breakpoints = breakpoints.toMap(),
            monitors = monitors.toMap(),
            hereAddr = hereaddr,
            labels = dict.toMap(),
            aliases = aliases.toMap(),
            constants = constants.toMap()
        )
    }

    // Core helpers
    private fun normalizeNewlines(s: String) = s.replace("\r\n", "\n").replace('\r', '\n')

    private fun data(a: Int) {
        val idx = hereaddr - 0x200
        if (idx >= 0 && idx < rom.size && rom[idx] != Int.MIN_VALUE) {
            fail("Data overlap. Address 0x${hereaddr.toString(16).uppercase()} has already been defined.")
        }
        ensureRomCapacity(idx)
        rom[idx] = a and 0xFF
        pos?.let { dbginfo.mapAddr(hereaddr, it.start) }
        hereaddr++
    }

    private fun ensureRomCapacity(idx: Int) {
        if (idx < 0) return
        if (idx >= rom.size) {
            val toAdd = idx - rom.size + 1
            repeat(toAdd) { rom.add(Int.MIN_VALUE) }
        }
    }

    private fun inst(a: Int, b: Int) {
        data(a); data(b)
    }

    private fun immediate(op: Int, nnn: Int) {
        inst(op or ((nnn shr 8) and 0xF), nnn and 0xFF)
    }

    private fun fourop(op: Int, x: Int, y: Int, n: Int) {
        inst((op shl 4) or x, (y shl 4) or (n and 0xF))
    }

    private fun jump(addr: Int, dest: Int) {
        val i0 = addr - 0x200
        val i1 = addr - 0x1FF
        ensureRomCapacity(maxOf(i1, i0))
        rom[i0] = 0x10 or ((dest shr 8) and 0xF)
        rom[i1] = dest and 0xFF
    }

    private fun here(): Int = hereaddr

    // Token iteration
    private fun end(): Boolean = currentToken >= tokens.size
    private fun peek(): Any = tokens[currentToken].value
    private fun raw(): RawToken = tokens[currentToken++].also { pos = it }
    private fun next(): Any {
        if (end()) fail("Unexpected EOF.")
        pos = tokens[currentToken++]
        return pos!!.value
    }

    // Registers
    private fun isRegister(name: Any? = null): Boolean {
        val n = (name ?: peek())
        if (n !is String) return false
        if (n in aliases) return true
        val u = n.uppercase()
        if (u.length != 2 || u[0] != 'V') return false
        return "0123456789ABCDEF".contains(u[1])
    }

    private fun register(name: Any? = null): Int {
        val n = name ?: next()
        if (!isRegister(n)) fail("Expected register, got '$n'.")
        if (n is String && aliases.containsKey(n)) return aliases.getValue(n)
        val u = (n as String).uppercase()
        return "0123456789ABCDEF".indexOf(u[1])
    }

    private fun expect(token: String) {
        val thing = next()
        if (thing != token) fail("Expected $token, got '$thing'.")
    }

    // Values
    private fun constantValue(): Int {
        var number = next()
        if (number is Number) return number.toInt()

        val name = number as String
        if (name in protos) fail("A constant reference to '$name' may not be forward-declared.")
        if (name in reservedNames) fail("Expected a constant name, but found the keyword '$name'. Missing a token?")
        if (name in dict) return dict.getValue(name)
        if (name in constants) return constants.getValue(name)
        fail("Undefined constant name '$name'.")
    }

    private val reservedNames = setOf(
        ":=", "|=", "&=", "^=", "-=", "=-", "+=",
        ">>=", "<<=", "==", "!=", "<", ">", "<=", ">=", "key", "-key",
        "hex", "bighex", "random", "delay", ":", ":next", ":unpack",
        ":breakpoint", ":proto", ":alias", ":const", ":org", ";", "return", "clear",
        "bcd", "save", "load", "buzzer", "if", "then", "begin", "else", "end",
        "jump", "jump0", "native", "sprite", "loop", "while", "again", "scroll-down",
        "scroll-right", "scroll-left", "lores", "hires", "loadflags", "saveflags", "i",
        "audio", "plane", "scroll-up", ":macro", ":calc", ":byte", ":call", ":stringmode",
        ":assert", ":monitor", ":pointer", "pitch"
    )

    private fun checkName(name: String, kind: String): String {
        if (name in reservedNames || name.startsWith("OCTO_")) {
            fail("The name '$name' is reserved and cannot be used for a $kind.")
        }
        return name
    }

    private fun identifier(kind: String): String {
        val n = next()
        if (n !is String) fail("Expected a name for a $kind, got $n.")
        return checkName(n, kind)
    }

    private fun string(): String {
        val n = next()
        if (n !is String) fail("Expected a string, got $n.")
        return n
    }

    private fun valueFail(type: String, value: Any, checkIfDefined: Boolean) {
        if (isRegister(value)) fail("Expected $type value, but found the register $value.")
        if (value in reservedNames) fail("Expected $type value, but found the keyword '$value'. Missing a token?")
        if (checkIfDefined && value is String && (value !in constants) && (value !in dict)) {
            fail("Expected $type value, but found the undefined name '$value'.")
        }
    }

    private fun valueRange(type: String, value: Int, min: Int, max: Int) {
        if (value < min || value > max) {
            fail("Argument ${value} does not fit in $type.")
        }
    }

    private fun veryWideValue(noForward: Boolean = false, noOffset: Boolean = false): Int {
        var nnnn = next()
        val target = here() + if (noOffset) 0 else 2
        if (nnnn !is Number) {
            valueFail("a 16-bit value", nnnn, false)
            val name = nnnn as String
            nnnn = when {
                name in constants -> constants.getValue(name)
                name in dict -> dict.getValue(name)
                noForward -> fail("The reference to '$name' may not be forward-declared.")
                name in protos -> {
                    protos.getValue(name).add(target)
                    longProto.add(target)
                    0
                }
                else -> {
                    protos[checkName(name, "label")] = mutableListOf(target)
                    longProto.add(target)
                    0
                }
            }
        }
        val vi = (nnnn as Number).toInt()
        valueRange("16 bits", vi, 0, 0xFFFF)
        return vi and 0xFFFF
    }

    private fun wideValue(nnnIn: Any? = null): Int {
        var nnn = nnnIn
        if (nnn == null || (nnn is Int && nnn != 0)) nnn = next()
        if (nnn !is Number) {
            if (isRegister(nnn)) fail("Expected a 12-bit value, but found the register $nnn.")
            val name = nnn as String
            nnn = when {
                name in constants -> constants.getValue(name)
                name in protos -> {
                    protos.getValue(name).add(here()); 0
                }
                name in dict -> dict.getValue(name)
                else -> {
                    protos[checkName(name, "label")] = mutableListOf(here()); 0
                }
            }
        }
        val vi = (nnn as Number).toInt()
        valueRange("12 bits", vi, 0, 0xFFF)
        return vi and 0xFFF
    }

    private fun shortValue(nnIn: Any? = null): Int {
        var nn = nnIn
        if (nn == null || (nn is Int && nn != 0)) nn = next()
        if (nn !is Number) {
            valueFail("an 8-bit", nn, true)
            nn = constants[nn as String]!!
        }
        val vi = (nn as Number).toInt()
        // negative allowed (trim), but limit positive
        if (vi < -128 || vi > 255) fail("Argument ${vi} does not fit in a byte- must be in range [-128,255].")
        return vi and 0xFF
    }

    private fun tinyValue(): Int {
        val n = next()
        if (n !is Number) {
            valueFail("a 4-bit", n, true)
            val c = constants[n as String] ?: fail("Undefined constant '$n'.")
            if (c < 0 || c > 15) fail("Argument $c does not fit in 4 bits- must be in range [0,15].")
            return c and 0xF
        }
        val vi = n.toInt()
        if (vi < 0 || vi > 15) fail("Argument ${vi} does not fit in 4 bits- must be in range [0,15].")
        return vi and 0xF
    }

    // Conditions
    private fun conditional(negated: Boolean) {
        val reg = register()
        var token = next()
        var compTemp = 0xF
        if (negated) {
            token = when (token) {
                "==" -> "!="
                "!=" -> "=="
                "key" -> "-key"
                "-key" -> "key"
                "<" -> ">="
                ">" -> "<="
                ">=" -> "<"
                "<=" -> ">"
                else -> token
            }
        }
        when (token) {
            "==" -> {
                if (isRegister()) fourop(0x8, reg, register(), 0x0)
                else inst(0x40 or reg, shortValue())
            }
            "!=" -> {
                if (isRegister()) fourop(0x8, reg, register(), 0x5)
                else inst(0x30 or reg, shortValue())
            }
            "key" -> inst(0xE0 or reg, 0xA1)
            "-key" -> inst(0xE0 or reg, 0x9E)
            ">" -> {
                if (isRegister()) fourop(0x8, compTemp, register(), 0x0)
                else inst(0x60 or compTemp, shortValue())
                fourop(0x8, compTemp, reg, 0x5)
                inst(0x4F, 0)
            }
            "<" -> {
                if (isRegister()) fourop(0x8, compTemp, register(), 0x0)
                else inst(0x60 or compTemp, shortValue())
                fourop(0x8, compTemp, reg, 0x7)
                inst(0x4F, 0)
            }
            ">=" -> {
                if (isRegister()) fourop(0x8, compTemp, register(), 0x0)
                else inst(0x60 or compTemp, shortValue())
                fourop(0x8, compTemp, reg, 0x7)
                inst(0x3F, 0)
            }
            "<=" -> {
                if (isRegister()) fourop(0x8, compTemp, register(), 0x0)
                else inst(0x60 or compTemp, shortValue())
                fourop(0x8, compTemp, reg, 0x5)
                inst(0x3F, 0)
            }
            else -> fail("Expected conditional operator, got ${token}.")
        }
    }

    private fun controlToken(): RawToken {
        val op = tokens.getOrNull(currentToken + 1)?.value
        var index = 3
        if (op == "key" || op == "-key") index = 2
        val idx = (currentToken + index).coerceAtMost(tokens.lastIndex)
        return tokens[idx]
    }

    // Assignments
    private fun iassign(token: Any) {
        when (token) {
            ":=" -> {
                val o = next()
                when (o) {
                    "hex" -> inst(0xF0 or register(), 0x29)
                    "bighex" -> {
                        schip = true
                        inst(0xF0 or register(), 0x30)
                    }
                    "long" -> {
                        xo = true
                        val addr = veryWideValue()
                        inst(0xF0, 0x00)
                        inst((addr shr 8) and 0xFF, addr and 0xFF)
                    }
                    else -> immediate(0xA0, wideValue(o))
                }
            }
            "+=" -> inst(0xF0 or register(), 0x1E)
            else -> fail("'$token' is not an operator that can target the i register.")
        }
    }

    private fun vassign(reg: Int) {
        when (val token = next()) {
            ":=" -> {
                val o = next()
                when {
                    isRegister(o) -> fourop(0x8, reg, register(o), 0x0)
                    o == "random" -> inst(0xC0 or reg, shortValue())
                    o == "key" -> inst(0xF0 or reg, 0x0A)
                    o == "delay" -> inst(0xF0 or reg, 0x07)
                    else -> inst(0x60 or reg, shortValue(o))
                }
            }
            "+=" -> {
                if (isRegister()) fourop(0x8, reg, register(), 0x4)
                else inst(0x70 or reg, shortValue())
            }
            "|=" -> fourop(0x8, reg, register(), 0x1)
            "&=" -> fourop(0x8, reg, register(), 0x2)
            "^=" -> fourop(0x8, reg, register(), 0x3)
            "-=" -> {
                if (isRegister()) fourop(0x8, reg, register(), 0x5)
                else inst(0x70 or reg, 0xFF and (1 + ~shortValue()))
            }
            "=-" -> fourop(0x8, reg, register(), 0x7)
            ">>=" -> fourop(0x8, reg, register(), 0x6)
            "<<=" -> fourop(0x8, reg, register(), 0xE)
            else -> fail("Unrecognized operator '$token'.")
        }
    }

    // Labels
    private fun resolveLabel(offset: Int) {
        var target = here() + offset
        val label = identifier("label")
        if ((target == 0x202 || target == 0x200) && label == "main") {
            hasMain = false
            hereaddr = 0x200
            if (rom.isNotEmpty()) {
                // erase reserved jump
                if (rom.size >= 2) {
                    rom[0] = Int.MIN_VALUE
                    rom[1] = Int.MIN_VALUE
                }
            }
            target = here()
        }
        if (label in dict) fail("The name '$label' has already been defined.")
        if (label in aliases) fail("The name '$label' is already used by an alias.")
        dict[label] = target

        protos.remove(label)?.forEach { addr ->
            if (addr in longProto && (rom[addr - 0x200] and 0xF0) == 0x60) {
                // :unpack long target
                rom[addr - 0x1FF] = (target shr 8) and 0xFF
                ensureRomCapacity(addr - 0x1FD)
                rom[addr - 0x1FD] = target and 0xFF
            } else if (addr in longProto) {
                // i := long target
                rom[addr - 0x200] = (target shr 8) and 0xFF
                rom[addr - 0x1FF] = target and 0xFF
            } else if ((target and 0xFFF) != target) {
                fail("Value 0x${target.toString(16).uppercase()} for label '$label' does not fit in 12 bits.")
            } else if ((rom[addr - 0x200] and 0xF0) == 0x60) {
                // :unpack target
                rom[addr - 0x1FF] = (rom[addr - 0x1FF] and 0xF0) or ((target shr 8) and 0xF)
                rom[addr - 0x1FD] = target and 0xFF
            } else {
                rom[addr - 0x200] = (rom[addr - 0x200] and 0xF0) or ((target shr 8) and 0xF)
                rom[addr - 0x1FF] = target and 0xFF
            }
        }
    }

    // Calculated constants
    private fun parseTerminal(name: String): Double {
        val x = peek()
        when (x) {
            "PI" -> return next().let { Math.PI }
            "E" -> return next().let { Math.E }
            "HERE" -> return next().let { hereaddr.toDouble() }
        }
        if (isRegister(x)) return next().let { register(x).toDouble() }
        if (x is Number) return next().let { (x as Number).toDouble() }
        if (x is String && x in constants) return next().let { constants.getValue(x).toDouble() }
        if (x is String && x in dict) return next().let { dict.getValue(x).toDouble() }
        if (x is String && x in protos) {
            next()
            fail("Cannot use forward declaration '$x' when calculating constant '$name'.")
        }
        val n = next()
        if (n != "(") fail("Found undefined name '$n' when calculating constant '$name'.")
        val value = parseCalc(name)
        if (next() != ")") fail("Expected ')' for calculated constant '$name'.")
        return value
    }

    private val unaryFunc: Map<String, (Double) -> Double> = mapOf(
        "-" to { -it },
        "~" to { it.toLong().inv().toDouble() },
        "!" to { if (it == 0.0) 1.0 else 0.0 },
        "sin" to { sin(it) },
        "cos" to { cos(it) },
        "tan" to { tan(it) },
        "exp" to { kotlin.math.exp(it) },
        "log" to { kotlin.math.ln(it) },
        "abs" to { kotlin.math.abs(it) },
        "sqrt" to { kotlin.math.sqrt(it) },
        "sign" to { sign(it) },
        "ceil" to { ceil(it) },
        "floor" to { floor(it) },
        "@" to { x -> // memory lookup
            if (x.isNaN()) 0.0 else {
                val idx = x.toInt() - 0x200
                if (idx in rom.indices) (rom[idx] and 0xFF).toDouble() else 0.0
            }
        }
    )
    private val binaryFunc: Map<String, (Double, Double) -> Double> = mapOf(
        "-" to { x, y -> x - y },
        "+" to { x, y -> x + y },
        "*" to { x, y -> x * y },
        "/" to { x, y -> x / y },
        "%" to { x, y -> x % y },
        "&" to { x, y -> (x.toLong() and y.toLong()).toDouble() },
        "|" to { x, y -> (x.toLong() or y.toLong()).toDouble() },
        "^" to { x, y -> (x.toLong() xor y.toLong()).toDouble() },
        "<<" to { x, y -> (x.toLong() shl y.toInt()).toDouble() },
        ">>" to { x, y -> (x.toLong() shr y.toInt()).toDouble() },
        "pow" to { x, y -> x.pow(y) },
        "min" to { x, y -> min(x, y) },
        "max" to { x, y -> max(x, y) },
        "<" to { x, y -> if (x < y) 1.0 else 0.0 },
        ">" to { x, y -> if (x > y) 1.0 else 0.0 },
        "<=" to { x, y -> if (x <= y) 1.0 else 0.0 },
        ">=" to { x, y -> if (x >= y) 1.0 else 0.0 },
        "==" to { x, y -> if (x == y) 1.0 else 0.0 },
        "!=" to { x, y -> if (x != y) 1.0 else 0.0 },
    )

    private fun parseCalc(name: String): Double {
        if (peek() == "strlen") {
            next()
            return string().length.toDouble()
        }
        val p = peek()
        if (p is String && p in unaryFunc) {
            val op = next() as String
            return unaryFunc.getValue(op)(parseCalc(name))
        }
        val t = parseTerminal(name)
        val q = peek()
        if (q is String && q in binaryFunc) {
            val op = next() as String
            return binaryFunc.getValue(op)(t, parseCalc(name))
        }
        return t
    }

    private fun parseCalculated(name: String): Int {
        if (next() != "{") fail("Expected '{' for calculated constant '$name'.")
        val value = parseCalc(name)
        if (next() != "}") fail("Expected '}' for calculated constant '$name'.")
        return value.toInt()
    }

    // Macro & string mode bodies
    private fun macroBody(name: String, desc: String): List<RawToken> {
        if (next() != "{") fail("Expected '{' for definition of $desc '$name'.")
        val body = mutableListOf<RawToken>()
        var depth = 1
        while (!end()) {
            if (peek() == "{") depth += 1
            if (peek() == "}") depth -= 1
            if (depth == 0) break
            body.add(raw())
        }
        if (next() != "}") fail("Expected '}' for definition of $desc '$name'.")
        return body
    }

    private fun instruction(token: Any) {
        when (token) {
            ":" -> resolveLabel(0)
            ":next" -> resolveLabel(1)
            ":unpack" -> {
                val a = if (peek() == "long") {
                    next()
                    veryWideValue(noOffset = true)
                } else {
                    val v = tinyValue()
                    (v shl 12) or wideValue()
                }
                inst(0x60 or aliases.getValue("unpack-hi"), a shr 8)
                inst(0x60 or aliases.getValue("unpack-lo"), a and 0xFF)
            }
            ":breakpoint" -> {
                val name = string()
                breakpoints[here()] = name
            }
            ":monitor" -> {
                val name = peek()
                if (isRegister()) {
                    val r = register()
                    val len = parseMonitorLength()
                    monitors[name.toString()] = Monitor.Register(name.toString(), r, len)
                } else {
                    val base = veryWideValue(noForward = true)
                    val len = parseMonitorLength()
                    monitors[name.toString()] = Monitor.Memory(name.toString(), base, len)
                }
            }
            ":proto" -> run { next() } // deprecated in original
            ":alias" -> {
                val name = identifier("alias")
                if (name in dict) fail("The name '$name' is already used by a constant.")
                val valOrReg = if (peek() == "{") parseCalculated("ANONYMOUS") else register()
                if (valOrReg < 0 || valOrReg >= 16) fail("Register index must be in the range [0,F].")
                aliases[name] = valOrReg
            }
            ":const" -> {
                val name = identifier("constant")
                if (name in constants) fail("The name '$name' has already been defined.")
                constants[name] = constantValue()
            }
            ":macro" -> {
                val name = identifier("macro")
                if (name in macros) fail("The name '$name' has already been defined.")
                val args = mutableListOf<String>()
                while (peek() != "{" && !end()) {
                    val arg = next()
                    if (arg !is String) fail("Expected a macro argument name, got $arg")
                    args += checkName(arg, "macro argument")
                }
                val body = macroBody(name, "macro")
                val mb = MacroBody().also {
                    it.args.addAll(args)
                    it.calls = 0
                    it.body.addAll(body.map { t -> t.value.toString() })
                }
                // Store raw body in a token-aware side list for expansion
                // We’ll keep separate structure for actual body tokens:
                macroRawBodies[name] = body
                macros[name] = mb
            }
            in macros.keys -> {
                val name = token as String
                val macro = macros.getValue(name)
                val bindings = hashMapOf<String, RawToken>()
                bindings["CALLS"] = RawToken(macro.calls++, pos?.start ?: 0, pos?.end ?: 0)
                for (x in 0 until macro.args.size) {
                    if (end()) {
                        pos = RawToken("<EOF>", source.length + 1, source.length + 2)
                        fail("Not enough arguments for expansion of macro '$name'.")
                    }
                    bindings[macro.args[x]] = raw()
                }
                // expand into token stream
                val bodyTokens = macroRawBodies.getValue(name)
                for ((i, chunk) in bodyTokens.withIndex()) {
                    val v = (bindings[chunk.value] ?: chunk)
                    tokens.add(currentToken + i, v)
                }
            }
            ":stringmode" -> {
                val name = identifier("stringmode")
                val mode = stringModes.getOrPut(name) { StringMode() }
                val alphabet = string()
                val alphabetPos = pos!!
                val macro = macroBody(name, "string mode")
                alphabet.forEachIndexed { index, ch ->
                    if (mode.bodies.containsKey(ch.toString())) {
                        pos = RawToken(ch.toString(), alphabetPos.start + index + 1, alphabetPos.start + index + 2)
                        fail("String mode '$name' is already defined for the character '$ch'.")
                    }
                    mode.values[ch.toString()] = index
                    mode.bodies[ch.toString()] = macro
                }
            }
            in stringModes.keys -> {
                val name = token as String
                val mode = stringModes.getValue(name)
                val s = next()
                if (s is Number) fail("String mode '$name' cannot be applied to a number ($s).")
                val str = s as String
                var insertion = currentToken
                val stringPos = pos!!
                str.forEachIndexed { index, ch ->
                    val key = ch.toString()
                    val macro = mode.bodies[key] ?: run {
                        pos = RawToken(key, stringPos.start + index + 1, stringPos.start + index + 2)
                        fail("String mode '$name' is not defined for the character '$ch'.")
                    }
                    val bindings = hashMapOf<String, RawToken>()
                    bindings["CALLS"] = RawToken(mode.calls++, 0, 0)
                    bindings["CHAR"] = RawToken(ch.code, 0, 0)
                    bindings["INDEX"] = RawToken(index, 0, 0)
                    bindings["VALUE"] = RawToken(mode.values.getValue(key), 0, 0)
                    for (chunk in macro) {
                        val v = bindings[chunk.value] ?: chunk
                        tokens.add(insertion++, v)
                    }
                }
            }
            ":calc" -> {
                val name = identifier("calculated constant")
                if (name in constants && name !in mutables) fail("Cannot redefine the name '$name' with :calc.")
                constants[name] = parseCalculated(name)
                mutables.add(name)
            }
            ":byte" -> {
                val v = if (peek() == "{") parseCalculated("ANONYMOUS") else shortValue()
                data(v)
            }
            ":pointer" -> {
                val addr = if (peek() == "{") parseCalculated("ANONYMOUS") else veryWideValue(noOffset = true)
                inst(addr shr 8, addr and 0xFF)
            }
            ":org" -> {
                val addr = if (peek() == "{") parseCalculated("ANONYMOUS") else constantValue()
                hereaddr = addr and 0xFFFF
            }
            ":assert" -> {
                val message = if (peek() == "{") null else string()
                val value = parseCalculated(message?.let { "assert $it" } ?: "assert")
                if (value == 0) fail(message?.let { "Assertion failed: $it" } ?: "Assertion failed.")
            }
            ";" , "return" -> inst(0x00, 0xEE)
            "clear" -> inst(0x00, 0xE0)
            "bcd" -> inst(0xF0 or register(), 0x33)
            "save" -> {
                val reg = register()
                if (!end() && peek() == "-") {
                    expect("-"); xo = true
                    inst(0x50 or reg, (register() shl 4) or 0x02)
                } else inst(0xF0 or reg, 0x55)
            }
            "load" -> {
                val reg = register()
                if (!end() && peek() == "-") {
                    expect("-"); xo = true
                    inst(0x50 or reg, (register() shl 4) or 0x03)
                } else inst(0xF0 or reg, 0x65)
            }
            "delay" -> { expect(":="); inst(0xF0 or register(), 0x15) }
            "buzzer" -> { expect(":="); inst(0xF0 or register(), 0x18) }
            "pitch" -> { expect(":="); inst(0xF0 or register(), 0x3A) }
            "if" -> {
                val control = controlToken()
                when (control.value) {
                    "then" -> {
                        conditional(false)
                        expect("then")
                    }
                    "begin" -> {
                        conditional(true)
                        expect("begin")
                        branches.addLast(Triple(here(), pos!!, "begin"))
                        inst(0x00, 0x00)
                    }
                    else -> {
                        pos = control
                        fail("Expected 'then' or 'begin'.")
                    }
                }
            }
            "else" -> {
                if (branches.isEmpty()) fail("This 'else' does not have a matching 'begin'.")
                val prev = branches.removeLast().first
                jump(prev, here() + 2)
                branches.addLast(Triple(here(), pos!!, "else"))
                inst(0x00, 0x00)
            }
            "end" -> {
                if (branches.isEmpty()) fail("This 'end' does not have a matching 'begin'.")
                val prev = branches.removeLast().first
                jump(prev, here())
            }
            "jump0" -> immediate(0xB0, wideValue())
            "jump" -> immediate(0x10, wideValue())
            "native" -> immediate(0x00, wideValue())
            "sprite" -> {
                val r1 = register()
                val r2 = register()
                val size = tinyValue()
                if (size == 0) schip = true
                inst(0xD0 or r1, (r2 shl 4) or size)
            }
            "loop" -> {
                loops.addLast(here() to pos!!)
                whiles.addLast(null)
            }
            "while" -> {
                if (loops.isEmpty()) fail("This 'while' is not within a loop.")
                conditional(true)
                whiles.addLast(here())
                immediate(0x10, 0)
            }
            "again" -> {
                if (loops.isEmpty()) fail("This 'again' does not have a matching 'loop'.")
                immediate(0x10, loops.removeLast().first)
                while (whiles.last() != null) {
                    val w = whiles.removeLast()
                    if (w != null) jump(w, here())
                }
                whiles.removeLastOrNull()
            }
            "plane" -> {
                val plane = tinyValue()
                if (plane > 3) fail("The plane bitmask must be [0,3], was $plane.")
                xo = true
                inst(0xF0 or plane, 0x01)
            }
            "audio" -> {
                xo = true; inst(0xF0, 0x02)
            }
            "scroll-down" -> { schip = true; inst(0x00, 0xC0 or tinyValue()) }
            "scroll-up" -> { xo = true; inst(0x00, 0xD0 or tinyValue()) }
            "scroll-right" -> { schip = true; inst(0x00, 0xFB) }
            "scroll-left" -> { schip = true; inst(0x00, 0xFC) }
            "exit" -> { schip = true; inst(0x00, 0xFD) }
            "lores" -> { schip = true; inst(0x00, 0xFE) }
            "hires" -> { schip = true; inst(0x00, 0xFF) }
            "saveflags" -> {
                val flags = register()
                schip = true; xo = flags > 7
                inst(0xF0 or flags, 0x75)
            }
            "loadflags" -> {
                val flags = register()
                schip = true; xo = flags > 7
                inst(0xF0 or flags, 0x85)
            }
            "i" -> iassign(next())
            is String -> {
                if (isRegister(token)) {
                    vassign(register(token))
                } else if (token == ":call") {
                    val addr = if (peek() == "{") parseCalculated("ANONYMOUS") else wideValue(next())
                    immediate(0x20, addr and 0xFFF)
                } else {
                    immediate(0x20, wideValue(token))
                }
            }
            else -> fail("Unknown token: $token")
        }
    }

    // Monitor format parser
    private fun parseMonitorLength(): MonitorLength {
        val pk = peek()
        // If next token is string containing '%', parse format; else parse tiny or veryWide as fixed number
        if (pk is String && pk.contains('%')) {
            val fmt = compileFormat(pk)
            next() // consume string
            return MonitorLength.Format(fmt)
        }
        // fixed length
        val num = if (pk is String || pk is Number) {
            // choose tiny for registers (nybble mask), or very wide for memory
            // we cannot know from here, but the JS allows tiny for register or wide for memory.
            // We'll accept numbers up to 0xFFFF
            val v = if (pk is Number) {
                next(); pk.toInt()
            } else {
                // may be parsed by veryWideValue(true)
                veryWideValue(noForward = true)
            }
            v
        } else {
            tinyValue()
        }
        return MonitorLength.Fixed(num)
    }

    private fun compileFormat(text: String): List<FormatPart> {
        /*
         * %[bytes][typecode]
         * c -> ascii chars
         * i -> signed integer (two's complement)
         * b -> binary
         * x -> hex (uppercase)
         */
        val r = mutableListOf<FormatPart>()
        var i = 0
        while (i < text.length) {
            if (text[i] == '%') {
                val m = "^([0-9]+)?([cibx])".toRegex().find(text.substring(i + 1))
                    ?: fail("Unknown format character %${text.getOrNull(i + 1)}- should be %Nc,%Ni,%Nb, or %Nx.")
                val lenStr = m.groupValues[1]
                val type = m.groupValues[2][0]
                if (lenStr == "0") fail("format byte count cannot be 0.")
                r.add(FormatPart(type = type, len = if (lenStr.isEmpty()) 1 else lenStr.toInt()))
                i += m.value.length + 1
            }
            var s = ""
            while (i < text.length && text[i] != '%') {
                s += text[i++]
            }
            if (s.isNotEmpty()) r.add(FormatPart(type = 't', len = 0, text = s))
        }
        return r
    }

    // Tokenizer
    private val escapeChars = mapOf(
        't' to '\t',
        'n' to '\n',
        'r' to '\r',
        'v' to '\u000b',
        '0' to '\u0000',
        '\\' to '\\',
        '"' to '\"',
    )

    private fun parseNumber(token: String): Int? {
        // 0b, 0x, decimal with optional +/- prefix
        if (Regex("^[+\\-]?0b[01]+$").matches(token)) {
            val isNeg = token.startsWith('-')
            val bitstring = if (isNeg || token.startsWith('+')) token.substring(3) else token.substring(2)
            val v = bitstring.toInt(2)
            return if (isNeg) -v else v
        }
        if (Regex("^[+\\-]?0x[0-9a-f]+$", RegexOption.IGNORE_CASE).matches(token)) {
            return token.toInt(16)
        }
        if (Regex("^[+\\-]?[0-9]+$").matches(token)) {
            return token.toInt(10)
        }
        return null
    }

    private fun tokenize(text: String): List<RawToken> {
        val ret = mutableListOf<RawToken>()
        var index = 0
        var token = ""
        var tokenStart = -1
        fun flush() {
            if (token.isNotEmpty()) {
                val num = parseNumber(token)
                ret.add(RawToken(num ?: token, tokenStart, index))
                token = ""
                tokenStart = -1
            }
        }
        while (index < text.length) {
            var c = text[index++]
            if (c == '#') {
                flush()
                while (c != '\n' && index < text.length) {
                    c = text[index++]
                }
            } else if (c == '"') {
                flush()
                var str = StringBuilder()
                tokenStart = index
                while (true) {
                    if (index >= text.length) {
                        pos = RawToken("<EOF>", index + 1, index + 2)
                        fail("Missing a closing \" in a string literal.")
                    }
                    c = text[index++]
                    if (c == '"') break
                    if (c == '\\') {
                        if (index >= text.length) {
                            pos = RawToken("<EOF>", index, index + 1)
                            fail("Missing a closing \" in a string literal.")
                        }
                        val esc = text[index++]
                        val mapped = escapeChars[esc] ?: run {
                            pos = RawToken(esc.toString(), index - 1, index)
                            fail("Unrecognized escape character '$esc' in a string literal.")
                        }
                        str.append(mapped)
                    } else str.append(c)
                }
                ret.add(RawToken(str.toString(), tokenStart, index + 1))
                tokenStart = -1
            } else if (" \t\n\r\u000b".contains(c)) {
                flush()
            } else {
                if (tokenStart == -1) tokenStart = index
                token += c
            }
        }
        flush()
        return ret
    }

    // Macro raw storage for token-aware expansion
    private val macroRawBodies = linkedMapOf<String, List<RawToken>>()

    // Error helper
    private fun fail(message: String): Nothing {
        throw AssemblerException(message)
    }
}

class AssemblerException(message: String) : RuntimeException(message)