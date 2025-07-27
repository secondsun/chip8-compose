/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package jthemedetecor

import jthemedetecor.consumers.DarkModeConsumer
import jthemedetecor.consumers.PrimaryColorConsumer
import jthemedetecor.consumers.ThemingConsumer
import jthemedetecor.util.ConcurrentHashSet
import com.sun.jna.platform.win32.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.awt.Color
import java.util.*
import kotlin.concurrent.Volatile

/**
 * Determines the dark/light theme by the windows registry values through JNA.
 * Works on a Windows 10 system.
 *
 * @author Daniel Gyorffy
 * @author airsquared
 */
internal class WindowsThemeDetector : OsThemeDetector() {
    private val listeners: MutableSet<ThemingConsumer<*>> = ConcurrentHashSet()

    @Volatile
    private var detectorThread: DetectorThread? = null

    override val isDark: Boolean
        get() = Advapi32Util.registryValueExists(
            WinReg.HKEY_CURRENT_USER,
            DARK_MODE_REGISTRY_PATH,
            DARK_MODE_REGISTRY_VALUE
        ) &&
                Advapi32Util.registryGetIntValue(
                    WinReg.HKEY_CURRENT_USER,
                    DARK_MODE_REGISTRY_PATH,
                    DARK_MODE_REGISTRY_VALUE
                ) == 0

    @OptIn(ExperimentalStdlibApi::class)
    override val primaryColor: Color
        get() = if (Advapi32Util.registryValueExists(
                WinReg.HKEY_CURRENT_USER,
                ACCENT_COLOR_REGISTRY_PATH,
                ACCENT_COLOR_REGISTRY_VALUE
            )
        ) {
            val colorABGR = Advapi32Util.registryGetIntValue(
                WinReg.HKEY_CURRENT_USER,
                ACCENT_COLOR_REGISTRY_PATH,
                ACCENT_COLOR_REGISTRY_VALUE
            )

            val colorARGB = (colorABGR and 0xff000000.toInt()) or
                            ((colorABGR and 0x000000ff) shl 16) or
                            ((colorABGR and 0x0000ff00)) or
                            ((colorABGR and 0x00ff0000) shr 16)

             Color(colorARGB)
        } else {
             Color(0x673AB7)
        }

    @Synchronized
    override fun registerListener(scope: CoroutineScope, darkThemeListener: ThemingConsumer<*>) {
        Objects.requireNonNull(darkThemeListener)
        val listenerAdded = listeners.add(darkThemeListener)
        val singleListener = listenerAdded && listeners.size == 1
        val currentDetectorThread = detectorThread
        val threadInterrupted =
            (currentDetectorThread != null) && (currentDetectorThread as DetectorThread).isInterrupted

        if (singleListener || threadInterrupted) {
            val newDetectorThread = DetectorThread(this)
            this.detectorThread = newDetectorThread
                scope.async {
                    newDetectorThread.run()
                }

        }
    }

    @Synchronized
    override fun removeListener(darkThemeListener: ThemingConsumer<*>?) {
        listeners.remove(darkThemeListener)
        if (listeners.isEmpty()) {
            detectorThread!!.isInterrupted = true
            this.detectorThread = null
        }
    }


    /**
     * Thread implementation for detecting the theme changes
     */
    private class  DetectorThread(private val themeDetector: WindowsThemeDetector) {

        private val job = SupervisorJob()
        private val coroutineScope = CoroutineScope(job + Dispatchers.IO)
        var isInterrupted = false;

        private var lastDarkModeValue: Boolean
        private var lastColorValue: Color

        init {
            this.lastDarkModeValue = themeDetector.isDark
            this.lastColorValue = themeDetector.primaryColor
        }

