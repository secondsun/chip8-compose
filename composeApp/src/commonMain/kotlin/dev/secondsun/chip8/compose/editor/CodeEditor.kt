package dev.secondsun.chip8.compose.editor

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.multiplatform.webview.web.WebView
import com.multiplatform.webview.web.rememberWebViewNavigator
import com.multiplatform.webview.web.rememberWebViewState
import dev.datlag.kcef.KCEF
import dev.secondsun.chip8.compose.editor.state.CodeEditorViewModel
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.dialogs.openFilePicker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.math.max


/**
 *
 * CodeEditor is a webview that runs Monaco, the CS Code code engine. It connects to an embedded http server and
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
//
//    LaunchedEffect(key1 = viewModel.file.value) {
//        if (!viewModel.file.value.isEmpty()) {
//            val bytes = File(viewModel.file.value).readText(Charsets.UTF_8).replace("\"", "\\\"")
//
//            System.out.println("loading ${bytes}")
//        }
//    }
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
    Column(Modifier.fillMaxSize()) {
        Button(onClick = { webViewState.nativeWebView.reload() }) { Text("Reload") }
        Button(onClick = { viewModel.openFile() { contents ->
            print(contents)
            webViewNavigator.evaluateJavaScript("updateText(\"$contents\", \"json\")") { out ->
                print("Result $out")
            }
        } }) { Text("Open File") }
        val text =
            webViewState.let {
                "${it.pageTitle ?: ""} ${it.loadingState} ${it.lastLoadedUrl ?: ""}"
            }
        Text(text)
        WebView(
            state = webViewState,
            navigator = webViewNavigator,
            modifier = Modifier.fillMaxSize(),
        )
    }
}
