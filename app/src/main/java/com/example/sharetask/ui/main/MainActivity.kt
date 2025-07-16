package com.example.sharetask.ui.main

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.sharetask.R
import com.example.sharetask.databinding.ActivityMainBinding
import com.example.sharetask.ui.home.HomeFragment
import com.example.sharetask.ui.menu.BookmarkFragment
import com.example.sharetask.ui.menu.NotificationFragment
import com.example.sharetask.ui.menu.ProfileFragment
import com.example.sharetask.ui.menu.UploadFragment
import com.example.sharetask.ui.main.MainViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.bottomNavigationBar

        val navController = findNavController(R.id.bottom_navigation_bar)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home, R.id.navigation_profile, R.id.navigation_upload
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }

//    private lateinit var binding: ActivityMainBinding
//    private val mainViewModel: MainViewModel by viewModels()
//
//    private var activeFragment: Fragment? = null
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//
//        // Inflate layout pakai ViewBinding
//        binding = ActivityMainBinding.inflate(layoutInflater)
//        setContentView(binding.root)
//
//        // Observe selectedMenuId dari ViewModel
//        mainViewModel.selectedMenuId.observe(this, Observer { menuId ->
//            val selectedFragment = when (menuId) {
//                R.id.menu_home -> HomeFragment()
//                R.id.menu_bookmark -> BookmarkFragment()
//                R.id.menu_upload -> UploadFragment()
//                R.id.menu_notification -> NotificationFragment()
//                R.id.menu_profile -> ProfileFragment()
//                else -> HomeFragment()
//            }
//            setFragment(selectedFragment)
//        })
//
//        // Listener BottomNav -> update menuId ke ViewModel
//        binding.bottomNavigationBar.setOnItemSelectedListener {
//            mainViewModel.selectMenu(it.itemId)
//            true
//        }
//
//        // Set default menu saat pertama buka
//        if (savedInstanceState == null) {
//            mainViewModel.selectMenu(R.id.menu_home)
//        }
//    }
//
//    private fun setFragment(fragment: Fragment) {
//        if (fragment::class == activeFragment?.javaClass) return
//
//        supportFragmentManager.beginTransaction()
//            .replace(R.id.fragment_container, fragment)
//            .commit()
//
//        activeFragment = fragment
//    }
}
