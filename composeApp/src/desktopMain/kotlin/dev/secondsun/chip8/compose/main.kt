package dev.secondsun.chip8.compose

import androidx.compose.material.Text
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.sun.net.httpserver.SimpleFileServer
import dev.secondsun.chip8.compose.editor.CodeEditor
import java.net.InetSocketAddress
import java.nio.file.Path

fun main() =
    application {

        val httpPort = mutableIntStateOf(0)

        fun startServer() {
            val EDITOR_HTML_RESOURCE_LOCATION = "D:\\Projects\\Chip8-Compose\\monaco\\";

            val address = InetSocketAddress(0)
            val path = Path.of(EDITOR_HTML_RESOURCE_LOCATION)
            val server = SimpleFileServer.createFileServer(address, path, SimpleFileServer.OutputLevel.VERBOSE)
            server.start()
            httpPort.value = server.address.port
        }

        startServer()

        Window(
            onCloseRequest = ::exitApplication,
            title = "Chip8-Compoze",
        ) {
            when(httpPort.value) {
                0 -> Text("Waiting")
                else -> CodeEditor(httpPort.value)
            }

        }
}