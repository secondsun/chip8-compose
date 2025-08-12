package dev.secondsun.chip8.compose

import androidx.compose.material.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import chip8_compose.composeapp.generated.resources.Res
import com.google.dynamiccolor.DynamicScheme
import com.google.hct.Hct
import com.google.scheme.SchemeTonalSpot
import com.sun.net.httpserver.SimpleFileServer
import dev.datlag.kcef.KCEF
import dev.secondsun.chip8.compose.editor.NachosMain
import dev.secondsun.chip8.compose.localproviders.DarkModeState
import dev.secondsun.chip8.compose.localproviders.KCEFState
import dev.secondsun.chip8.compose.localproviders.LocalDarkMode
import dev.secondsun.chip8.compose.localproviders.LocalKCEF
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.filesDir
import jthemedetecor.OsThemeDetector
import jthemedetecor.consumers.DarkModeConsumer
import jthemedetecor.consumers.PrimaryColorConsumer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileOutputStream
import java.net.InetSocketAddress
import java.nio.file.Path
import java.util.zip.ZipInputStream
import kotlin.math.max

fun main() {
    FileKit.init("Chip8-Compose")

    var httpPort = MutableStateFlow(0)

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

    application {

        val scope = rememberCoroutineScope()

        val detector: OsThemeDetector by remember { mutableStateOf(OsThemeDetector.detector) }
        var isDarkMode by remember { mutableStateOf(detector.isDark) }
        var primaryColor by remember { mutableStateOf(detector.primaryColor) }
        var scheme: DynamicScheme by remember(key1 = { (if (isDarkMode) 0 else 1) * 3 + primaryColor.rgb }) {
            mutableStateOf(SchemeTonalSpot(Hct.fromInt(primaryColor.rgb), isDarkMode, 0.0))
        }


        // Monitor theme changes
        detector.registerListener(scope, DarkModeConsumer({ isDark ->
            if (isDark != isDarkMode) {
                isDarkMode = isDark
                scheme = SchemeTonalSpot(Hct.fromInt(primaryColor.rgb), isDarkMode, 0.0)
            }
        }))

        detector.registerListener(scope, PrimaryColorConsumer({ color ->
            if (primaryColor != color) {
                primaryColor = color
                scheme = SchemeTonalSpot(Hct.fromInt(primaryColor.rgb), isDarkMode, 0.0)
            }
        }))


        var restartRequired by remember { mutableStateOf(false) }
        var downloading by remember { mutableStateOf(0F) }
        var initialized by remember { mutableStateOf(false) }



        val kcefState = KCEFState(restartRequired, downloading, initialized)
        val darkModeState = DarkModeState(isDarkMode)


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


        Window(
            onCloseRequest = ::exitApplication,
            title = "Nachos",
        ) {
            when (httpPort.value) {
                0 -> Text("Waiting")
                else ->
                    CompositionLocalProvider(LocalKCEF provides kcefState) {
                        CompositionLocalProvider(LocalDarkMode provides darkModeState) {
                            println("Recomposed CompositionLocalProvider")
                            NachosMain(scheme = scheme, port = httpPort.value)
                        }
                    }
            }

        }


        DisposableEffect(Unit) {
            onDispose {
                KCEF.disposeBlocking()
            }
        }
    }
}