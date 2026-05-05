import kotlinx.browser.document
import kotlin.test.Test
import kotlin.test.assertEquals

class BrowserTest {
    @Test
    fun mainTest() {
        val mockContainer = document.createElement("div")
        val expectMessage = Message(topic = "Test Framework", content = "Hello!")
        mockContainer.appendMessage(expectMessage)

        assertEquals(
            "{\n   \"topic\": \"Test Framework\",\n   \"content\": \"Hello!\"\n}",
            mockContainer.textContent
        )
    }
}