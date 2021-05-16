package com.example.cleansodoku

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.cleansodoku.titleScreen.TitleFragmentDirections
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private lateinit var navController: NavController


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        navController = findNavController(R.id.nav_host_fragment)
        // Setting Navigation Controller with the BottomNavigationView
        bottom_nav.setupWithNavController(navController)
        setupActionBarWithNavController(navController)
    }

//    override fun onNavigateUp(): Boolean {
//        var navController = findNavController(R.id.nav_host_fragment)
//
//        return navController.navigateUp() || super.onNavigateUp()
//    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.game_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                (nav_host_fragment as NavHostFragment).navController.popBackStack()
            }
            R.id.menu_theme -> {

            }



        }
        return super.onOptionsItemSelected(item)
    }


}