package com.example.sharetask.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainViewModel : ViewModel() {

    // Menyimpan itemId dari BottomNavigation yang sedang aktif
    private val _selectedMenuId = MutableLiveData<Int>()

    // LiveData untuk di-observe di MainActivity agar UI tahu menu mana yang aktif
    val selectedMenuId: LiveData<Int> get() = _selectedMenuId

    // Fungsi untuk mengubah menu yang dipilih dari BottomNavigation
    fun selectMenu(menuId: Int) {
        _selectedMenuId.value = menuId
    }
}
