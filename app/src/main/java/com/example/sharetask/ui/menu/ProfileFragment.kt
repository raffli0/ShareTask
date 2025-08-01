package com.example.sharetask.ui.menu

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.sharetask.R
import com.example.sharetask.adapter.LatestViewAdapter
import com.example.sharetask.utils.LatestViewManager
import com.example.sharetask.data.model.LatestView
import com.example.sharetask.databinding.FragmentProfileBinding
import com.example.sharetask.ui.auth.LoginActivity
import com.example.sharetask.ui.menu.DetailQuestionFragment
import com.example.sharetask.viewmodel.ProfileViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ProfileViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.vm = viewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadUserData()

        val requestOptions = RequestOptions()
            .placeholder(R.drawable.ic_profile) //load photo profile from drawable
            .error(R.drawable.ic_profile)

        binding.ivSettings.setOnClickListener {
            val fragment = ProfileSettingsFragment()
            requireActivity()
                .supportFragmentManager
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack("profile_fragment")
                .commit()
        }

        // Tombol logout dipindahkan ke ProfileSettingsFragment

        binding.btnEditProfile.setOnClickListener {
            val fragment = EditProfileFragment()
            requireActivity()
                .supportFragmentManager
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack("profile_fragment")
                .commit()
        }

        // Setup Latest View RecyclerView
        setupLatestView()
    }

    // Fungsi performLogout() dipindahkan ke ProfileSettingsFragment

    private fun setupLatestView() {
        // Dapatkan daftar Latest View dari LatestViewManager
        val latestViewManager = LatestViewManager.getInstance(requireContext())
        
        // Buat adapter dengan click listener
        val latestViewAdapter = LatestViewAdapter { latestView ->
            // Navigasi ke DetailQuestionFragment saat item diklik
            navigateToQuestionDetail(latestView)
        }
        
        // Set adapter ke RecyclerView
        binding.rvLatestView.adapter = latestViewAdapter
        
        // Observe LiveData dari LatestViewManager
        latestViewManager.latestViewList.observe(viewLifecycleOwner) { latestViews ->
            // Submit list ke adapter saat data berubah
            latestViewAdapter.submitList(latestViews)
            
            // Tampilkan atau sembunyikan pesan kosong
            if (latestViews.isEmpty()) {
                binding.tvEmptyLatestView?.visibility = View.VISIBLE
                binding.rvLatestView.visibility = View.GONE
            } else {
                binding.tvEmptyLatestView?.visibility = View.GONE
                binding.rvLatestView.visibility = View.VISIBLE
            }
        }
        
        // Muat ulang data terbaru
        latestViewManager.loadLatestViewList()
    }
    
    private fun navigateToQuestionDetail(latestView: LatestView) {
        val bundle = Bundle().apply {
            putString("questionId", latestView.questionId)
        }
        
        val fragment = DetailQuestionFragment().apply {
            arguments = bundle
        }
        
        requireActivity().supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragment_container, fragment, "detail_question_fragment")
            .addToBackStack("detail_question_fragment")
            .commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun refreshData() {
        loadUserData()
        setupLatestView()
    }
    
    private fun loadUserData() {
        // Ambil data dari Firestore untuk memastikan data terbaru
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        if (currentUserId != null) {
            FirebaseFirestore.getInstance().collection("users").document(currentUserId)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        // Update UI dengan data terbaru dari Firestore
                        val name = document.getString("name")
                        val profilePicUrl = document.getString("profilePic")
                        val role = document.getString("role") ?: "Beginner"
                        val followingCount = document.getLong("followingCount")?.toInt() ?: 0
                        val followersCount = document.getLong("followersCount")?.toInt() ?: 0
                        val rewardPoints = document.getLong("rewardPoints")?.toInt() ?: 0
                        
                        // Update nama dan foto profil
                        binding.tvUserName.text = name ?: "User"
                        binding.chipRoleStatus.text = role
                        
                        // Update statistik
                        updateStatistics(rewardPoints, followingCount, followersCount)
                        
                        Glide.with(this)
                            .load(profilePicUrl)
                            .apply(RequestOptions()
                                .placeholder(R.drawable.ic_profile)
                                .error(R.drawable.ic_profile))
                            .into(binding.ivProfilePicture)
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(context, "Failed to load profile: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
    
    private fun updateStatistics(rewardPoints: Int, followingCount: Int, followersCount: Int) {
        // Dapatkan semua TextView dalam LinearLayout stats
        val statsLayout = binding.llStats
        
        // Update Answer count (index 0)
        val answerLayout = statsLayout.getChildAt(0) as LinearLayout
        val answerCountTextView = answerLayout.getChildAt(0) as TextView
        answerCountTextView.text = rewardPoints.toString()
        
        // Update Following count (index 1)
        val followingLayout = statsLayout.getChildAt(1) as LinearLayout
        val followingCountTextView = followingLayout.getChildAt(0) as TextView
        followingCountTextView.text = followingCount.toString()
        
        // Update Followers count (index 2)
        val followersLayout = statsLayout.getChildAt(2) as LinearLayout
        val followersCountTextView = followersLayout.getChildAt(0) as TextView
        followersCountTextView.text = followersCount.toString()
        
        // Friends count tetap menggunakan nilai default untuk saat ini
    }
}