        suspend fun run() {

            val colorHkey = WinReg.HKEYByReference()

            val darkModeHkey = WinReg.HKEYByReference()

            var darkModeErr = Advapi32.INSTANCE.RegOpenKeyEx(
                WinReg.HKEY_CURRENT_USER,
                DARK_MODE_REGISTRY_PATH,
                0,
                WinNT.KEY_READ,
                darkModeHkey
            )
            var colorErr = Advapi32.INSTANCE.RegOpenKeyEx(
                WinReg.HKEY_CURRENT_USER,
                ACCENT_COLOR_REGISTRY_PATH,
                0,
                WinNT.KEY_READ,
                colorHkey
            )
            if (darkModeErr != W32Errors.ERROR_SUCCESS) {
                throw Win32Exception(darkModeErr)
            } else if (colorErr != W32Errors.ERROR_SUCCESS) {
                throw Win32Exception(colorErr)
            }


                coroutineScope.async {
                    while (!this@DetectorThread.isInterrupted) {
                        val darkModeErrFlow = flow<Int> {
                            while (!this@DetectorThread.isInterrupted) {
                                val toReturn = Advapi32.INSTANCE.RegNotifyChangeKeyValue(
                                    darkModeHkey.value,
                                    false,
                                    WinNT.REG_NOTIFY_CHANGE_LAST_SET,
                                    null,
                                    false
                                )
                                emit(toReturn)
                            }
                        }

                        val colorErrFlow =flow<Int> {
                            while (!this@DetectorThread.isInterrupted) {
                                val toReturn = Advapi32.INSTANCE.RegNotifyChangeKeyValue(
                                    colorHkey.value,
                                    false,
                                    WinNT.REG_NOTIFY_CHANGE_LAST_SET,
                                    null,
                                    false
                                )
                                emit(toReturn)
                            }
                        }

                        merge(darkModeErrFlow, colorErrFlow).onEach { keyChangeValue ->
                            if (keyChangeValue != W32Errors.ERROR_SUCCESS) {
                                throw Win32Exception(keyChangeValue)
                            }

                        }



                        val currentDarkModeDetection = themeDetector.isDark
                        val currentColorDetection = themeDetector.primaryColor

                        if (currentDarkModeDetection != this@DetectorThread.lastDarkModeValue) {
                            lastDarkModeValue = currentDarkModeDetection
                            logger.debug("Theme change detected: dark: {}", currentDarkModeDetection)
                            for (listener in themeDetector.listeners) {
                                try {
                                    when (listener) {
                                        is DarkModeConsumer -> {listener.accept(currentDarkModeDetection)}
                                        is PrimaryColorConsumer -> {}
                                    }
                                } catch (e: RuntimeException) {
                                    logger.error("Caught exception during listener notifying ", e)
                                }
                            }
                        }

                        if (currentColorDetection !== this@DetectorThread.lastColorValue) {
                            this@DetectorThread.lastColorValue = currentColorDetection
                            logger.debug("Theme change detected: dark: {}", currentColorDetection)
                            for (listener in themeDetector.listeners) {
                                try {
                                    when (listener) {

                                        is DarkModeConsumer -> {}
                                        is PrimaryColorConsumer -> {
                                            listener.accept(currentColorDetection)
                                        }
                                    }
                                } catch (e: RuntimeException) {
                                    logger.error("Caught exception during listener notifying ", e)
                                }
                            }
                        }
                    }
                }.await()


            Advapi32Util.registryCloseKey(colorHkey.value)
            Advapi32Util.registryCloseKey(darkModeHkey.value)
            job.cancel()
        }
    }

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(WindowsThemeDetector::class.java)

        private const val DARK_MODE_REGISTRY_PATH = "Software\\Microsoft\\Windows\\CurrentVersion\\Themes\\Personalize"
        private const val ACCENT_COLOR_REGISTRY_PATH = "Software\\Microsoft\\Windows\\DWM"
        private const val ACCENT_COLOR_REGISTRY_VALUE = "AccentColor"
        private const val DARK_MODE_REGISTRY_VALUE = "AppsUseLightTheme"
    }
}
