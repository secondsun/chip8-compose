import com.multiplatform.webview.jsbridge.IJsMessageHandler
import com.multiplatform.webview.jsbridge.JsMessage
import com.multiplatform.webview.jsbridge.dataToJsonString
import com.multiplatform.webview.web.WebViewNavigator
import dev.secondsun.chip8.compose.editor.state.FileType
import kotlinx.serialization.Serializable

/**
 * When Monaco starts up, it will call this handler to get the
 * file content and theme.
 */
class MonacoInitMessageHandler(val provideFileText : ()->String, val provideFileType : ()-> FileType, val provideTheme:()->Boolean) : IJsMessageHandler {
    override fun methodName(): String {
        return "MonacoInit"
    }

    override fun handle(
        message: JsMessage,
        navigator: WebViewNavigator?,
        callback: (String) -> Unit
    ) {

        val data = MonacoInitResult(provideFileText().replace("\n", "\\n"), provideFileType(), provideTheme())
        val jsonString = dataToJsonString(data)
        callback(jsonString)    }

}

@Serializable
data class MonacoInitResult(val fileContent : String , val fileType: FileType, val darkMode : Boolean ) {

}

