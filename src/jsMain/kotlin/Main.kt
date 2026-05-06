import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.attributes.ButtonType
import org.jetbrains.compose.web.attributes.min
import org.jetbrains.compose.web.attributes.type
import org.jetbrains.compose.web.attributes.value
import org.jetbrains.compose.web.css.DisplayStyle
import org.jetbrains.compose.web.css.display
import org.jetbrains.compose.web.css.gap
import org.jetbrains.compose.web.css.gridTemplateColumns
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.H1
import org.jetbrains.compose.web.dom.Input
import org.jetbrains.compose.web.dom.Label
import org.jetbrains.compose.web.dom.P
import org.jetbrains.compose.web.dom.Text
import org.jetbrains.compose.web.renderComposable

private const val DefaultRows = 6
private const val DefaultCols = 7
private const val DefaultConnect = 4

fun main() {
    renderComposable(rootElementId = "root") {
        var rowsInput by remember { mutableStateOf(DefaultRows.toString()) }
        var colsInput by remember { mutableStateOf(DefaultCols.toString()) }
        var connectInput by remember { mutableStateOf(DefaultConnect.toString()) }
        var errorMessage by remember { mutableStateOf<String?>(null) }
        var gameState by remember {
            mutableStateOf(newGame(GameConfig(DefaultRows, DefaultCols, DefaultConnect)))
        }

        Div {
            H1 { Text("Connect Four") }

            Div {
                Label(forId = "rows") { Text("Rows") }
                Input(InputType.Number) {
                    id("rows")
                    min("1")
                    value(rowsInput)
                    onInput { rowsInput = it.value?.toString() ?: "" }
                }

                Label(forId = "cols") { Text("Cols") }
                Input(InputType.Number) {
                    id("cols")
                    min("1")
                    value(colsInput)
                    onInput { colsInput = it.value?.toString() ?: "" }
                }

                Label(forId = "connect") { Text("Connect") }
                Input(InputType.Number) {
                    id("connect")
                    min("1")
                    value(connectInput)
                    onInput { connectInput = it.value?.toString() ?: "" }
                }

                Button(attrs = {
                    type(ButtonType.Button)
                    onClick {
                        val updated = parseConfig(rowsInput, colsInput, connectInput)
                        if (updated == null) {
                            errorMessage = "Enter valid numbers for rows, cols, and connect."
                        } else {
                            errorMessage = null
                            gameState = newGame(updated)
                        }
                    }
                }) {
                    Text("Apply")
                }
                Button(attrs = {
                    type(ButtonType.Button)
                    onClick {
                        errorMessage = null
                        gameState = newGame(gameState.config)
                    }
                }) {
                    Text("Reset")
                }
            }

            if (errorMessage != null) {
                P { Text(errorMessage ?: "") }
            }

            Div {
                val statusText = when (val status = gameState.status) {
                    is GameStatus.InProgress -> "In progress"
                    is GameStatus.Draw -> "Draw"
                    is GameStatus.Win -> "Winner: ${status.player}"
                }
                P { Text("Status: $statusText") }
                P { Text("Current player: ${gameState.currentPlayer}") }
            }

            Div({
                style {
                    display(DisplayStyle.Grid)
                    gridTemplateColumns("repeat(${gameState.config.cols}, 32px)")
                    gap(4.px)
                }
            }) {
                gameState.board.forEachIndexed { rowIndex, row ->
                    row.forEachIndexed { colIndex, cell ->
                        val fillColor = when (cell) {
                            Cell.Empty -> "#ffffff"
                            Cell.Red -> "#e53935"
                            Cell.Yellow -> "#fbc02d"
                        }

                        Div(attrs = {
                            onClick {
                                if (gameState.status == GameStatus.InProgress) {
                                    val result = tryDropPiece(gameState, colIndex)
                                    if (result.wasAccepted) {
                                        gameState = result.state
                                    }
                                }
                            }
                            attr("data-row", rowIndex.toString())
                            attr("data-col", colIndex.toString())
                            style {
                                property("width", "32px")
                                property("height", "32px")
                                property("border", "1px solid #333")
                                property("border-radius", "16px")
                                property("background-color", fillColor)
                                property("cursor", if (gameState.status == GameStatus.InProgress) "pointer" else "default")
                            }
                        })
                    }
                }
            }
        }
    }
}

private fun parseConfig(rows: String, cols: String, connect: String): GameConfig? {
    val parsedRows = rows.toIntOrNull() ?: return null
    val parsedCols = cols.toIntOrNull() ?: return null
    val parsedConnect = connect.toIntOrNull() ?: return null
    if (parsedRows <= 0 || parsedCols <= 0 || parsedConnect <= 0) {
        return null
    }
    if (parsedConnect > maxOf(parsedRows, parsedCols)) {
        return null
    }
    return GameConfig(parsedRows, parsedCols, parsedConnect)
}