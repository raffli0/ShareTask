package com.example.sharetask.ui.home

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.sharetask.R
import com.example.sharetask.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HomeViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.vm = viewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // Banner
        viewModel.bannerList.observe(viewLifecycleOwner) {
            binding.viewPagerBanner.adapter = BannerAdapter(it)
        }

        // Category
        viewModel.categoryList.observe(viewLifecycleOwner) {
            binding.rvCategory.adapter = CategoryAdapter(it)
        }

        // Friends
        viewModel.friendList.observe(viewLifecycleOwner) {
            binding.rvFriend.adapter = FriendAdapter(it)
        }

        // Discovery
        viewModel.discoveryList.observe(viewLifecycleOwner) {
            binding.rvDiscovery.adapter = DiscoveryAdapter(it)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}