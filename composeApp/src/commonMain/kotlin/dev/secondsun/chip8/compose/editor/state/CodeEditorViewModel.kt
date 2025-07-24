package dev.secondsun.chip8.compose.editor.state

import androidx.compose.runtime.Composable
import androidx.compose.runtime.InternalComposeApi
import androidx.compose.runtime.currentComposer
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.absoluteFile
import io.github.vinceglb.filekit.absolutePath
import io.github.vinceglb.filekit.dialogs.openFilePicker
import io.github.vinceglb.filekit.path
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.coroutines.CoroutineContext
import kotlin.text.Charsets.UTF_8

class CodeEditorViewModel : ViewModel() {

    private var _file = mutableStateOf("")
    val file = _file
    fun openFile(block: (String) -> Unit = {}) {
        viewModelScope.launch {
            val file = FileKit.openFilePicker()
            if (file != null) {
                _file.value = file.absolutePath()
            }
            if (!_file.value.isEmpty()) {
                val string = File(_file.value).readText(UTF_8).replace("\"","\\\"").replace("\n","\\n")
                block(string)
            }
        }

    }
}


