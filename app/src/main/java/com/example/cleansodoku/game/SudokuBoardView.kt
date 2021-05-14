package com.example.cleansodoku.game

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import com.example.cleansodoku.R
import com.example.cleansodoku.models.Cell

class SudokuBoardView(context: Context, attributes: AttributeSet) : View(context, attributes) {

    private var listener: OnTouchListener? = null

    private var gameBoard: Array<Array<Cell>>? = null

    // for cell highlights
//    private var selectedCell.row = -1
//    private var selectedCell.col = -1
    private var selectedCell: Cell = Cell(-1, -1)

    // size of one square from number 1-9
    private val squareLength = 3
    private val squareSize = 9

    // individual size for each cell
    private var cellSize = 0F
    private var noteCellSize = 0F
    private var isSelectedCellCorrect = true

    // paint for the outer border
    private val thickLinePaint = Paint().apply {
        style = Paint.Style.STROKE
        color = ContextCompat.getColor(context, R.color.black)
        strokeWidth = 4F
    }
    private val borderPaint = Paint().apply {
        val density = resources.displayMetrics.density

        style = Paint.Style.STROKE
        color = ContextCompat.getColor(context, R.color.black)
        strokeWidth = 4F * density
    }

    //paint for cell border
    private val thinLinePaint = Paint().apply {
        style = Paint.Style.STROKE
        color = ContextCompat.getColor(context, R.color.black)
        strokeWidth = 2F
    }

    private val selectedCellPaint = Paint().apply {
        style = Paint.Style.FILL_AND_STROKE
        color = ContextCompat.getColor(context, R.color.light_blue)
    }
    private val highlightedCellPaint = Paint().apply {
        style = Paint.Style.FILL_AND_STROKE
        color = ContextCompat.getColor(context, R.color.transparent)
    }
    private val darkerHighlightedCellPaint = Paint().apply {
        style = Paint.Style.FILL_AND_STROKE
        color = ContextCompat.getColor(context, R.color.gray)
    }

