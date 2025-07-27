package dev.secondsun.chip8.compose

import androidx.compose.material.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import chip8_compose.composeapp.generated.resources.Res
import com.google.dynamiccolor.DynamicScheme
import com.google.hct.Hct
import com.google.scheme.SchemeTonalSpot
import com.sun.net.httpserver.SimpleFileServer
import dev.secondsun.chip8.compose.editor.CodeEditor
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.createDirectories
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.filesDir
import jthemedetecor.OsThemeDetector
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.skia.skottie.Logger
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileOutputStream
import java.net.InetSocketAddress
import java.nio.file.Path
import java.util.zip.ZipFile
import java.util.zip.ZipInputStream
import kotlin.coroutines.CoroutineContext

fun main() =
    application {
        FileKit.init("Chip8-Compose")

        val httpPort = mutableIntStateOf(0)

        fun startServer() {

            val scope = CoroutineScope(Dispatchers.IO + Job())

            // Launch a coroutine within the scope
            scope.launch {
                val bytes = Res.readBytes("files/monaco.zip")
                System.out.println("Unzipping")
                val tempDirectory = File(FileKit.filesDir.file, "monaco")
                if (!tempDirectory.exists()) {
                    tempDirectory.mkdirs()
                }

                ZipInputStream(ByteArrayInputStream(bytes)).use { zipInputStream ->
                    var entry = zipInputStream.nextEntry
                    while (entry != null) {

                        if (entry.isDirectory) {
                            val directory = File(tempDirectory, entry.name)
                            if(!directory.exists()) {
                                directory.mkdirs()
                            }
                        } else {
                            val file = File(tempDirectory, entry.name)
                            FileOutputStream(file).use {
                                it.write(zipInputStream.readAllBytes())
                            }
                        }
                        entry = zipInputStream.nextEntry
                    }

                }
                System.out.println("Unzipped to ${tempDirectory.path} ")
                val address = InetSocketAddress(0)
                val path = Path.of(tempDirectory.path)

                val server = SimpleFileServer.createFileServer(address, path, SimpleFileServer.OutputLevel.VERBOSE)
                server.start()

                httpPort.value = server.address.port
            }

        }

        startServer()


        val detector: OsThemeDetector by remember { mutableStateOf(OsThemeDetector.detector) }
        var isDarkMode by remember { mutableStateOf(detector.isDark) }
        var primaryColor by remember { mutableStateOf(detector.primaryColor) }
        var scheme: DynamicScheme by remember(key1 = { (if (isDarkMode) 0 else 1) * 3 + primaryColor.rgb }) {
            mutableStateOf(SchemeTonalSpot(Hct.fromInt(primaryColor.rgb), isDarkMode, 0.0))
        }


        Window(
            onCloseRequest = ::exitApplication,
            title = "Chip8-Compoze",
        ) {
            when (httpPort.value) {
                0 -> Text("Waiting")
                else -> CodeEditor(httpPort.value)
            }

        }
    }