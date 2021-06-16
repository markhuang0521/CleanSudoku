package com.cleanSudoku.cleansodoku.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.cleanSudoku.cleansodoku.models.Cell


@Entity(tableName = "tb_sudoku_game")

data class DbSudokuGame(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id") var id: Long = 0,
    @ColumnInfo(name = "difficulty") val difficulty: String,
    @ColumnInfo(name = "mistakes") var mistakes: Int,
    @ColumnInfo(name = "hints") var hints: Int,
    @ColumnInfo(name = "current_board") var currentBoard: Array<Array<Cell>>,
    @ColumnInfo(name = "solution_board") var solutionBoard: Array<Array<Cell>>,
    @ColumnInfo(name = "original_board") var originalBoard: Array<Array<Cell>>,
    @ColumnInfo(name = "total_time") var time: Long,
    @ColumnInfo(name = "completed") var isCompleted: Boolean,
    @ColumnInfo(name = "succeed") var isSucceed: Boolean

) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DbSudokuGame

        if (id != other.id) return false
        if (difficulty != other.difficulty) return false
        if (mistakes != other.mistakes) return false
        if (hints != other.hints) return false
        if (!currentBoard.contentDeepEquals(other.currentBoard)) return false
        if (!solutionBoard.contentDeepEquals(other.solutionBoard)) return false
        if (!originalBoard.contentDeepEquals(other.originalBoard)) return false
        if (time != other.time) return false
        if (isCompleted != other.isCompleted) return false
        if (isSucceed != other.isSucceed) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + difficulty.hashCode()
        result = 31 * result + mistakes
        result = 31 * result + hints
        result = 31 * result + currentBoard.contentDeepHashCode()
        result = 31 * result + solutionBoard.contentDeepHashCode()
        result = 31 * result + originalBoard.contentDeepHashCode()
        result = 31 * result + time.hashCode()
        result = 31 * result + isCompleted.hashCode()
        result = 31 * result + isSucceed.hashCode()
        return result
    }

}