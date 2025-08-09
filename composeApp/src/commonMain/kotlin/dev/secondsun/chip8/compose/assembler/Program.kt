package dev.secondsun.chip8.compose.assembler

class Program() {

    val errors = mutableListOf<Error>()
    val labels = mutableMapOf<String, Label>()

    fun addError(error: Error) : Program{
        errors.add(error)
        return this
    }

    fun addLabel(label: Label) : Program{
        labels[label.name] = label
        return this
    }

    fun hasErrors(): Boolean {
        return errors.isNotEmpty()
    }

}
