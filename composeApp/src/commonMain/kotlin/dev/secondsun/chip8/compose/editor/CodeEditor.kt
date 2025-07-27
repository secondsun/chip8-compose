package dev.secondsun.chip8.compose.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import com.multiplatform.webview.web.WebView
import com.multiplatform.webview.web.rememberWebViewNavigator
import com.multiplatform.webview.web.rememberWebViewState
import dev.datlag.kcef.KCEF
import dev.secondsun.chip8.compose.editor.state.CodeEditorViewModel
import jthemedetecor.OsThemeDetector
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.math.max


/**
 *
 * CodeEditor is a webview that runs Monaco, the VS Code code engine. It connects to an embedded http server and
 * loads the monaco instance there.
 *
 * @param port This is the port on the local machine that the kcef browser will connect to.
 *
 * Design note: Monaco uses workers which will only run if they are loaded from an http server. This is why we embed
 * instead of using a file:// resource.
 */
@Composable
fun CodeEditor(port: Int) {
    var restartRequired by remember { mutableStateOf(false) }
    var downloading by remember { mutableStateOf(0F) }
    var initialized by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.Default) {
            KCEF.init(builder = {
                installDir(File("kcef-bundle"))
                progress {
                    onDownloading {
                        downloading = max(it, 0F)
                    }
                    onInitialized {
                        initialized = true
                    }
                }
                settings {
                    cachePath = File("cache").absolutePath
                }
            }, onError = {
                it!!.printStackTrace()
            }, onRestartRequired = {
                restartRequired = true
            })
        }
    }

    val detector: OsThemeDetector by remember { mutableStateOf(OsThemeDetector.detector) }
    var isDarkMode by remember { mutableStateOf(detector.isDark) }
    var primaryColor by remember { mutableStateOf(detector.primaryColor) }
    var scheme: DynamicScheme by remember(key1 = { (if (isDarkMode) 0 else 1) * 3 + primaryColor.rgb }) {
        mutableStateOf(SchemeTonalSpot(Hct.fromInt(primaryColor.rgb), isDarkMode, 0.0))
    }


    if (restartRequired) {
        Text(text = "Restart required.")
    } else {
        if (initialized) {
            MonacoView(viewModel = CodeEditorViewModel(), url = "http://localhost:$port/index.html")
        } else {
            Text(text = "Downloading $downloading%")
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            KCEF.disposeBlocking()
        }
    }
}

@Composable
fun MonacoView(url: String, viewModel: CodeEditorViewModel = viewModel { CodeEditorViewModel() }) {

    val webViewState =
        rememberWebViewState(url)
    val webViewNavigator = rememberWebViewNavigator()

    System.out.println("Recomposing")
    webViewState.webSettings.apply {
        isJavaScriptEnabled = true
        customUserAgentString =
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 11_1) AppleWebKit/625.20 (KHTML, like Gecko) Version/14.3.43 Safari/625.20"
        androidWebSettings.apply {
            isAlgorithmicDarkeningAllowed = true
            safeBrowsingEnabled = true
        }
    }

    @Composable
    fun ControlRow(modifier: Modifier = Modifier) {

        var debugMenuOpened by remember { mutableStateOf(false) }
        Box() {
            Row(modifier) {
                IconButton(onClick = { debugMenuOpened = !debugMenuOpened }) {
                    Icon(
                        Icons.Default.Settings,
                        contentDescription = "Open Debug Menu"
                    )
                }

                IconButton(onClick = {
                    viewModel.openFile { contents ->
                        webViewNavigator.evaluateJavaScript("updateText(`${contents.replace("`", "\\`")}`, \"octo\")")
                    }
                }) {
                    Icon(
                        Icons.Default.FileOpen,
                        contentDescription = "Open File"
                    )
                }

            }
            if (debugMenuOpened) {
                Row(modifier.background(Color.Black)) {

                    IconButton(onClick = {debugMenuOpened = false}) {
                        Icon(Icons.Default.Close, "Close")
                    }

                    Button(onClick = { webViewState.nativeWebView.reload(); debugMenuOpened = false }) {
                        Text("Reload")
                    }
                }

            }
        }
    }

    Column(Modifier.fillMaxSize().background(Color(0xff272822))) {
        ControlRow(modifier = Modifier.fillMaxWidth().wrapContentHeight())
        WebView(
            state = webViewState,
            navigator = webViewNavigator,
            modifier = Modifier.fillMaxSize(),
        )
    }


}
