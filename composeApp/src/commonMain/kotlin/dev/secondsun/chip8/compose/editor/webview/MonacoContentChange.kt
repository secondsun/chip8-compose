package dev.secondsun.chip8.compose.editor.webview

import com.multiplatform.webview.jsbridge.IJsMessageHandler
import com.multiplatform.webview.jsbridge.JsMessage
import com.multiplatform.webview.jsbridge.dataToJsonString
import com.multiplatform.webview.jsbridge.processParams
import com.multiplatform.webview.web.WebViewNavigator
import dev.secondsun.chip8.compose.assembler.ParsedTokenType
import dev.secondsun.chip8.compose.assembler.Token
import dev.secondsun.chip8.compose.assembler.parse
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*

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
        val program = processParams<MonacoContentChangedMessage>(message)
        val parsedProgram = parse(program.program)
        val errors = parsedProgram.parsedTokens.filter { it.type == ParsedTokenType.Error }
            .flatMap { it.tokens }
            .filterIsInstance<Token.Error>()
            .map { errorToken ->
                    TokenError(errorToken.line, errorToken.column, errorToken.length, errorToken.message)
            }
            .toList()
        println(errors)
        //val data = MonacoContentChangeResult(dataToJsonString(errors))
        val jsonString = dataToJsonString(errors)
        callback(jsonString)
    }

}

@Serializable
data class TokenError(val line: Int, val column: Int, val length: Int, val message: String)

@Serializable
data class MonacoContentChangeResult(val message: String = "OK")

@Serializable
data class MonacoContentChangedMessage(
    val program: String,
)