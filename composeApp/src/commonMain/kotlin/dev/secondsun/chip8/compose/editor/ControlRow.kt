package dev.secondsun.chip8.compose.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import chip8_compose.composeapp.generated.resources.Res
import chip8_compose.composeapp.generated.resources.logo
import com.multiplatform.webview.web.WebViewState
import org.jetbrains.compose.resources.painterResource

@Composable
fun ControlRow(
    modifier: Modifier = Modifier,
    webViewState: WebViewState,
    openFileOnclick: () -> Unit = {print("Open File Pressed")},
    runOnClick: () -> Unit = {print("Run File Pressed")},
) {

    var debugMenuOpened by remember { mutableStateOf(false) }
    Box() {
        Row(modifier) {

            IconButton(onClick = { print("TODO : Open Website") }) {
                Icon(
                    painterResource(Res.drawable.logo),
                    tint = Color.Unspecified,
                    contentDescription = "Chip8 Compose"
                )
            }

            IconButton(onClick = openFileOnclick) {
                Icon(
                    Icons.Default.FolderOpen,
                    contentDescription = "Open File"
                )
            }

            IconButton(onClick = runOnClick) {
                Icon(
                    Icons.Default.PlayArrow,
                    contentDescription = "Run"
                )
            }

            IconButton(
                onClick = { debugMenuOpened = !debugMenuOpened },
                modifier = Modifier.weight(1f).wrapContentWidth(Alignment.End)
            ) {
                Icon(
                    Icons.Default.BugReport,
                    contentDescription = "Open Debug Menu"
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

