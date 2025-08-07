package dev.secondsun.chip8.compose

import com.multiplatform.webview.jsbridge.JsMessage
import dev.secondsun.chip8.compose.editor.state.FileType
import dev.secondsun.chip8.compose.editor.webview.MonacoInitMessageHandler
import dev.secondsun.chip8.compose.editor.webview.MonacoInitResult
import kotlin.test.Test
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class ComposeAppCommonTest {

    @Test
    fun example() {
        val handler = MonacoInitMessageHandler(
            {
                """
            This is a line
            This is a second line
            This is a third line
            """.trimIndent()
            },
            { FileType.TEXT },
            { true })

        val expectedResult = MonacoInitResult("""
            This is a line
            This is a second line
            This is a third line
            """.trimIndent(), FileType.TEXT, true)

        handler.handle(JsMessage(1,"","")  , null) {
            println(it)
        }
        
        val json = Json.encodeToString(expectedResult)
        println(json)

    }
    

}