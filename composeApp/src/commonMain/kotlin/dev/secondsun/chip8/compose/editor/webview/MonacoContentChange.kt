package dev.secondsun.chip8.compose.editor.webview

import com.multiplatform.webview.jsbridge.IJsMessageHandler
import com.multiplatform.webview.jsbridge.JsMessage
import com.multiplatform.webview.jsbridge.dataToJsonString
import com.multiplatform.webview.jsbridge.processParams
import com.multiplatform.webview.web.WebViewNavigator
import dev.secondsun.chip8.compose.assembler.parse
import dev.secondsun.chip8.compose.editor.state.FileType
import kotlinx.serialization.Serializable

/**
 * When Monaco starts up, it will call this handler to get the
 * file content and theme.
 */
class MonacoContentChangedMessageHandler() : IJsMessageHandler {
    override fun methodName(): String {
        println("Change Message register")
        return "MonacoContentChanged"
    }

    override fun handle(
        message: JsMessage,
        navigator: WebViewNavigator?,
        callback: (String) -> Unit
    ) {
        println("Change Message handle")
        println(message);
        val program = processParams<MonacoContentChangedMessage>(message);
        val parsedProgram = parse(program.program);

        val data = MonacoContentChangeResult()
        val jsonString = dataToJsonString(data)
        callback(jsonString)    }

}

@Serializable
data class MonacoContentChangeResult(val message : String = "OK") {

}

@Serializable
data class MonacoContentChangedMessage(
    val program: String,
)