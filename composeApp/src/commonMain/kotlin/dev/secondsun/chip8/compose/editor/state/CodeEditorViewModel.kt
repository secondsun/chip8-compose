package dev.secondsun.chip8.compose.editor.state

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.absolutePath
import io.github.vinceglb.filekit.dialogs.openFilePicker
import io.github.vinceglb.filekit.exists
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import kotlin.text.Charsets.UTF_8

class CodeEditorViewModel : ViewModel() {


    private var _editorState = MutableStateFlow(CodeEditorState("", FileType.TEXT, ByteArray(0)))
    val editorState = _editorState.asStateFlow()

    /**
     * Opens the file and returns the string contents. Will remove UTF-8 BOM marks
     *
     */
    fun openFile() {
        viewModelScope.launch {

            val file = FileKit.openFilePicker()

            withContext(Dispatchers.IO) {
                if (file != null && file.exists()) {
                    val tempFilePath = file.absolutePath()


                    val javaFile = File(tempFilePath)
                    //Start reading file
                    FileInputStream(javaFile).use { fis ->

                        //Remove UTF-8 Byte order marks (if present)
                        var byteArray = fis.readAllBytes()

                        val fileType = guessFileType(byteArray)
                        _editorState.value = CodeEditorState(String(byteArray, UTF_8), fileType, byteArray)

                    }
                }

            }
        }

    }

    /**
     * This is a heuristic method to detect if a file is a UTF-8 text file or a binary file.
     */
    private fun guessFileType(byteArray: ByteArray) :FileType {

        //Check for UTF-8 BOM
        if (byteArray.size > 3) {
            if (byteArray[0] == 0xef.toByte() && byteArray[1] == 0xbb.toByte() && byteArray[2] == 0xbf.toByte()) {
                return FileType.TEXT
            }
        }

        //We will check for non-printing characters and if they cross a critical threshold, return binary
        var nonPrintingCharacters = 0
        byteArray.forEach { byte ->
            if (byte < 32 || byte > 126) {
                nonPrintingCharacters++
            }
        }

        return if (nonPrintingCharacters > byteArray.size / 4) {
            FileType.BINARY
        } else {
            FileType.TEXT
        }

    }


}


