package dev.secondsun.chip8.compose.editor.state

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.absolutePath
import io.github.vinceglb.filekit.dialogs.openFilePicker
import io.github.vinceglb.filekit.exists
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import kotlin.text.Charsets.UTF_8

class CodeEditorViewModel : ViewModel() {


    private var _file = mutableStateOf("")
    val file = _file

    /**
     * Opens the file and returns the string contents. Will remove UTF-8 BOM marks
     *
     */
    fun openFile(block: (String) -> Unit = {}) {
        viewModelScope.launch {

            val file = FileKit.openFilePicker()

            withContext(Dispatchers.IO) {
                if (file != null && file.exists()) {
                    _file.value = file.absolutePath()


                    val javaFile = File(_file.value)
                    //Start reading file
                    FileInputStream(javaFile).use { fis ->

                        //Remove UTF-8 Byte order marks (if present)
                        var byteArray = fis.readAllBytes()
                        if (byteArray.size > 3) {
                            if (byteArray[0] == 0xef.toByte() && byteArray[1] == 0xbb.toByte() && byteArray[2] == 0xbf.toByte()) {
                                byteArray = byteArray.drop(3).toByteArray()
                            }
                        }

                        //convert to string and escape backticks
                        val string = String(bytes = byteArray, UTF_8)

                        withContext(Dispatchers.Main) {
                            block(string)
                        }

                    }
                }

            }
        }

    }


}