    private val textPaint = Paint().apply {
        style = Paint.Style.FILL_AND_STROKE
        color = ContextCompat.getColor(context, R.color.darker_blue)
        textSize = 64F
        typeface = Typeface.DEFAULT_BOLD
    }
    private val noteTextPaint = Paint().apply {
        style = Paint.Style.FILL_AND_STROKE
        color = ContextCompat.getColor(context, R.color.black)
        textSize = 36F

    }
    private val startingCellTextPaint = Paint().apply {
        style = Paint.Style.FILL_AND_STROKE
        color = ContextCompat.getColor(context, R.color.black)
        textSize = 64F
    }
    private val confliectTextPaint = Paint().apply {
        style = Paint.Style.FILL_AND_STROKE
        color = ContextCompat.getColor(context, R.color.accent)
        textSize = 64F


    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {

        val sizePixels = Math.min(widthMeasureSpec, heightMeasureSpec)

        setMeasuredDimension(sizePixels, sizePixels)
    }


    override fun onDraw(canvas: Canvas) {
        cellSize = width / squareSize.toFloat()
        noteCellSize = cellSize / squareLength.toFloat()


        fillCells(canvas)

        drawBoard(canvas)

        drawNumber(canvas)

    }

    private fun drawNumber(canvas: Canvas) {
        gameBoard?.let {

            for (array in gameBoard!!) {
                for (cell in array) {
                    if (cell.value == 0 && cell.notes.isNotEmpty()) {
                        // draw notes
                        cell.notes.forEach { note ->
                            val rowInCell = (note - 1) / squareLength
                            val colInCell = (note - 1) % squareLength
                            val valueString = note.toString()

                            val textBounds = Rect()
                            noteTextPaint.getTextBounds(
                                valueString,
                                0,
                                valueString.length,
                                textBounds
                            )
                            val textWidth = noteTextPaint.measureText(valueString)
                            val textHeight = textBounds.height()
                            canvas.drawText(
                                valueString,
                                ((cell.col * cellSize) + (colInCell * noteCellSize) + noteCellSize / 2f - textWidth / 2f),
                                ((cell.row * cellSize) + (rowInCell * noteCellSize) + noteCellSize / 2f + textHeight / 2f),
                                noteTextPaint
                            )
                        }
                    } else {
                        val row = cell.row
                        val col = cell.col
                        val valueString = cell.value.toString()
                        val textBounds = Rect()
                        val paintToUse =
                            if (cell.isStartingCell) startingCellTextPaint else textPaint

                        paintToUse.getTextBounds(valueString, 0, valueString.length, textBounds)
                        val textWidth = textPaint.measureText(valueString)
                        val textHeight = textBounds.height()


                        if (cell.value != 0) {
                            canvas.drawText(
                                valueString, (col * cellSize) + cellSize / 2 - textWidth / 2,
                                (row * cellSize) + cellSize / 2 - textHeight / 2 + 50, paintToUse
                            )
                            if (!isSelectedCellCorrect && !cell.isStartingCell && col == selectedCell.col && row == selectedCell.row) {
                                canvas.drawText(
                                    valueString,
                                    (col * cellSize) + cellSize / 2 - textWidth / 2,
                                    (row * cellSize) + cellSize / 2 - textHeight / 2 + 50,
                                    confliectTextPaint
                                )
                            }

                        }


                    }
                }
            }

        }
    }


    private fun fillCells(canvas: Canvas) {
        gameBoard?.let {
            for (array in gameBoard!!) {
                for (cell in array) {
                    val row = cell.row
                    val col = cell.col
//                    if (cell.isStartingCell) {
//                        fillCell(canvas, row, col, startingCellPaint)
//                    }

                    // hightligh current cell
                    if (row == selectedCell.row && col == selectedCell.col) {
                        fillCell(canvas, row, col, selectedCellPaint)
                    }
                    //hightligh vertical and horizontal
                    else if (row == selectedCell.row || col == selectedCell.col) {
                        fillCell(canvas, row, col, highlightedCellPaint)

                    }
                    // hightligh 3*3 box cells
                    else if (row / squareLength == selectedCell.row / squareLength && col / squareLength == selectedCell.col / squareLength) {
                        fillCell(canvas, row, col, highlightedCellPaint)
                    }
                    // hightligh same number
                    else if (cell.value != 0 && cell.value == selectedCell.value) {
                        fillCell(canvas, row, col, darkerHighlightedCellPaint)

                    }
                }
            }
        }

    }

    private fun fillCell(canvas: Canvas, row: Int, col: Int, paint: Paint) {
        canvas.drawRect(
            col * cellSize,
            row * cellSize,
            (col + 1) * cellSize,
            (row + 1) * cellSize,
            paint
        )


    }

    private fun drawBoard(canvas: Canvas) {
        canvas.drawRect(0F, 0F, width.toFloat(), height.toFloat(), borderPaint)


        for (i in 1 until squareSize) {
            // which paint to user
            val paintToUse = when (i % squareLength) {
                0 -> thickLinePaint
                else -> thinLinePaint
            }
            // horizontal lines
            canvas.drawLine(
                i * cellSize,
                0F,
                i * cellSize,
                height.toFloat(),
                paintToUse
            )
            // vertical lines
            canvas.drawLine(
                0F,
                i * cellSize,
                width.toFloat(),
                i * cellSize,
                paintToUse
            )

        }


    }


    override fun onTouchEvent(event: MotionEvent): Boolean {
        return when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                handleTouchEvent(event.x, event.y)
                true
            }
            else -> false
        }
    }

    private fun handleTouchEvent(x: Float, y: Float) {
        selectedCell.row = (y / cellSize).toInt()
        selectedCell.col = (x / cellSize).toInt()
        listener?.onCellTouched(selectedCell.row, selectedCell.col)
//        invalidate()


    }

    fun updateSelectedCellUI(cell: Cell) {
        selectedCell = cell
        invalidate()
    }

    fun updateBoard(board: Array<Array<Cell>>, isCorrect: Boolean) {
        this.gameBoard = board
        this.isSelectedCellCorrect = isCorrect
        invalidate()
    }

    fun setBoardTouchListener(listener: OnTouchListener) {
        this.listener = listener
    }

    interface OnTouchListener {
        fun onCellTouched(row: Int, col: Int)
    }


}