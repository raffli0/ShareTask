package com.example.sharetask.ui.main

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.example.sharetask.R
import com.example.sharetask.databinding.ActivityMainBinding
import com.example.sharetask.ui.home.HomeFragment
import com.example.sharetask.ui.menu.ForumFragment
import com.example.sharetask.ui.menu.DetailQuestionFragment
import com.example.sharetask.ui.menu.NotificationFragment
import com.example.sharetask.ui.menu.ProfileFragment
import com.example.sharetask.ui.menu.UploadFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding // ViewBinding instance
    private val mainViewModel: MainViewModel by viewModels() // ViewModel untuk mengatur menu aktif
    private var activeFragment: Fragment? = null // Fragment yang sedang ditampilkan

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inflate layout pakai ViewBinding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.swipeRefresh.setOnRefreshListener {
            refreshActiveFragment()
        }

        // Perubahan menu yang dipilih dari ViewModel
        mainViewModel.selectedMenuId.observe(this, Observer { menuId ->
            val selectedFragment = when (menuId) {
                R.id.menu_home -> HomeFragment()
                R.id.menu_forum -> ForumFragment()
                R.id.menu_upload -> UploadFragment()
                R.id.menu_notification -> NotificationFragment()
                R.id.menu_profile -> ProfileFragment()
                else -> HomeFragment()
            }
            setFragment(selectedFragment) // Tampilkan fragment yang dipilih
        })

        // Listener BottomNav -> update menuId ke ViewModel
        binding.bottomNavigationBar.setOnItemSelectedListener {
            mainViewModel.selectMenu(it.itemId)
            true
        }

        // Jika activity baru pertama kali dibuat, pilih menu home
        if (savedInstanceState == null) {
            mainViewModel.selectMenu(R.id.menu_home)
        }

        // Atur jarak trigger refresh
        val density = resources.displayMetrics.density
        binding.swipeRefresh.setDistanceToTriggerSync((200 * density).toInt()) // 200dp
    }

    private fun setFragment(fragment: Fragment) {
        // Hindari reload fragment yang sama
        if (fragment::class == activeFragment?.javaClass) return

        // Atur animasi transisi
        val transaction = supportFragmentManager.beginTransaction()
//            .setCustomAnimations(
//                R.anim.slide_in_right,    // fragment baru masuk dari kanan
//                R.anim.slide_out_left,    // fragment lama keluar ke kiri
//                R.anim.slide_in_left,     // fragment masuk saat kembali (backstack)
//                R.anim.slide_out_right    // fragment keluar saat kembali
//            )
        
        // Tambahkan tag ke fragment untuk memudahkan pencarian nanti
        val tag = when (fragment) {
            is HomeFragment -> "home_fragment"
            is ForumFragment -> "forum_fragment"
            is UploadFragment -> "upload_fragment"
            is NotificationFragment -> "notification_fragment"
            is ProfileFragment -> "profile_fragment"
            else -> fragment.javaClass.simpleName
        }
        
        transaction.replace(R.id.fragment_container, fragment, tag) // Ganti fragment container dengan tag
            .addToBackStack(tag) // Tambahkan ke backstack dengan tag
            .commit()

        activeFragment = fragment  // Update fragment aktif
    }

    fun refreshActiveFragment() {
        // Cek fragment aktif dan panggil metode refresh jika ada
        when (activeFragment) {
            is HomeFragment -> (activeFragment as HomeFragment).refreshData()
            is ForumFragment -> (activeFragment as ForumFragment).refreshData()
            is UploadFragment -> (activeFragment as UploadFragment).refreshData()
            is NotificationFragment -> (activeFragment as NotificationFragment).refreshData()
            is ProfileFragment -> (activeFragment as ProfileFragment).refreshData()
            is DetailQuestionFragment -> (activeFragment as DetailQuestionFragment).refreshData()
        }
        binding.swipeRefresh.isRefreshing = false // Hentikan animasi refreshing
    }
}
