package com.example.cleansodoku.models

import com.example.cleansodoku.utils.Difficulty
import com.example.cleansodoku.utils.fullCopy
import com.example.cleansodoku.utils.randomNumGenerator
import kotlin.math.sqrt


class SudokuGenerator {


    // size of the board 9*9
    val boardWidth: Int = 9

    // using two dimensional array as sudoku board
    val board = Array(boardWidth) { IntArray(boardWidth) }
    var solution = Array(boardWidth) { IntArray(boardWidth) }

    private val sqrtWidth = sqrt(boardWidth.toDouble()).toInt()

    init {

        // Compute square root of N

    }

//    fun generate(difficulty: Difficulty): SudokuGenerator {
//        val sudoku = SudokuGenerator()
//        sudoku.generateBoard(difficulty)
//        return sudoku
//    }

    // Sudoku Generator
    private fun generateBoard(difficulty: Difficulty) {


        // Fill the diagonal of SRN x SRN matrices
        fillDiagonal()

        // Fill remaining blocks
        fillRemaining(0, sqrtWidth)

        // Remove Randomly K digits to make game
        removeKDigits(difficulty)
        printSudoku()


    }

    // Fill the diagonal SRN number of sqrtWidth x sqrtWidth matrices
    private fun fillDiagonal() {
        var i = 0
        while (i < boardWidth) {
            // for diagonal box, start coordinates->i==j
            fillBox(i, i)
            i += sqrtWidth
        }
    }


    private fun fillBox(row: Int, col: Int) {
        var curNumber: Int
        for (i in 0 until sqrtWidth) {
            for (j in 0 until sqrtWidth) {
                do {
                    curNumber = randomNumGenerator(boardWidth)
                } while (!unUsedInBox(row, col, curNumber))
                board[row + i][col + j] = curNumber
            }
        }

    }

    private fun fillRemaining(rowStart: Int, colStart: Int): Boolean {
        // System.out.println(i+" "+j);
        var row = rowStart
        var col = colStart
        if (col >= boardWidth && row < boardWidth - 1) {
            row += 1
            col = 0
        }
        if (row >= boardWidth && col >= boardWidth) return true
        if (row < sqrtWidth) {
            if (col < sqrtWidth) col = sqrtWidth
        } else if (row < boardWidth - sqrtWidth) {
            if (col == (row / sqrtWidth) * sqrtWidth) col += sqrtWidth
        } else {
            if (col == boardWidth - sqrtWidth) {
                row += 1
                col = 0
                if (row >= boardWidth) return true
            }
        }
        for (num in 1..boardWidth) {
            if (checkIfSafe(row, col, num)) {
                board[row][col] = num
                if (fillRemaining(row, col + 1)) return true
                board[row][col] = 0
            }
        }

        return false

    }


    private fun checkIfSafe(row: Int, col: Int, num: Int): Boolean {

        return (unUsedInRow(row, num) &&
                unUsedInCol(col, num) &&
                unUsedInBox(row - row % sqrtWidth, col - col % sqrtWidth, num));
    }

    // Returns false if given 3 x 3 block contains num.
    private fun unUsedInBox(rowStart: Int, colStart: Int, num: Int): Boolean {
        for (i in 0 until sqrtWidth) {
            for (j in 0 until sqrtWidth) {
                if (board[rowStart + i][colStart + j] == num) return false
            }
        }
        return true
    }

    // check in the row for existence
    private fun unUsedInRow(row: Int, num: Int): Boolean {
        for (col in 0 until boardWidth) if (board[row][col] == num) return false
        return true
    }

    // check in the row for existence
    private fun unUsedInCol(col: Int, num: Int): Boolean {
        for (row in 0 until boardWidth) if (board[row][col] == num) return false
        return true
    }


    private fun removeKDigits(difficulty: Difficulty) {
        // create a copy of the full board before remove for game play
        solution = board.fullCopy()

        var count = difficulty.count

        while (count != 0) {

            // cell position from 1-81
            val cellPosition = randomNumGenerator(boardWidth * boardWidth)

            // extract coordinates row  and col
            var row = cellPosition / 9
            var col = cellPosition % 9
            if (col != 0) {
                col -= 1
            } else {
                row -= 1
                col = 8
            }

            // delete cell will have default of 0
//            println("remove $count, position $cellPosition, row: $row  col:$col   num: ${board[row][col]}");

            if (board[row][col] != 0) {
                count--
                board[row][col] = 0
            }
        }
    }


    fun printSudoku() {
        for (i in 0 until boardWidth) {
            for (j in 0 until boardWidth) print(board[i][j].toString() + " ")
            println()
        }
        println()
        println("---------------------")
        for (i in 0 until boardWidth) {
            for (j in 0 until boardWidth) print(solution[i][j].toString() + " ")
            println()
        }
        println()
    }

    companion object {
        // static method to generate to sudoku
        fun generate(difficulty: Difficulty): SudokuGenerator {

            val sudoku = SudokuGenerator()
            sudoku.generateBoard(difficulty)
            return sudoku
        }
    }
}

fun boardToString(board: Array<IntArray>): String {
    var string = ""
    for (rowArr in board) {
        for (num in rowArr) {
            string += num.toString()
        }
    }
    return string

}


fun main() {
    val test = SudokuGenerator.generate(Difficulty.Easy)


//    println(boardToString(test.board))
//    println(boardToString(test.solution))


}



