package dev.secondsun.chip8.compose.editor.state

import kotlin.text.Charsets.UTF_8

data class CodeEditorState(val fileName : String, val fileType : FileType, val fileContents : ByteArray) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CodeEditorState

        if (fileName != other.fileName) return false
        if (fileType != other.fileType) return false
        if (!fileContents.contentEquals(other.fileContents)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = fileName.hashCode()
        result = 31 * result + fileType.hashCode()
        result = 31 * result + fileContents.contentHashCode()
        return result
    }

    fun contents() :String {
        when(fileType) {
            FileType.TEXT -> {
                if (fileContents.size > 3) {
                    if (fileContents[0] == 0xef.toByte() && fileContents[1] == 0xbb.toByte() && fileContents[2] == 0xbf.toByte()) {
                        return String(bytes = fileContents.drop(3).toByteArray(), UTF_8)

                    }
                }
                //convert to string
                val string = String(bytes = fileContents, UTF_8)
                return string
            }
            FileType.BINARY -> {
                    return (": main \n${fileContents.joinToString(" ") { String.format("0x%02X", it) }.split(" ").chunked(8).joinToString("\n") { it.joinToString(" ") }}")

            }
        }
    }
}

enum class FileType {
    TEXT, BINARY
}