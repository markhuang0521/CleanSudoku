package com.cleanSudoku.cleansodoku.game

import android.app.Application
import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cleanSudoku.cleansodoku.R
import com.cleanSudoku.cleansodoku.database.DbSudokuGame
import com.cleanSudoku.cleansodoku.models.Cell
import com.cleanSudoku.cleansodoku.models.Move
import com.cleanSudoku.cleansodoku.models.SudokuGame
import com.cleanSudoku.cleansodoku.settings.Setting
import com.cleanSudoku.cleansodoku.statistics.GameStatistics
import com.cleanSudoku.cleansodoku.utils.Difficulty
import com.cleanSudoku.cleansodoku.utils.formatToTimeString
import kotlinx.coroutines.launch
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.text.DecimalFormat
import java.util.*


class SudokuViewModel(val app: Application, private val sudokuGame: SudokuGame) : ViewModel(),
    KoinComponent {

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

    var mediaPlayer: MediaPlayer? = null
    var soundPool: SoundPool
    var buttonClickSound: Int
    var hintClickSound: Int

    private val isNoteOn = MutableLiveData<Boolean>(false)
    private var selectedRow = 0
    private var selectedCol = 0
    private val undoStack: Stack<Move> = Stack()
    private val setting: Setting by inject()
    private var vibrator: Vibrator

    private fun playButtonSoundAndVibrate(soundId: Int) {
        if (setting.sound) {
            soundPool.play(soundId, 1F, 1F, 0, 0, 1F)
        }
        if (setting.vibration) {
            if (Build.VERSION.SDK_INT >= 26) {
                vibrator.vibrate(
                    VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE)
                )
            } else {
                vibrator.vibrate(200)
            }
        }
    }


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
        val audioAttributes = AudioAttributes.Builder()
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .setUsage(AudioAttributes.USAGE_GAME)
            .build()
        vibrator = app.applicationContext?.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        soundPool = SoundPool.Builder()
            .setAudioAttributes(audioAttributes)
            .build()
        buttonClickSound = soundPool.load(app.applicationContext, R.raw.click, 0)
        hintClickSound = soundPool.load(app.applicationContext, R.raw.hint_click, 0)
        mediaPlayer = MediaPlayer.create(app.applicationContext, R.raw.click)

//        viewModelScope.launch {
//            sudokuGame.deleteAll()
//
//        }


    }


    fun loadGame() {

        if (gameBoard.value == null) {
            viewModelScope.launch {
                val currentGame = sudokuGame.loadCurrentGame()
                currentGame?.let {
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
        if (mistakes.value!! >= 5) {
//            gameBoard.value = null
            return true
        }
        return false
    }

    fun moreHints(): Boolean {
        if (hints.value!! < 0) {
            return true
        }
        return false
    }

    fun restartBoard() {
        playButtonSoundAndVibrate(buttonClickSound)

        selectedCell.postValue(Cell(selectedRow, selectedCol, 0))
        gameBoard.postValue(sudokuGame.resetBoard())

    }


    fun undoBoard() {
        if (!undoStack.isEmpty()) {
            val move = undoStack.pop()
            selectedCell.value = (move.cell)
            gameBoard.postValue(move.board)
        }


    }


    fun isSelectedCellCorrect(): Boolean {
        selectedCell.value?.let {
            if (validPosition()) {


                if (selectedCell.value?.value == 0 || selectedCell.value == null) {
                    return false
                } else if (!sudokuGame.isCellCorrect(selectedCell.value!!)
                ) {
                    val b = !sudokuGame.isCellCorrect(selectedCell.value!!)

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
        playButtonSoundAndVibrate(buttonClickSound)
        isNoteOn.value = !isNoteOn.value!!
    }


    fun isBoardCompleted(): Boolean {
        gameBoard.value?.let {
            return sudokuGame.isBoardComplete(it)
        }
        return false
    }


    fun updateCellValue(num: Int) {
        playButtonSoundAndVibrate(buttonClickSound)
        if (isNoteOn.value!!) {

            gameBoard.postValue(sudokuGame.addNotesForCurrentCell(selectedCell.value!!, num))

        } else {
            selectedCell.postValue(Cell(selectedRow, selectedCol, num))
            gameBoard.postValue(sudokuGame.updateCurrentCell(selectedRow, selectedCol, num))

        }

    }

    fun clearCellValue() {
        playButtonSoundAndVibrate(buttonClickSound)
        selectedCell.value = (Cell(selectedRow, selectedCol, 0))
        gameBoard.value = (sudokuGame.clearCell(selectedRow, selectedCol))


    }

    fun showCellHint() {
        playButtonSoundAndVibrate(hintClickSound)
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
            gameBoard.value?.let {
                selectedCell.value = (gameBoard.value!![selectedRow][selectedCol].copy())

            }
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