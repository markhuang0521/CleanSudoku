package com.example.cleansodoku.game

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cleansodoku.database.DbSudokuGame
import com.example.cleansodoku.models.Cell
import com.example.cleansodoku.models.Move
import com.example.cleansodoku.models.SudokuGame
import com.example.cleansodoku.statistics.GameStatistics
import com.example.cleansodoku.utils.Difficulty
import com.example.cleansodoku.utils.formatToTimeString
import kotlinx.coroutines.launch
import timber.log.Timber
import java.text.DecimalFormat
import java.util.*

class SudokuViewModel(private val sudokuGame: SudokuGame) : ViewModel() {
    var gameBoard = MutableLiveData<Array<Array<Cell>>>()
    var solutionBoard = MutableLiveData<Array<Array<Cell>>>()
    var originalBoard = MutableLiveData<Array<Array<Cell>>>()
    val selectedCell = MutableLiveData<Cell>()
    val gameDifficulty = MutableLiveData<String>()
    val mistakes = MutableLiveData<Int>()
    val hints = MutableLiveData<Int>()
    val timer = MutableLiveData<Long>()
    val gameId = MutableLiveData<Long>()
    val gameStatistic = MutableLiveData<GameStatistics>()

    private val isNoteOn = MutableLiveData<Boolean>(false)
    private val hasCurrentGame = MutableLiveData<Boolean>(false)
    private var selectedRow = 0
    private var selectedCol = 0
    private val undoStack: Stack<Move> = Stack()

    fun startNewGame(difficulty: Difficulty) {

        viewModelScope.launch {
            updateCurrentGame(true, false)
            timer.value = 0
            hints.value = 1
            mistakes.value = 0
            gameDifficulty.value = difficulty.name

            gameBoard.value = null
            gameBoard.value = sudokuGame.generateBoard(difficulty)
            solutionBoard.value = sudokuGame.solution
            originalBoard.value = sudokuGame.originalBoard
            val dbSudokuGame = DbSudokuGame(
                difficulty = gameDifficulty.value!!,
                mistakes = mistakes.value!!,
                hints = hints.value!!,
                currentBoard = gameBoard.value!!,
                solutionBoard = solutionBoard.value!!,
                originalBoard = originalBoard.value!!,
                time = timer.value!!
            )

            sudokuGame.saveCurrentGame(dbSudokuGame)
//            gameId.value = sudokuGame.getCurrentGameId()
        }

    }

    init {
        viewModelScope.launch {
            sudokuGame.deleteAll()

        }


    }


    fun loadGame() {

        if (gameBoard.value == null) {
            viewModelScope.launch {
                val currentGame = sudokuGame.loadCurrentGame()
                Timber.d("current game: ${currentGame.toString()}")
                currentGame?.let {
                    Timber.d("current game: ${currentGame.currentBoard.size}")

                    gameId.value = currentGame.id
                    gameBoard.value = (currentGame.currentBoard)
                    solutionBoard.value = (currentGame.solutionBoard)
                    originalBoard.value = (currentGame.originalBoard)
                    timer.value = currentGame.time
                    mistakes.value = currentGame.mistakes
                    hints.value = currentGame.mistakes
                    gameDifficulty.value = currentGame.difficulty
                }

            }
        }

    }

    fun updateCurrentGame(isCompleted: Boolean, isSucceed: Boolean) {
        viewModelScope.launch {
            gameBoard.value?.let {
                gameId.value = sudokuGame.getCurrentGameId()
                gameId.value?.let {
                    val game = DbSudokuGame(
                        id = gameId.value!!,
                        difficulty = gameDifficulty.value!!,
                        mistakes = mistakes.value!!,
                        hints = hints.value!!,
                        currentBoard = gameBoard.value!!,
                        solutionBoard = solutionBoard.value!!,
                        originalBoard = originalBoard.value!!,
                        time = timer.value!!,
                        isCompleted = isCompleted,
                        isSucceed = isSucceed
                    )
                    sudokuGame.updateCurrentGame(game)
                    if (isSucceed && isCompleted) {
                        gameBoard.value = null
                    }
                }
            }
        }
    }


    fun getGameStatistic(difficulty: String) {
        val percentFormat = DecimalFormat("##%")
        viewModelScope.launch {

            gameStatistic.value =
                GameStatistics(
                    sudokuGame.getTotalGame(difficulty).toString(),
                    sudokuGame.getTotalWin(difficulty).toString(),
                    percentFormat.format(sudokuGame.getWinRate(difficulty)),
                    sudokuGame.getBestTime(difficulty).formatToTimeString(),
                    sudokuGame.getAvgTime(difficulty).formatToTimeString()
                )


        }


    }


    fun gameOver(): Boolean {
        if (mistakes.value!! == 3) {
            gameBoard.value = null
            return true
        }
        return false
    }

    fun moreHints(): Boolean {
        if (hints.value!! <= 0) {
            return true
        }
        return false
    }

    fun restartBoard() {
        selectedCell.postValue(Cell(selectedRow, selectedCol, 0))
        gameBoard.postValue(sudokuGame.resetBoard())


    }


    fun undoBoard() {
        if (!undoStack.isEmpty()) {
            val move = undoStack.pop()
            Log.d("viewmodel", "undoBoard: ${move.cell.toString()}, ${move.board.toString()}")
            selectedCell.value = (move.cell)
            gameBoard.postValue(move.board)
        }


    }


    fun isSelectedCellCorrect(): Boolean {
        selectedCell.value?.let {
            if (validPosition()) {
                if (selectedCell.value?.value == 0) {
                    return false
                } else if (!sudokuGame.isCellCorrect(
                        selectedCell.value!!
                    )
                ) {
                    mistakes.value = mistakes.value?.plus(1)
                    return false
                } else {
                    return true
                }
            }
        }
        return false
    }

    fun toggleNotes() {
        isNoteOn.value = !isNoteOn.value!!
        Log.d("TAG", "toggleNotes: ${isNoteOn.value}")
    }


    fun isBoardCompleted(): Boolean {
        gameBoard.value?.let {
            return sudokuGame.isBoardComplete(it)
        }
        return false
    }


    fun updateCellValue(num: Int) {
        if (isNoteOn.value!!) {

            gameBoard.postValue(sudokuGame.addNotesForCurrentCell(selectedCell.value!!, num))

        } else {
            selectedCell.postValue(Cell(selectedRow, selectedCol, num))
            gameBoard.postValue(sudokuGame.updateCurrentCell(selectedRow, selectedCol, num))

        }

    }

    fun clearCellValue() {

        selectedCell.postValue(Cell(selectedRow, selectedCol, 0))
        gameBoard.value = (sudokuGame.clearCell(selectedRow, selectedCol))
        Log.d("TAG", "clearCellValue: ${selectedCell.value?.notes}")


    }

    fun showCellHint() {
        if (validPosition()) {
            val tempBoard = sudokuGame.showCellHint(selectedRow, selectedCol)
            selectedCell.postValue(tempBoard[selectedRow][selectedCol].copy())
            gameBoard.postValue(tempBoard)
            hints.value = hints.value?.minus(1)

        }
    }


    fun updateSelectedCell(row: Int, col: Int) {
        if (validPosition()) {
            selectedRow = row
            selectedCol = col
            selectedCell.value = (gameBoard.value!![selectedRow][selectedCol].copy())
        }
    }

    private fun validPosition(): Boolean {
        return selectedRow != -1 && selectedCol != -1

    }


    fun setTimer(time: Long) {
        timer.value = time
    }

    fun setTimerToString(): String {
        return timer.value!!.formatToTimeString()


    }

}