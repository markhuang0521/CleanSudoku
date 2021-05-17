package com.cleanSudoku.cleansodoku

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.cleanSudoku.cleansodoku.database.SudoKuDatabase
import com.cleanSudoku.cleansodoku.game.SudokuViewModel
import com.cleanSudoku.cleansodoku.models.SudokuGame
import com.cleanSudoku.cleansodoku.settings.Setting
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.dsl.module
import timber.log.Timber

class SudokuApp : Application() {
    private val setting: Setting by inject()

    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())

        /**
         * use Koin Library as a service locator
         */
        val myModule = module {
            //Declare a ViewModel - be later inject into Fragment with dedicated injector using by viewModel()
            single {
                SudokuViewModel(
                    get(),
                    get()
                )
            }
            //Declare singleton definitions to be later injected using by inject()
            single { SudokuGame(get()) }
            single { SudoKuDatabase.getInstance(this@SudokuApp).sudokuDao }
            single { Setting(get()) }
        }

        startKoin {
            androidContext(this@SudokuApp)
            modules(listOf(myModule))
        }
        setUpDarkMode()


    }

    private fun setUpDarkMode() {

        AppCompatDelegate.setDefaultNightMode(
            if (setting.darkMode) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO

        )
    }
}