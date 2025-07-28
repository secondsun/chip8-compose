package dev.secondsun.chip8.compose.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.dynamiccolor.DynamicScheme
import com.multiplatform.webview.web.WebView
import com.multiplatform.webview.web.rememberWebViewNavigator
import com.multiplatform.webview.web.rememberWebViewState
import dev.secondsun.chip8.compose.editor.state.CodeEditorViewModel
import dev.secondsun.chip8.compose.localproviders.LocalDarkMode
import dev.secondsun.chip8.compose.localproviders.LocalKCEF


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
fun CodeEditor(port: Int, scheme: DynamicScheme) {

    MaterialTheme(colorScheme = scheme.toMaterialScheme()) {
        Scaffold(modifier = Modifier.fillMaxSize()) {
            val kcefState = LocalKCEF.current

            if (kcefState.restartRequired) {
                Text(text = "Restart required.")
            } else {
                if (kcefState.initialized) {
                    MonacoView(viewModel = CodeEditorViewModel(), url = "http://localhost:$port/index.html?darkMode=${LocalDarkMode.current.isDarkMode}")
                } else {
                    Text(text = "Downloading $kcefState.downloading%")
                }
            }
        }
    }

}

private fun DynamicScheme.toMaterialScheme() : ColorScheme {
    val scheme = this
    return ColorScheme(
        primary = Color(primary),
        onPrimary = Color(scheme.onPrimary),
        primaryContainer = Color(scheme.primaryContainer),
        onPrimaryContainer = Color(scheme.onPrimaryContainer),
        inversePrimary = Color(scheme.inversePrimary),
        secondary = Color(scheme.secondary),
        onSecondary = Color(scheme.onSecondary),
        secondaryContainer = Color(scheme.secondaryContainer),
        onSecondaryContainer = Color(scheme.onSecondaryContainer),
        tertiary = Color(scheme.tertiary),
        onTertiary = Color(scheme.onTertiary),
        tertiaryContainer = Color(scheme.tertiaryContainer),
        onTertiaryContainer = Color(scheme.onTertiaryContainer),
        background = Color(scheme.background),
        onBackground = Color(scheme.onBackground),
        surface = Color(scheme.surface),
        onSurface = Color(scheme.onSurface),
        surfaceVariant = Color(scheme.surfaceVariant),
        onSurfaceVariant = Color(scheme.onSurfaceVariant),
        surfaceTint = Color(scheme.surfaceTint),
        inverseSurface = Color(scheme.inverseSurface),
        inverseOnSurface = Color(scheme.inverseOnSurface),
        error = Color(scheme.error),
        onError = Color(scheme.onError),
        errorContainer = Color(scheme.errorContainer),
        onErrorContainer = Color(scheme.onErrorContainer),
        outline = Color(scheme.outline),
        outlineVariant = Color(scheme.outlineVariant),
        scrim = Color(scheme.scrim),
        surfaceBright = Color(scheme.surfaceBright),
        surfaceDim = Color(scheme.surfaceDim),
        surfaceContainer = Color(scheme.surfaceContainer),
        surfaceContainerHigh = Color(scheme.surfaceContainerHigh),
        surfaceContainerHighest = Color(scheme.surfaceContainerHighest),
        surfaceContainerLow = Color(scheme.surfaceContainerLow),
        surfaceContainerLowest = Color(scheme.surfaceContainerLowest)
    )
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

    val darkMode = LocalDarkMode.current.isDarkMode

    LaunchedEffect(LocalDarkMode.current.isDarkMode) {
        println("Setting dark mode to $darkMode")
    webViewNavigator.evaluateJavaScript("setDarkMode(${darkMode})")
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

    Column(Modifier.fillMaxSize()) {
        ControlRow(modifier = Modifier.fillMaxWidth().wrapContentHeight())
        WebView(
            state = webViewState,
            navigator = webViewNavigator,
            modifier = Modifier.fillMaxSize(),
        )
    }


}
