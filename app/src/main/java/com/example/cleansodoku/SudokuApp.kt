package com.example.cleansodoku

import android.app.Application
import com.example.cleansodoku.database.SudoKuDatabase
import com.example.cleansodoku.game.SudokuViewModel
import com.example.cleansodoku.models.SudokuGame
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.dsl.module
import timber.log.Timber

class SudokuApp : Application() {

    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())


        /**
         * use Koin Library as a service locator
         */
        val myModule = module {
            //Declare a ViewModel - be later inject into Fragment with dedicated injector using by viewModel()
            single {
                SudokuViewModel(get())
            }
            //Declare singleton definitions to be later injected using by inject()
            single { SudokuGame(get()) }
            single { SudoKuDatabase.getInstance(this@SudokuApp).sudokuDao }
        }

        startKoin {
            androidContext(this@SudokuApp)
            modules(listOf(myModule))
        }

    }
}