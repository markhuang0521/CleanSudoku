package com.cleanSudoku.cleansodoku.models

import com.cleanSudoku.cleansodoku.database.DbSudokuGame
import com.cleanSudoku.cleansodoku.database.SudokuDao
import com.cleanSudoku.cleansodoku.util.Difficulty
import com.cleanSudoku.cleansodoku.util.formatToTimeString
import com.cleanSudoku.cleansodoku.util.fullCopy
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.text.DecimalFormat

class SudokuGameRepository(
    private val sudokuDao: SudokuDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    private lateinit var sudoku: SudokuGenerator
    lateinit var board: Array<Array<Cell>>
    lateinit var solution: Array<Array<Cell>>
    lateinit var originalBoard: Array<Array<Cell>>
    private val boardSize = 9


    suspend fun generateBoard(difficulty: Difficulty): Array<Array<Cell>> =
        withContext(ioDispatcher)
        {
            sudoku = SudokuGenerator.generate(difficulty)
            board = intArrayToCellArray(sudoku.board)
            solution = intArrayToCellArray(sudoku.solution)
            originalBoard = intArrayToCellArray(sudoku.board)
            return@withContext board

        }

    private fun intArrayToCellArray(board: Array<IntArray>): Array<Array<Cell>> {
        return Array(boardSize) { row ->
            Array(boardSize) { col ->
                // game board generation
                val num = board[row][col]
                val isStartCell = num != 0
                Cell(row, col, num, isStartCell)
            }
        }
    }

    suspend fun updateCurrentGame(game: DbSudokuGame) = withContext(ioDispatcher) {
        sudokuDao.updateGameByObject(game)
    }

    suspend fun updateGameById(id: Long, isComplete: Boolean, isSuccessful: Boolean) = withContext(ioDispatcher) {
        sudokuDao.updateGameById(id, isComplete, isSuccessful)
    }

    suspend fun saveCurrentGame(dbSudokuGame: DbSudokuGame) =
        withContext(ioDispatcher) { sudokuDao.saveCurrentGame(dbSudokuGame) }

    suspend fun getCurrentGameById(id: Long): DbSudokuGame = withContext(ioDispatcher)
    {
        return@withContext sudokuDao.getGameById(id)
    }

    suspend fun checkCurrentGame(): DbSudokuGame? = withContext(ioDispatcher) {
        return@withContext sudokuDao.getCurrentGame()
    }


    suspend fun loadCurrentGame(): DbSudokuGame? = withContext(ioDispatcher) {
        val currentGame = sudokuDao.getCurrentGame()
        currentGame?.let {
            board = currentGame.currentBoard
            solution = currentGame.solutionBoard
            originalBoard = currentGame.originalBoard
        }
        return@withContext currentGame
    }


    suspend fun getCurrentGameId(): Long? = withContext(ioDispatcher) {
        return@withContext sudokuDao.getCurrentGameId()
    }

    suspend fun getTotalGame(difficulty: String): Int = withContext(ioDispatcher) {
        return@withContext sudokuDao.getTotalGame(difficulty)
    }

    suspend fun getTotalWin(difficulty: String): Int = withContext(ioDispatcher) {
        return@withContext sudokuDao.getTotalWin(difficulty)
    }

    suspend fun getWinRate(difficulty: String): Double = withContext(ioDispatcher) {
        val game = getTotalGame(difficulty).toDouble()
        val win = getTotalWin(difficulty).toDouble()
        val rate = if (game != 0.0) (win / game) else 0.0
        Timber.d("win: $win game:$game rate: $rate.toString()")

        return@withContext rate
    }

    suspend fun getBestTime(difficulty: String): Long? = withContext(ioDispatcher) {
        return@withContext sudokuDao.getBestTime(difficulty)
    }

    suspend fun getAvgTime(difficulty: String): Long? = withContext(ioDispatcher) {
        return@withContext sudokuDao.getAvgTime(difficulty)
    }

    suspend fun getGameStatistic(difficulty: String): GameStatistics = withContext(ioDispatcher) {
        val percentFormat = DecimalFormat("##%")
        return@withContext GameStatistics(
            getTotalGame(difficulty).toString(),
            getTotalWin(difficulty).toString(),
            percentFormat.format(getWinRate(difficulty)),
            getBestTime(difficulty).formatToTimeString(),
            getAvgTime(difficulty).formatToTimeString()

        )

    }

    fun getCurrentCell(row: Int, col: Int): Cell {
        return board[row][col]
    }

    fun resetBoard(): Array<Array<Cell>> {
        board = originalBoard.fullCopy()
        return board
    }


    fun addNotesForCurrentCell(cell: Cell, num: Int): Array<Array<Cell>> {
        if (!isStartingCell(cell.row, cell.col)) {
            board[cell.row][cell.col].notes.add(num)
        }
        return board
    }


    fun updateCurrentCell(row: Int, col: Int, num: Int): Array<Array<Cell>> {

        if (!isStartingCell(row, col)) {
            board[row][col].value = num
        }
        return board

    }

    fun clearCell(row: Int, col: Int): Array<Array<Cell>> {
        if (!isStartingCell(row, col)) {
            board[row][col].notes.clear()
            board[row][col].value = 0


        }
        return board

    }


    fun showCellHint(row: Int, col: Int): Array<Array<Cell>> {
        if (!isStartingCell(row, col)) {
            board[row][col].value = solution[row][col].value
        }
        return board

    }


    private fun isStartingCell(row: Int, col: Int): Boolean {
        return board[row][col].isStartingCell
    }

    fun isCellCorrect(cell: Cell): Boolean =
        board[cell.row][cell.col].value == solution[cell.row][cell.col].value


    fun isBoardComplete(board: Array<Array<Cell>>): Boolean {
        for (row in 0 until boardSize) {
            for (col in 0 until boardSize) {
                if (board[row][col].value != solution[row][col].value) return false
            }
        }
        return true
//        return board.contentDeepEquals(solution)
    }


    suspend fun deleteAll() = withContext(ioDispatcher) {

        sudokuDao.deleteAll()
    }


    fun printBoard() {
        println("--current board----------------")

        for (i in 0 until sudoku.boardWidth) {
            for (j in 0 until sudoku.boardWidth) print("${board[i][j].value}  ")
            println()
        }

        println("--original board----------------")

        println()

    }


}

