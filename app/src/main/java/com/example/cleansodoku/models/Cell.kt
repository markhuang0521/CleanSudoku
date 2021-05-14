package com.example.cleansodoku.models

data class Cell(
    var row: Int,
    var col: Int,
    var value: Int = 0,
    var isStartingCell: Boolean = false,
    var notes: MutableSet<Int> = mutableSetOf()
) {
    fun clone2(): Cell {
        return Cell(this.row, this.col, this.value, this.isStartingCell, this.notes)
    }
}