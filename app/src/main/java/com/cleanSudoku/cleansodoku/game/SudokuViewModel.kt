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
import com.cleanSudoku.cleansodoku.models.GameStatistics
import com.cleanSudoku.cleansodoku.models.Move
import com.cleanSudoku.cleansodoku.models.SudokuGameRepository
import com.cleanSudoku.cleansodoku.settings.Setting
import com.cleanSudoku.cleansodoku.util.Difficulty
import com.cleanSudoku.cleansodoku.util.formatToTimeString
import kotlinx.coroutines.launch
import org.koin.core.KoinComponent
import org.koin.core.inject
import timber.log.Timber
import java.util.*


class SudokuViewModel(
    val app: Application,
    private val sudokuGameRepository: SudokuGameRepository
) : ViewModel(),
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
//            sudokuGameRepository.deleteAll()
//
//        }
    }

    fun checkAndCompleteCurrentGame() {
        gameId?.let {
            viewModelScope.launch {
                val currentGame = sudokuGameRepository.checkCurrentGame()
                if (currentGame != null) {

                    currentGame.isSucceed = false
                    currentGame.isCompleted = true
                    sudokuGameRepository.saveCurrentGame(currentGame)
                }
            }
        }
    }

    fun startNewGame(difficulty: Difficulty) {
        checkAndCompleteCurrentGame()

        timer.value = 0
        hints.value = 1
        mistakes.value = 0
        gameDifficulty.value = difficulty.name
        viewModelScope.launch {

            gameBoard.value = sudokuGameRepository.generateBoard(difficulty)
            solutionBoard.value = sudokuGameRepository.solution
            originalBoard.value = sudokuGameRepository.originalBoard
            val dbSudokuGame = DbSudokuGame(
                difficulty = gameDifficulty.value!!,
                mistakes = mistakes.value!!,
                hints = hints.value!!,
                currentBoard = gameBoard.value!!,
                solutionBoard = solutionBoard.value!!,
                originalBoard = originalBoard.value!!,
                time = timer.value!!,
                isCompleted = false,
                isSucceed = false
            )

            sudokuGameRepository.saveCurrentGame(dbSudokuGame)
            gameId.value = sudokuGameRepository.getCurrentGameId()

        }
    }

    fun loadGame() {
        viewModelScope.launch {
            val currentGame = sudokuGameRepository.loadCurrentGame()
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

    fun updateCurrentGame(isCompleted: Boolean, isSucceed: Boolean) {
        viewModelScope.launch {
            gameBoard.value?.let {
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
                    sudokuGameRepository.updateCurrentGame(game)
                    if (isCompleted || isSucceed) {
                        gameId.value = null
                    }
                }
            }
        }
    }


    fun getGameStatistic(difficulty: String) {
        viewModelScope.launch {
            gameStatistic.value = sudokuGameRepository.getGameStatistic(difficulty)
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
        gameBoard.postValue(sudokuGameRepository.resetBoard())

    }


//    fun undoBoard() {
//        if (!undoStack.isEmpty()) {
//            val move = undoStack.pop()
//            selectedCell.value = (move.cell)
//            gameBoard.postValue(move.board)
//        }
//    }


    fun isSelectedCellCorrect(): Boolean {
        selectedCell.value?.let {
            if (validPosition()) {


                if (selectedCell.value?.value == 0 || selectedCell.value == null) {
                    return false
                } else if (!sudokuGameRepository.isCellCorrect(selectedCell.value!!)
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
        playButtonSoundAndVibrate(buttonClickSound)
        isNoteOn.value = !isNoteOn.value!!
    }


    fun isBoardCompleted(): Boolean {
        gameBoard.value?.let {
            return sudokuGameRepository.isBoardComplete(it)
        }
        return false
    }


    fun updateCellValue(num: Int) {
        playButtonSoundAndVibrate(buttonClickSound)
        if (isNoteOn.value!!) {

            gameBoard.postValue(
                sudokuGameRepository.addNotesForCurrentCell(
                    selectedCell.value!!,
                    num
                )
            )

        } else {
            selectedCell.postValue(Cell(selectedRow, selectedCol, num))
            gameBoard.postValue(
                sudokuGameRepository.updateCurrentCell(
                    selectedRow,
                    selectedCol,
                    num
                )
            )

        }

    }

    fun clearCellValue() {
        playButtonSoundAndVibrate(buttonClickSound)
        selectedCell.value = (Cell(selectedRow, selectedCol, 0))
        gameBoard.value = (sudokuGameRepository.clearCell(selectedRow, selectedCol))


    }

    fun showCellHint() {
        playButtonSoundAndVibrate(hintClickSound)
        if (validPosition()) {
            val tempBoard = sudokuGameRepository.showCellHint(selectedRow, selectedCol)
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