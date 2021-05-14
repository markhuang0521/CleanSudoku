package com.example.cleansodoku.database

import androidx.room.TypeConverter
import com.example.cleansodoku.models.Cell
import com.squareup.moshi.FromJson
import com.squareup.moshi.Moshi
import com.squareup.moshi.ToJson
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory


class SudokuConverter {
    val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()
    private val cellsType = Types.newParameterizedType(List::class.java, Cell::class.java)
    private val cellAdapter = moshi.adapter<List<Cell>>(cellsType)

    @TypeConverter
    @FromJson
    fun fromJsonToBoardArray(json: String?): Array<Array<Cell>>? {
        val board: Array<Array<Cell>> = Array(9) { Array(9) { Cell(0, 0) } }
        json?.let {
            val cellList = cellAdapter.fromJson(json)

            for (i in cellList!!.indices) {

                board[i / 9][i % 9] = cellList[i]
            }
            return board
        }
        return null

    }

    @TypeConverter
    @ToJson
    fun boardArrayToJson(board: Array<Array<Cell>>): String {

        val cellList = mutableListOf<Cell>()

        for (arr in board) {
            for (cell in arr) {
                cellList.add(cell)
            }
        }
        return cellAdapter.toJson(cellList)
    }


}