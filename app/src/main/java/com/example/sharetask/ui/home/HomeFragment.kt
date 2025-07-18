package com.example.sharetask.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.sharetask.adapter.CategoryAdapter
import com.example.sharetask.adapter.FriendAdapter
import com.example.sharetask.databinding.FragmentHomeBinding
import com.google.firebase.auth.FirebaseAuth

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HomeViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.vm = viewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val user = FirebaseAuth.getInstance().currentUser
        viewModel.setUserName(user?.displayName ?: "User")

        // Setup Adapters
        binding.rvFriendlist.adapter = FriendAdapter(getSampleFriends())
        binding.rvDiscovery.adapter = CategoryAdapter(getSampleDiscovery())
        }

    fun refreshData() {
        // Simulate refreshing data
        binding.rvFriendlist.adapter = FriendAdapter(getSampleFriends())
        binding.rvDiscovery.adapter = CategoryAdapter(getSampleDiscovery())
    }

    private fun getSampleFriends(): List<String> {
        return listOf("Friend 1", "Friend 2", "Friend 3", "Friend 4", "Friend 5")
    }

    private fun getSampleDiscovery(): List<String> {
        return listOf("Discovery 1", "Discovery 2", "Discovery 3", "Discovery 4")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}