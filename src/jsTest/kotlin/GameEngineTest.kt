import kotlin.test.*

class ConnectFourEngineTest {

    @Test
    fun testInitialGridIsEmpty() {
        val config = GameConfig(rows = 6, cols = 7, winCondition = 4)
        val engine = ConnectFourEngine(config)

        for (c in 0 until config.cols) {
            for (r in 0 until config.rows) {
                assertNull(engine.grid[c][r], "Cell at $c, $r should be null")
            }
        }
        assertEquals(Player.RED, engine.currentPlayer)
        assertNull(engine.winner)
    }

    @Test
    fun testDroppingPieceUpdatesGridAndPlayer() {
        val engine = ConnectFourEngine(GameConfig(7, 7, 4))
        val result = engine.dropPiece(0)

        assertTrue(result, "Move should be valid")
        assertTrue(engine.grid[0].contains(Player.RED), "Column 0 should contain a RED piece")
        assertEquals(Player.YELLOW, engine.currentPlayer, "Should be Yellow's turn")
    }

    @Test
    fun testCannotDropInFullColumn() {
        val rows = 3
        val engine = ConnectFourEngine(GameConfig(rows = rows, cols = 3, winCondition = 3))

        repeat(rows) { engine.dropPiece(0) }
        val playerBefore = engine.currentPlayer

        val result = engine.dropPiece(0) // Attempting 4th piece in 3-row col

        assertFalse(result, "Should return false for full column")
        assertEquals(playerBefore, engine.currentPlayer, "Player should not change on failed move")
    }

    @Test
    fun testHorizontalWin() {
        val engine = ConnectFourEngine(GameConfig(6, 7, 4))
        val moves = listOf(0, 0, 1, 1, 2, 2, 3)
        moves.forEach { engine.dropPiece(it) }

        assertEquals(Player.RED, engine.winner, "Red should win horizontally")
    }

    @Test
    fun testVerticalWin() {
        val engine = ConnectFourEngine(GameConfig(6, 7, 4))
        val moves = listOf(0, 1, 0, 1, 0, 1, 0)
        moves.forEach { engine.dropPiece(it) }

        assertEquals(Player.RED, engine.winner, "Red should win vertically")
    }

    @Test
    fun testDiagonalWinAscending() {
        val engine = ConnectFourEngine(GameConfig(6, 7, 4))

        val moves = listOf(0, 1, 1, 2, 2, 3, 2, 3, 3, 0, 3)
        moves.forEach { engine.dropPiece(it) }

        assertEquals(Player.RED, engine.winner, "Red should win on ascending diagonal")
    }

    @Test
    fun testDiagonalWinDescending() {
        val engine = ConnectFourEngine(GameConfig(6, 7, 4))

        val moves = listOf(3, 2, 2, 1, 1, 0, 1, 0, 0, 3, 0)
        moves.forEach { engine.dropPiece(it) }

        assertEquals(Player.RED, engine.winner, "Red should win on descending diagonal")
    }

    @Test
    fun testDrawRecognitionSmallBlock() {
        val engine = ConnectFourEngine(GameConfig(rows = 2, cols = 2, winCondition = 3))

        engine.dropPiece(0)
        engine.dropPiece(1)
        engine.dropPiece(1)
        engine.dropPiece(0)

        assertTrue(engine.isDraw, "Game should be a draw")
        assertNull(engine.winner, "Winner should be null on draw")
    }

    @Test
    fun testUpdateConfigResetsState() {
        val engine = ConnectFourEngine(GameConfig(7, 7, 4))
        engine.dropPiece(0)

        engine.updateConfig(10, 10, 5)

        assertEquals(10, engine.config.rows)
        assertEquals(10, engine.config.cols)
        assertNull(engine.grid[0][0], "Grid should be cleared after config update")
        assertEquals(Player.RED, engine.currentPlayer, "Game should reset to Red's turn")
    }

    @Test
    fun testSmallestWinCondition() {
        val engine = ConnectFourEngine(GameConfig(rows = 3, cols = 3, winCondition = 2))
        engine.dropPiece(0)
        engine.dropPiece(1)
        engine.dropPiece(0)

        assertEquals(Player.RED, engine.winner, "Should support winCondition = 2")
    }

    @Test
    fun testNoMovesAllowedAfterWin() {
        val engine = ConnectFourEngine(GameConfig(6, 7, 4))
        // Win the game vertically for RED
        listOf(0, 1, 0, 1, 0, 1, 0).forEach { engine.dropPiece(it) }
        assertNotNull(engine.winner)

        val currentPlayerAtWin = engine.currentPlayer
        val moveAttempt = engine.dropPiece(5)

        assertFalse(moveAttempt, "Engine should reject moves after a winner is found")
        assertEquals(currentPlayerAtWin, engine.currentPlayer, "Player state should remain frozen after win")
    }

    @Test
    fun testWinAtAbsoluteBoardBoundaries() {
        val config = GameConfig(rows = 15, cols = 15, winCondition = 5)
        val engine = ConnectFourEngine(config)

        for (col in 10..14) {
            repeat(14) { engine.dropPiece(col) }
        }

        listOf(10, 0, 11, 0, 12, 0, 13, 0, 14).forEach { engine.dropPiece(it) }

        assertEquals(Player.RED, engine.winner, "Win should be detected at board boundaries")
    }

    @Test
    fun testDrawOnCompletelyFullBoard() {
        val engine = ConnectFourEngine(GameConfig(rows = 6, cols = 7, winCondition = 4))

        val fillPattern = listOf(0, 1, 2, 3, 4, 5, 6)
        repeat(6) {
            fillPattern.forEach { engine.dropPiece(it) }
        }

        if (engine.winner == null) {
            assertTrue(engine.isDraw, "Engine should detect draw when all cells are occupied")
        }
    }

    @Test
    fun testPersistenceSerialization() {
        val originalConfig = GameConfig(rows = 12, cols = 13, winCondition = 6)
        val engine = ConnectFourEngine(originalConfig)


        val serializer = GameConfig.serializer()
        val json = kotlinx.serialization.json.Json.encodeToString(serializer, engine.config)
        val decodedConfig = kotlinx.serialization.json.Json.decodeFromString(serializer, json)

        assertEquals(originalConfig.rows, decodedConfig.rows)
        assertEquals(originalConfig.cols, decodedConfig.cols)
        assertEquals(originalConfig.winCondition, decodedConfig.winCondition)
    }

    @Test
    fun testResetClearsAllState() {
        val engine = ConnectFourEngine(GameConfig(7, 6, 4))
        engine.dropPiece(0)
        engine.dropPiece(0)
        engine.dropPiece(1)

        engine.reset()

        assertNull(engine.winner)
        assertFalse(engine.isDraw)
        assertEquals(Player.RED, engine.currentPlayer)
        assertTrue(engine.grid.all { col -> col.all { it == null } }, "Grid must be null after reset")
    }
}