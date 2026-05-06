import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
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
private const val CellSizePx = 32
private const val CellGapPx = 4

fun main() {
    renderComposable(rootElementId = "root") {
        val initialState = remember {
            loadGameState() ?: newGame(GameConfig(DefaultRows, DefaultCols, DefaultConnect))
        }
        var rowsInput by remember { mutableStateOf(initialState.config.rows.toString()) }
        var colsInput by remember { mutableStateOf(initialState.config.cols.toString()) }
        var connectInput by remember { mutableStateOf(initialState.config.connect.toString()) }
        var errorMessage by remember { mutableStateOf<String?>(null) }
        var gameState by remember { mutableStateOf(initialState) }
        var lastMove by remember { mutableStateOf<Pair<Int, Int>?>(null) }
        var hoveredCol by remember { mutableStateOf<Int?>(null) }

        LaunchedEffect(gameState) {
            saveGameState(gameState)
        }

        Div(attrs = {
            attr("class", "app")
        }) {
            H1 { Text("Connect Four") }

            Div(attrs = {
                attr("class", "controls")
            }) {
                Div(attrs = {
                    attr("class", "fields")
                }) {
                    Div(attrs = { attr("class", "field") }) {
                        Label(forId = "rows") { Text("Rows") }
                        Input(InputType.Number) {
                            id("rows")
                            min("1")
                            value(rowsInput)
                            onInput { rowsInput = it.value?.toString() ?: "" }
                        }
                    }

                    Div(attrs = { attr("class", "field") }) {
                        Label(forId = "cols") { Text("Cols") }
                        Input(InputType.Number) {
                            id("cols")
                            min("1")
                            value(colsInput)
                            onInput { colsInput = it.value?.toString() ?: "" }
                        }
                    }

                    Div(attrs = { attr("class", "field") }) {
                        Label(forId = "connect") { Text("Connect") }
                        Input(InputType.Number) {
                            id("connect")
                            min("1")
                            value(connectInput)
                            onInput { connectInput = it.value?.toString() ?: "" }
                        }
                    }
                }

                Div(attrs = {
                    attr("class", "actions")
                }) {
                Button(attrs = {
                    type(ButtonType.Button)
                    onClick {
                        errorMessage = null
                        lastMove = null
                        hoveredCol = null
                        gameState = newGame(gameState.config)
                    }
                }) {
                    Text("Restart")
                }
                Button(attrs = {
                    type(ButtonType.Button)
                    onClick {
                        val updated = parseConfig(rowsInput, colsInput, connectInput)
                        if (updated == null) {
                            errorMessage = "Enter valid numbers for rows, cols, and connect."
                        } else {
                            errorMessage = null
                            clearGameState()
                            lastMove = null
                            hoveredCol = null
                            gameState = newGame(updated)
                        }
                    }
                }) {
                    Text("New Game")
                }
                }
            }

            if (errorMessage != null) {
                P { Text(errorMessage ?: "") }
            }

            Div {
                val currentColor = when (gameState.currentPlayer) {
                    Player.Red -> "#e53935"
                    Player.Yellow -> "#fbc02d"
                }
                P {
                    Div(attrs = { attr("class", "current-player") }) {
                        Text("Current player:")
                        Div(attrs = {
                            attr("class", "player-swatch")
                            style {
                                property("background-color", currentColor)
                            }
                        })
                    }
                }
            }

            Div(attrs = { attr("class", "board-wrap") }) {
                Div(attrs = {
                    attr("class", "board")
                    style {
                        display(DisplayStyle.Grid)
                        gridTemplateColumns("repeat(${gameState.config.cols}, ${CellSizePx}px)")
                        gap(CellGapPx.px)
                    }
                }) {
                    gameState.board.forEachIndexed { rowIndex, row ->
                        row.forEachIndexed { colIndex, cell ->
                        val fillColor = when (cell) {
                            Cell.Empty -> "transparent"
                            Cell.Red -> "#e53935"
                            Cell.Yellow -> "#fbc02d"
                        }
                        val isLastMove = lastMove?.first == rowIndex && lastMove?.second == colIndex
                        val pieceClass = if (isLastMove && cell != Cell.Empty) "piece drop" else "piece"
                        val dropDistancePx = (rowIndex + 1) * (CellSizePx + CellGapPx)
                        val isHoveredCol =
                            hoveredCol == colIndex && gameState.status == GameStatus.InProgress

                            Div(attrs = {
                                onClick {
                                    if (gameState.status == GameStatus.InProgress) {
                                        val result = tryDropPiece(gameState, colIndex)
                                        if (result.wasAccepted) {
                                            gameState = result.state
                                            val placedRow = result.placedRow
                                            val placedCol = result.placedCol
                                            lastMove = if (placedRow != null && placedCol != null) {
                                                placedRow to placedCol
                                            } else {
                                                null
                                            }
                                        }
                                    }
                                }
                                onMouseEnter {
                                    if (gameState.status == GameStatus.InProgress) {
                                        hoveredCol = colIndex
                                    }
                                }
                                onMouseLeave { hoveredCol = null }
                                attr("data-row", rowIndex.toString())
                                attr("data-col", colIndex.toString())
                                style {
                                    property("width", "${CellSizePx}px")
                                    property("height", "${CellSizePx}px")
                                    property("border-radius", "${CellSizePx / 2}px")
                                    property("background-color", if (isHoveredCol) "rgba(0, 0, 0, 0.06)" else "transparent")
                                    property("position", "relative")
                                    property("overflow", if (isLastMove && cell != Cell.Empty) "visible" else "hidden")
                                    property("cursor", if (gameState.status == GameStatus.InProgress) "pointer" else "default")
                                    if (isLastMove && cell != Cell.Empty) {
                                        property("--drop-distance", "${dropDistancePx}px")
                                        property("z-index", "2")
                                    }
                                }
                            }) {
                                if (cell != Cell.Empty) {
                                    Div(attrs = {
                                        attr("class", pieceClass)
                                        style {
                                            property("background-color", fillColor)
                                        }
                                    })
                                }
                                Div(attrs = {
                                    attr("class", "slot-border")
                                })
                            }
                        }
                    }
                }
            }

            when (val status = gameState.status) {
                is GameStatus.Win -> {
                    val winnerColor = when (status.player) {
                        Player.Red -> "#e53935"
                        Player.Yellow -> "#fbc02d"
                    }
                    P {
                        Div(attrs = { attr("class", "current-player") }) {
                            Text("Winner:")
                            Div(attrs = {
                                attr("class", "player-swatch")
                                style {
                                    property("background-color", winnerColor)
                                }
                            })
                        }
                    }
                }
                is GameStatus.Draw -> {
                    P {
                        Div(attrs = { attr("class", "current-player") }) {
                            Text("Draw")
                            Div(attrs = {
                                attr("class", "player-swatch")
                                style {
                                    property("background-color", "#9e9e9e")
                                }
                            })
                        }
                    }
                }
                is GameStatus.InProgress -> Unit
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