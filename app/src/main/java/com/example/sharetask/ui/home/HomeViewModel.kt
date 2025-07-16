package com.example.sharetask.ui.home

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class HomeViewModel : ViewModel() {

    val userName = MutableLiveData("")
    val bannerList = MutableLiveData<List<Int>>() // Drawable resources
    val categoryList = MutableLiveData<List<Category>>()
    val friendList = MutableLiveData<List<UserProfile>>()
    val discoveryList = MutableLiveData<List<DiscoveryItem>>()

    init {
        loadUserData()
        loadBanners()
        loadCategories()
        loadFriendList()
        loadDiscovery()
    }

    private fun loadUserData() {
        val user = FirebaseAuth.getInstance().currentUser
        userName.value = user?.displayName ?: user?.email?.substringBefore("@") ?: "User"
    }

    private fun loadBanners() {
        bannerList.value = listOf(
            R.drawable.banner1,
            R.drawable.banner2,
            R.drawable.banner3
        )
    }

    private fun loadCategories() {
        categoryList.value = listOf(
            Category("Animal Name"),
            Category("Challenge"),
            Category("Mini Quiz"),
            Category("Math"),
            Category("Science")
        )
    }

    private fun loadFriendList() {
        friendList.value = listOf(
            UserProfile("1", "Tina", ""),
            UserProfile("2", "Jake", ""),
            UserProfile("3", "Maya", "")
        )
    }

    private fun loadDiscovery() {
        discoveryList.value = listOf(
            DiscoveryItem("Practice composure", R.drawable.discovery1),
            DiscoveryItem("Train brain system", R.drawable.discovery2)
        )
    }
}
