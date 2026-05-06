import kotlinx.serialization.Serializable

@JsExport
@Serializable
data class GameConfig(
    val rows: Int,
    val cols: Int,
    val connect: Int,
)

@JsExport
@Serializable
enum class Player {
    Red,
    Yellow,
}

@JsExport
@Serializable
enum class Cell {
    Empty,
    Red,
    Yellow,
}

@JsExport
@Serializable
sealed class GameStatus {
    @Serializable
    data object InProgress : GameStatus()

    @Serializable
    data class Win(val player: Player) : GameStatus()

    @Serializable
    data object Draw : GameStatus()
}

@JsExport
@Serializable
data class GameState(
    val config: GameConfig,
    val board: List<List<Cell>>,
    val currentPlayer: Player,
    val status: GameStatus,
)

@JsExport
@Serializable
data class MoveResult(
    val state: GameState,
    val placedRow: Int?,
    val placedCol: Int?,
    val wasAccepted: Boolean,
)

fun newGame(config: GameConfig, firstPlayer: Player = Player.Red): GameState {
    require(config.rows > 0) { "rows must be > 0" }
    require(config.cols > 0) { "cols must be > 0" }
    require(config.connect > 0) { "connect must be > 0" }
    require(config.connect <= maxOf(config.rows, config.cols)) {
        "connect must be <= max(rows, cols)"
    }

    return GameState(
        config = config,
        board = createEmptyBoard(config.rows, config.cols),
        currentPlayer = firstPlayer,
        status = GameStatus.InProgress,
    )
}

fun tryDropPiece(state: GameState, column: Int): MoveResult {
    if (state.status != GameStatus.InProgress) {
        return MoveResult(state, null, null, false)
    }
    if (column !in 0 until state.config.cols) {
        return MoveResult(state, null, null, false)
    }

    val row = findDropRow(state.board, column)
    if (row == null) {
        return MoveResult(state, null, null, false)
    }

    val updatedBoard = placePiece(state.board, row, column, state.currentPlayer)
    val status = resolveStatus(updatedBoard, state.config, row, column, state.currentPlayer)
    val nextPlayer = if (state.currentPlayer == Player.Red) Player.Yellow else Player.Red

    return MoveResult(
        state = state.copy(
            board = updatedBoard,
            currentPlayer = if (status == GameStatus.InProgress) nextPlayer else state.currentPlayer,
            status = status,
        ),
        placedRow = row,
        placedCol = column,
        wasAccepted = true,
    )
}

fun isBoardFull(board: List<List<Cell>>): Boolean {
    return board.all { row -> row.none { cell -> cell == Cell.Empty } }
}

fun cellForPlayer(player: Player): Cell {
    return if (player == Player.Red) Cell.Red else Cell.Yellow
}

private fun createEmptyBoard(rows: Int, cols: Int): List<List<Cell>> {
    return List(rows) { List(cols) { Cell.Empty } }
}

private fun findDropRow(board: List<List<Cell>>, column: Int): Int? {
    for (row in board.indices.reversed()) {
        if (board[row][column] == Cell.Empty) {
            return row
        }
    }
    return null
}

private fun placePiece(
    board: List<List<Cell>>,
    row: Int,
    column: Int,
    player: Player,
): List<List<Cell>> {
    val playerCell = cellForPlayer(player)
    return board.mapIndexed { r, rowCells ->
        if (r != row) {
            rowCells
        } else {
            rowCells.mapIndexed { c, cell ->
                if (c == column) playerCell else cell
            }
        }
    }
}

private fun resolveStatus(
    board: List<List<Cell>>,
    config: GameConfig,
    row: Int,
    column: Int,
    player: Player,
): GameStatus {
    return when {
        hasConnect(board, config, row, column, player) -> GameStatus.Win(player)
        isBoardFull(board) -> GameStatus.Draw
        else -> GameStatus.InProgress
    }
}

private fun hasConnect(
    board: List<List<Cell>>,
    config: GameConfig,
    row: Int,
    column: Int,
    player: Player,
): Boolean {
    val target = cellForPlayer(player)
    val directions = listOf(
        1 to 0,
        0 to 1,
        1 to 1,
        1 to -1,
    )

    return directions.any { (dr, dc) ->
        val count =
            1 + countInDirection(board, row, column, dr, dc, target) +
                countInDirection(board, row, column, -dr, -dc, target)
        count >= config.connect
    }
}

private fun countInDirection(
    board: List<List<Cell>>,
    startRow: Int,
    startCol: Int,
    rowStep: Int,
    colStep: Int,
    target: Cell,
): Int {
    var r = startRow + rowStep
    var c = startCol + colStep
    var count = 0
    while (r in board.indices && c in board[0].indices) {
        if (board[r][c] != target) {
            break
        }
        count += 1
        r += rowStep
        c += colStep
    }
    return count
}
