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

import jthemedetecor.consumers.ThemingConsumer
import jthemedetecor.util.OsInfo
import kotlinx.coroutines.CoroutineScope
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import oshi.annotation.concurrent.ThreadSafe
import java.awt.Color
import kotlin.concurrent.Volatile

/**
 * For detecting the theme (dark/light) used by the Operating System.
 *
 * @author Daniel Gyorffy
 */
abstract class OsThemeDetector internal constructor() {
    open val primaryColor: Color
        get() = Color(0x673AB7)

    @get:ThreadSafe
    abstract val isDark: Boolean

    /**
     * Registers a [Consumer] that will listen to a theme-change.
     *
     * @param listener the [Consumer] that accepts a [Boolean] that represents
     * that the os using a dark theme or not
     */
    @ThreadSafe
    abstract fun registerListener(scope: CoroutineScope, listener: ThemingConsumer<*>)

    /**
     * Removes the listener.
     */
    @ThreadSafe
    abstract fun removeListener(listener: ThemingConsumer<*>?)


    private class EmptyDetector : OsThemeDetector() {
        override val isDark = false

        override fun registerListener(scope: CoroutineScope, listener: ThemingConsumer<*>) {
        }

        override fun removeListener(listener: ThemingConsumer<*>?) {
        }
    }

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(OsThemeDetector::class.java)

        @Volatile
        private var osThemeDetector: OsThemeDetector? = null

        @get:ThreadSafe
        val detector: OsThemeDetector
            get() {
                var instance = osThemeDetector

                if (instance == null) {
                    synchronized(OsThemeDetector::class.java) {
                        instance = osThemeDetector
                        if (instance == null) {
                            instance = createDetector()
                            osThemeDetector = instance
                        }
                    }
                }

                return instance!!
            }

        private fun createDetector(): OsThemeDetector {
            if (OsInfo.isWindows10OrLater) {
                logDetection("Windows 10", WindowsThemeDetector::class.java)
                return WindowsThemeDetector()
            } else if (OsInfo.isGnome) {
                logDetection("Gnome", GnomeThemeDetector::class.java)
                return GnomeThemeDetector()
            } else if (OsInfo.isMacOsMojaveOrLater) {
                logDetection("MacOS", MacOSThemeDetector::class.java)
                return MacOSThemeDetector()
            } else {
                logger.debug(
                    "Theme detection is not supported on the system: {} {}",
                    OsInfo.family,
                    OsInfo.version
                )
                logger.debug("Creating empty detector...")
                return EmptyDetector()
            }
        }

        private fun logDetection(desktop: String, detectorClass: Class<out OsThemeDetector>) {
            logger.debug("Supported Desktop detected: {}", desktop)
            logger.debug("Creating {}...", detectorClass.name)
        }

        @get:ThreadSafe
        val isSupported: Boolean
            get() = OsInfo.isWindows10OrLater || OsInfo.isMacOsMojaveOrLater || OsInfo.isGnome
    }
}
