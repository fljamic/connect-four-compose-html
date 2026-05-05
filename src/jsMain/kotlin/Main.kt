import org.w3c.dom.Element
import kotlinx.browser.document
import kotlinx.serialization.Serializable

@JsExport
@Serializable
data class Message(val topic: String, val content: String)

fun main() {
    val message = Message(topic = "Kotlin/JS", content = "Hello!")
    document.body!!.appendMessage(message)
}

private val prettyPrintOptions = PrettyPrintJsonOptions(
    trailingCommas = false,
    quoteKeys = true,
)

fun Element.appendMessage(message: Message) {
    val jsonContainer = document.createElement("div").apply {
        innerHTML = HtmlJsonConverter.toHtml(
            message,
            prettyPrintOptions
        )
    }

    appendChild(jsonContainer)
}