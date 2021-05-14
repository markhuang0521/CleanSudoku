package com.example.cleansodoku.database

import androidx.room.*


@Dao
interface SudokuDao {

    // CRUD operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveCurrentGame(game: DbSudokuGame)

    @Query("SELECT* FROM tb_sudoku_game where  completed=0 ORDER BY id DESC limit 1")
    suspend fun getCurrentGame(): DbSudokuGame?

    @Update
    suspend fun updateGameByObject(game: DbSudokuGame)

    @Query("SELECT id FROM tb_sudoku_game where completed=0 ORDER BY id DESC limit 1")
    suspend fun getCurrentGameId(): Long


    @Query("SELECT* FROM tb_sudoku_game where  id=:id limit 1")
    suspend fun getCurrentGameById(id: Long): DbSudokuGame?

    // statics queries
    @Query("SELECT COUNT(*)FROM tb_sudoku_game where  difficulty=:difficulty ")
    suspend fun getTotalGame(difficulty: String): Int

    @Query("SELECT COUNT(*)FROM tb_sudoku_game where  difficulty=:difficulty and succeed=1")
    suspend fun getTotalWin(difficulty: String): Int

    @Query("SELECT total_time FROM tb_sudoku_game where  difficulty=:difficulty ORDER BY total_time  limit 1")
    suspend fun getBestTime(difficulty: String): Long?

    @Query("SELECT AVG(total_time)  FROM tb_sudoku_game where  difficulty=:difficulty ")
    suspend fun getAvgTime(difficulty: String): Long?

    @Query("DELETE  FROM tb_sudoku_game")
    suspend fun deleteAll()

    //    @Query("UPDATE tb_sudoku_game SET difficulty=:difficulty, current_board=:currentBoard, solution_board=:solutionBoard,total_time=:time,completed=:isCompleted,succeed=:isSucceed where id=:id")
//    suspend fun updateGameById(
//        id: Long,
//        difficulty: String,
//        currentBoard: Array<Array<Cell>>,
//        solutionBoard: Array<Array<Cell>>,
//        time: Long,
//        isCompleted: Boolean,
//        isSucceed: Boolean
//    )

}