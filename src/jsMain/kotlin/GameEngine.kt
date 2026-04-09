import androidx.compose.runtime.*
import kotlinx.browser.window
import kotlinx.serialization.*
import kotlinx.serialization.json.*

@Serializable
enum class Player { RED, YELLOW }

@Serializable
data class GameConfig(
    val rows: Int = 6,
    val cols: Int = 7,
    val winCondition: Int = 4
)

class ConnectFourEngine(var config: GameConfig) {
    var grid by mutableStateOf(List(config.cols) { List<Player?>(config.rows) { null } })
    var currentPlayer by mutableStateOf(Player.RED)
    var winner by mutableStateOf<Player?>(null)
    var isDraw by mutableStateOf(false)

    fun updateConfig(newRows: Int, newCols: Int, win: Int) {
        val safeRows = newRows.coerceIn(5, 20)
        val safeCols = newCols.coerceIn(4, 20)
        val safeWin = win.coerceIn(4, 10)
        this.config = GameConfig(rows = safeRows, cols = safeCols, winCondition = safeWin)
        window.localStorage.removeItem("c4_save")
        reset()
    }

    fun dropPiece(colIndex: Int): Boolean {
        if (winner != null || isDraw) return false

        val col = grid[colIndex].toMutableList()
        val emptyRowIndex = col.indexOfLast { it == null }

        if (emptyRowIndex == -1) return false
        col[emptyRowIndex] = currentPlayer
        val newGrid = grid.toMutableList()
        newGrid[colIndex] = col
        grid = newGrid

        if (checkWin(colIndex, emptyRowIndex)) {
            winner = currentPlayer
        } else if (grid.all { column -> column.none { it == null } }) {
            isDraw = true
        } else {
            currentPlayer = if (currentPlayer == Player.RED) Player.YELLOW else Player.RED
        }
        saveToLocalStorage()
        return true
    }


    private fun checkWin(col: Int, row: Int): Boolean {
        val directions = listOf(
            Pair(1, 0),  // Horizontal
            Pair(0, 1),  // Vertical
            Pair(1, 1),  // Diagonal /
            Pair(1, -1)  // Diagonal \
        )

        return directions.any { (dx, dy) ->
            countSequence(col, row, dx, dy) + countSequence(col, row, -dx, -dy) + 1 >= config.winCondition
        }
    }

    private fun countSequence(col: Int, row: Int, dx: Int, dy: Int): Int {
        var count = 0
        var currCol = col + dx
        var currRow = row + dy

        while (currCol in 0 until config.cols &&
            currRow in 0 until config.rows &&
            grid[currCol][currRow] == currentPlayer) {
            count++
            currCol += dx
            currRow += dy
        }
        return count
    }

    fun reset() {
        grid = List(config.cols) { List(config.rows) { null } }
        currentPlayer = Player.RED
        winner = null
        isDraw = false
    }

    fun saveToLocalStorage() {
        val boardData = grid.map { col -> col.map { it?.name } }
        val json = Json.encodeToString(boardData)
        window.localStorage.setItem("c4_save", json)
    }

    fun loadFromLocalStorage() {
        try {
        val saved = window.localStorage.getItem("c4_save") ?: return
        val boardData: List<List<String?>> = Json.decodeFromString(saved)
        grid = boardData.map { col ->
            col.map { name -> if (name != null) Player.valueOf(name) else null }
        }} catch (e: Exception) {println("Failed to load save: ${e.message}")}
    }
}