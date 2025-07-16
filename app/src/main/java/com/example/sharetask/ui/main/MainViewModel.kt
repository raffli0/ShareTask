package com.example.sharetask.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.sharetask.R

class MainViewModel : ViewModel() {

    // Menyimpan itemId dari BottomNavigation yang sedang aktif
    private val _selectedMenuId = MutableLiveData<Int>().apply {
        value = R.id.menu_home // default menu saat app dibuka
    }
    val selectedMenuId: LiveData<Int> get() = _selectedMenuId

    /**
     * Fungsi untuk mengganti menu yang dipilih
     */
    fun selectMenu(menuId: Int) {
        _selectedMenuId.value = menuId
    }
}
