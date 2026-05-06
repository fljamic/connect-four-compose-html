import kotlinx.browser.window
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private const val StorageKey = "connect-four-state"
private const val StorageVersion = 1

@Serializable
data class StoredGame(
    val version: Int,
    val state: GameState,
)

private val storageJson = Json {
    encodeDefaults = true
    ignoreUnknownKeys = true
}

fun loadGameState(): GameState? {
    val raw = window.localStorage.getItem(StorageKey) ?: return null
    return runCatching {
        val stored = storageJson.decodeFromString<StoredGame>(raw)
        if (stored.version != StorageVersion) {
            null
        } else {
            stored.state
        }
    }.getOrNull()
}

fun saveGameState(state: GameState) {
    val stored = StoredGame(version = StorageVersion, state = state)
    val raw = storageJson.encodeToString(stored)
    window.localStorage.setItem(StorageKey, raw)
}

fun clearGameState() {
    window.localStorage.removeItem(StorageKey)
}
