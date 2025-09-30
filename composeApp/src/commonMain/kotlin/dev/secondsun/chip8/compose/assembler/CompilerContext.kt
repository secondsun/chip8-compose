package dev.secondsun.chip8.compose.assembler

class CompilerContext(val parsedProgram: ParserOutput, val source: String) {
    var dict = mutableMapOf<String,Int>()//LabelToAddress
    var aliases = mutableMapOf<String, Int>()//Registers
    var rom = mutableMapOf<Int, Int?>()//Addr, byteValue
    var dbginfo = DebugInfo(source)
    var hereaddr = 0x200
    var hasMain = true
    var schip = false
    var xo = false
    var pos: ParsedToken = parsedProgram.parsedTokens[0]



    fun compile() {
        aliases["unpack-hi"] = 0x0
        aliases["unpack-lo"] = 0x1
        //Reserve Jump Slot
        inst(0,0)
        parsedProgram.parsedTokens.forEach { parsedToken ->
            this.pos = parsedToken
            if (parsedToken.type == ParsedTokenType.Number) {
                var nn = (parsedToken.tokens[0] as Token.Number).value
                if (nn < -128 || nn > 255) throw IllegalStateException("Literal value '${nn}' does not fit in a byte- must be in range [-128,255].")
                this.data(nn);
            } else {
                instruction(parsedToken);
            }
        }
        TODO("See below, implement rest of compiler")
        /*

	if (this.hasmain == true) {
		if (!('main' in this.constants) && !('main' in this.dict)) {
			this.pos=['<EOF>',this.source.length+1,this.source.length+2];
			throw "This program is missing a 'main' label."
		}
		this.jump(0x200, this.wideValue("main"));
	}
	var keys=Object.keys(this.protos);
	if (keys.length > 0) {
		this.pos = this.protos[keys[0]].proto_pos;
		throw `Undefined forward reference: ${keys[0]}`;
	}
	if (this.loops.length > 0) {
		this.pos = this.loops.pop()[1];
		throw "This 'loop' does not have a matching 'again'.";
	}
	if (this.branches.length > 0) {
		var flow=this.branches.pop()
		this.pos = flow[1];
		throw `This '${flow[2]}' does not have a matching 'end'.`;
	}
	for(var index = 0; index < this.rom.length; index++) {
		if (typeof this.rom[index] == "undefined") { this.rom[index] = 0x00; }
	}*/
    }

    private fun instruction(parsedToken: ParsedToken) {

        var type = parsedToken.type
        if (type == ParsedTokenType.Label) {
            resolveLabel(0)
        } else if (type == ParsedTokenType.Next) {
            resolveLabel(1)
        } else if (type == ParsedTokenType.Unpack) {
            unpack();
        }

        TODO ("Rest of compiler")
    }

    private fun unpack() {
        val peek = pos.tokens[1]
        var value = 0
        if (peek is Token.Identifier && peek.name == "long") {
            value = veryWideValue(pos.tokens[2], false, true)
        } else {

        }
    }

    private fun veryWideValue(token: Token, noForward: Boolean, noOffset: Boolean): Int {
        val nnnn: Token = token
        val target = this.here() + (if (noOffset) 0 else 2)
        if (nnnn is Token.Number) {
            val value = nnnn.value
            if (value < -32768 || value > 65535) {
                throw IllegalStateException("Literal value '${value}' does not fit in a word- must be in range [-32768,65535].")
            }
            this.data(value)
            return target
        }
        if (noForward && nnnn is Token.Identifier) {
            val label = nnnn.name
        }
        TODO("Above was AI generated, consider adding compilation steps to parser")
    }

    private fun resolveLabel(offset: Int) {
        var target = here() + offset
        val label = (pos.tokens[1] as Token.Identifier).name
        if ((target == 0x202 || target == 0x200) && (label == "main")) {
            this.hasMain = false;
            this.hereaddr = 0x200;
            this.rom[0] = null; // erase reserved jump
            this.rom[1] = null;
            target = this.here();
        }
        this.dict[label] = target;
    }

    private fun here(): Int {
        return hereaddr
    }

    private fun inst(a: Int, b: Int) {
        this.data(a)
        this.data(b)

    }

    private fun data(a: Int) {
        if (rom[hereaddr] != null && hereaddr >= 0x200) {
            throw IllegalStateException("Data overlap. Address 0x${this.hereaddr.toString(16).uppercase()} has already been defined.")
        }
        this.rom[this.hereaddr - 0x200] = (a and 0xFF)
        this.dbginfo.mapAddr(this.hereaddr, this.pos.tokens[0].line)
        this.hereaddr++
    }

}
