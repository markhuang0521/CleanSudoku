package com.example.cleansodoku.utils

import android.content.Context
import android.os.Build
import android.os.Parcelable
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.cleansodoku.R
import com.example.cleansodoku.game.GameCompleteFragmentDirections
import com.example.cleansodoku.game.SudokuViewModel
import com.example.cleansodoku.models.Cell
import com.example.cleansodoku.titleScreen.TitleFragmentDirections
import kotlinx.android.parcel.Parcelize
import kotlinx.android.synthetic.main.activity_main.*
import java.util.concurrent.TimeUnit
import kotlin.math.floor

enum class FragmentTag {
    Title, GameBoard, GameComplete
}

enum class ReWardType {
    Hint, Mistake
}


@Parcelize
enum class Difficulty(val count: Int) : Parcelable {
    Easy(13), Medium(1), Hard(1), Insane(5)
}


fun Fragment.showDifficultyDialogAndStartNewGame(viewModel: SudokuViewModel, FragmentTag: String) {
    val difficulties = arrayOf(
        Difficulty.Easy.name,
        Difficulty.Medium.name,
        Difficulty.Hard.name,
        Difficulty.Insane.name
    )
    AlertDialog.Builder(requireContext())
        .setTitle(getString(R.string.new_game_dialog_title))
        .setItems(difficulties) { dialog, which ->

            when (which) {
                0 -> {
                    viewModel.startNewGame(Difficulty.Easy)
                    if (FragmentTag == com.example.cleansodoku.utils.FragmentTag.Title.name) {
                        findNavController().navigate(TitleFragmentDirections.actionTitleFragmentToGameFragment())

                    } else if (FragmentTag == com.example.cleansodoku.utils.FragmentTag.GameComplete.name) {
                        findNavController().navigate(GameCompleteFragmentDirections.actionGameCompleteFragmentToGameFragment())

                    }

                }
                1 -> {
                    viewModel.startNewGame(Difficulty.Medium)
                    if (FragmentTag == com.example.cleansodoku.utils.FragmentTag.Title.name) {
                        findNavController().navigate(TitleFragmentDirections.actionTitleFragmentToGameFragment())

                    } else if (FragmentTag == com.example.cleansodoku.utils.FragmentTag.GameComplete.name) {
                        findNavController().navigate(GameCompleteFragmentDirections.actionGameCompleteFragmentToGameFragment())

                    }
                }
                2 -> {
                    viewModel.startNewGame(Difficulty.Hard)
                    if (FragmentTag == com.example.cleansodoku.utils.FragmentTag.Title.name) {
                        findNavController().navigate(TitleFragmentDirections.actionTitleFragmentToGameFragment())

                    } else if (FragmentTag == com.example.cleansodoku.utils.FragmentTag.GameComplete.name) {
                        findNavController().navigate(GameCompleteFragmentDirections.actionGameCompleteFragmentToGameFragment())

                    }
                }
                3 -> {
                    viewModel.startNewGame(Difficulty.Insane)
                    if (FragmentTag == com.example.cleansodoku.utils.FragmentTag.Title.name) {
                        findNavController().navigate(TitleFragmentDirections.actionTitleFragmentToGameFragment())

                    } else if (FragmentTag == com.example.cleansodoku.utils.FragmentTag.GameComplete.name) {
                        findNavController().navigate(GameCompleteFragmentDirections.actionGameCompleteFragmentToGameFragment())

                    }
                }

            }
        }
        .show()

}

fun Fragment.showGameOverDialog(viewModel: SudokuViewModel) {
    val builder = AlertDialog.Builder(requireContext())
        .setTitle("Game Over")
        .setMessage("Opps!, you got 3 strikes and out!")
        .setPositiveButton("New Game") { dialog, i ->
            showDifficultyDialogAndStartNewGame(viewModel, FragmentTag.GameBoard.name)
        }
        .setNeutralButton("Back to Home") { dialogInterface, i ->
            findNavController().popBackStack()
        }

        .setCancelable(false)
        .show()
}

fun Long?.formatToTimeString(): String {

    return if (this != null) {
        String.format(
            "%02d:%02d:%02d",
            TimeUnit.MILLISECONDS.toHours(this),
            TimeUnit.MILLISECONDS.toMinutes(this) % TimeUnit.HOURS.toMinutes(1),
            TimeUnit.MILLISECONDS.toSeconds(this) % TimeUnit.MINUTES.toSeconds(1)
        )
    } else {
        "00:00::00"
    }

}

fun Fragment.vibratePhone() {
    val vibrator = context?.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    if (Build.VERSION.SDK_INT >= 26) {
        vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE))
    } else {
        vibrator.vibrate(200)
    }
}

fun Fragment.setToolbarTitle(title: String? = "") {
    if (activity is AppCompatActivity) {
        (activity as AppCompatActivity).supportActionBar?.title = title
    }
}

fun Fragment.removeBottomNav() {
    if (activity is AppCompatActivity) {
        (activity as AppCompatActivity).bottom_nav.visibility = View.GONE
    }
}

fun Fragment.showBottomNav() {
    if (activity is AppCompatActivity) {
        (activity as AppCompatActivity).bottom_nav.visibility = View.VISIBLE
    }
}

fun Fragment.removeToolbar() {
    if (activity is AppCompatActivity) {
        (activity as AppCompatActivity).supportActionBar?.hide()
    }
}

fun Fragment.showToolbar() {
    if (activity is AppCompatActivity) {
        (activity as AppCompatActivity).supportActionBar?.show()
    }
}

fun Fragment.setToolbar(toolbar: androidx.appcompat.widget.Toolbar) {
    if (activity is AppCompatActivity) {
        (activity as AppCompatActivity).setSupportActionBar(toolbar)
    }
}

fun Fragment.setDisplayHomeAsUpEnabled(bool: Boolean) {
    if (activity is AppCompatActivity) {
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(
            bool
        )
    }
}

fun randomNumGenerator(number: Int): Int {
    return floor(Math.random() * number + 1).toInt()
}

fun Array<IntArray>.fullCopy() = Array(size) { get(it).clone() }
fun Array<Array<Cell>>.fullCopy(): Array<Array<Cell>> {
    return Array(this.size) { row ->
        Array(this.size) { col ->
            // game board generation
            val cell = this[row][col]
            Cell(row, col, cell.value, cell.isStartingCell)

        }
    }
}

fun Array<Array<Cell>>.toValueString(): String {
    var string = StringBuilder()

    for (row in this) {
        for (cell in row) {
            string.append(cell.value)
        }
    }
    return string.toString()
}

fun String.toBoardArray() {
    Array(9) { row ->
        Array(9) { col ->

        }
    }
}

fun main() {
    var string = StringBuilder()
    string.append(12313123)
    println(string)
    val arr = Array(9) { Array(9) { Cell(0, 0) } }


//    val long = 3986L
//    println(long.formatToTimeString())
//
//    val arr = arrayOf(Cell(0, 0), Cell(1, 1), Cell(2, 2))
//    val a2 = arr.map { it.copy() }
//    arr[0].row = 99
//    arr[0].col = 99
//
//    println(arr[0].toString())
//    println(a2[0].toString())
}