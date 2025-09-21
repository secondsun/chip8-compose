package dev.secondsun.chip8.compose.editor


import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewModelScope
import com.google.dynamiccolor.DynamicScheme
import com.multiplatform.webview.jsbridge.WebViewJsBridge
import com.multiplatform.webview.util.KLogSeverity
import com.multiplatform.webview.web.WebView
import com.multiplatform.webview.web.rememberWebViewNavigator
import com.multiplatform.webview.web.rememberWebViewState
import dev.secondsun.chip8.compose.editor.state.CodeEditorViewModel
import dev.secondsun.chip8.compose.editor.state.FileType
import dev.secondsun.chip8.compose.editor.webview.MonacoContentChangedMessageHandler
import dev.secondsun.chip8.compose.editor.webview.MonacoInitMessageHandler
import dev.secondsun.chip8.compose.emulator.Chip8EmulatorCanvas
import dev.secondsun.chip8.compose.emulator.state.Chip8EmulatorViewModel
import dev.secondsun.chip8.compose.localproviders.LocalDarkMode
import dev.secondsun.chip8.compose.localproviders.LocalKCEF
import dev.secondsun.chip8.compose.theme.toMaterialScheme
import dev.secondsun.chip8.util.Chip8Utils
import kotlinx.coroutines.launch


/**
 *
 * NachosMain is responsible for setup of the application. This includes theming, initializing the embedded browser
 * for Monaco, and passing this information to NachosAppContent.
 *
 * @param port This is the port on the local machine that the kcef browser will connect to.
 *
 * Design note: Monaco uses workers which will only run if they are loaded from an http server. This is why we embed
 * instead of using a file:// resource.
 */

@Suppress("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun NachosMain(port: Int, scheme: DynamicScheme) {
    MaterialTheme(colorScheme = scheme.toMaterialScheme()) {
        Scaffold(modifier = Modifier.fillMaxSize()) {

            val kcefState = LocalKCEF.current
            val isDarkMode = LocalDarkMode.current.isDarkMode

            if (kcefState.restartRequired) {
                Text(text = "Restart required.")
            } else {
                if (kcefState.initialized) {
                    // Use remember to memoize the MonacoView
                    val baseUrl = remember { "http://localhost:$port/index.html" }
                    key(baseUrl) {
                        NachosAppContent(
                            baseUrl = baseUrl,
                            isDarkMode = isDarkMode,
                            viewModel = remember { CodeEditorViewModel() }
                        )
                    }
                } else {
                    Text(text = "Downloading $kcefState.downloading%")
                }
            }
        }
    }
}




@Composable
private fun NachosAppContent(
    baseUrl: String,
    isDarkMode: Boolean,
    viewModel: CodeEditorViewModel
) {

    //State for the editor
    val editorState by viewModel.editorState.collectAsState()

    // Derive emulator state from editor state
    val emulatorState by remember {
        derivedStateOf {
            if (editorState.fileType == FileType.BINARY) {
                val chip8 = Chip8Utils.createFromRom(editorState.fileContents)
                Chip8EmulatorViewModel(chip8, 30)
            } else {
                null
            }
        }
    }

    var showEmulator by remember { mutableStateOf(false) }
    var chip8EmulatorViewModel by remember { mutableStateOf<Chip8EmulatorViewModel?>(null) }



    // BEGIN webview/monaco setup

    val webViewState = rememberWebViewState(baseUrl)
    val webViewNavigator = rememberWebViewNavigator()
    webViewState.webSettings.apply {
        logSeverity = KLogSeverity.Error
    }

    val jsBridge = WebViewJsBridge(webViewNavigator, "nachosBridge")

        jsBridge.register(
            MonacoInitMessageHandler(
                provideFileText = { editorState.contents() },
                provideFileType = { editorState.fileType },
                provideTheme = { isDarkMode })
        )

        jsBridge.register(
            MonacoContentChangedMessageHandler()
        )

    // Update dark mode when it changes
    LaunchedEffect(isDarkMode) {
        webViewNavigator.evaluateJavaScript("setDarkMode($isDarkMode)")
    }


    //END webview/monaco setup

    // Handle editor state changes
    //TODO: Move this to the view model somehow and possible combine the editor and emulator VMs
    LaunchedEffect(editorState) {
        val readOnly = when (editorState.fileType) {
            FileType.TEXT -> "false"
            FileType.BINARY -> "true"
        }

        webViewNavigator.evaluateJavaScript(
            "updateText(`${
                editorState.contents().replace("`", "\\`")
            }`, $readOnly)"
        )

        // Reset emulator when file changes
        showEmulator = false
        chip8EmulatorViewModel?.stop()
        chip8EmulatorViewModel = null
    }




    Column(Modifier.fillMaxSize()) {
        ControlRow(
            modifier = Modifier.fillMaxWidth().wrapContentHeight(),
            webViewState = webViewState,
            openFileOnclick = {
                viewModel.openFile()
            },
            runOnClick = {
                if (editorState.fileType == FileType.BINARY && emulatorState != null) {
                    showEmulator = true
                    chip8EmulatorViewModel = emulatorState
                    chip8EmulatorViewModel?.viewModelScope?.launch {
                        chip8EmulatorViewModel?.launch()
                    }
                }
            }

        )
        if (showEmulator && chip8EmulatorViewModel != null) {
            Chip8EmulatorCanvas(modifier = Modifier.fillMaxSize(), chip8EmulatorViewModel!!)
        } else {
            WebView(
                state = webViewState,
                navigator = webViewNavigator,
                modifier = Modifier.fillMaxSize(),
                webViewJsBridge = jsBridge
            )

        }
    }
}
