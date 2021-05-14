package com.example.cleansodoku.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@TypeConverters(SudokuConverter::class)
@Database(entities = [DbSudokuGame::class], version = 5, exportSchema = false)
abstract class SudoKuDatabase : RoomDatabase() {

    abstract val sudokuDao: SudokuDao

    companion object {

        @Volatile
        private var INSTANCE: SudoKuDatabase? = null

        fun getInstance(context: Context): SudoKuDatabase {
            synchronized(this) {
                var instance = INSTANCE
                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        SudoKuDatabase::class.java,
                        "sudoku_database"
                    )
                        .fallbackToDestructiveMigration()
                        .build()

                    INSTANCE = instance
                }

                return instance
            }
        }

    }


}