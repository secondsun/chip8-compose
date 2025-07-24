package dev.secondsun.chip8.compose.editor.state

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.dialogs.openFilePicker
import kotlinx.coroutines.launch

class CodeEditorViewModel : ViewModel() {
    fun openFile() {
        viewModelScope.launch {
            FileKit.openFilePicker()
        }
    }

}
