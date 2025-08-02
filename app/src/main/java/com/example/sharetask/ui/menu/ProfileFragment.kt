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

    private var userId: String? = null
    private var isFriend: Boolean = false
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Ambil userId dan isFriend dari argumen jika ada
        arguments?.let {
            userId = it.getString("userId")
            isFriend = it.getBoolean("isFriend", false)
        }
        
        // Jika userId null, gunakan user yang sedang login
        if (userId == null) {
            userId = FirebaseAuth.getInstance().currentUser?.uid
        }
        
        // Load data user berdasarkan userId
        loadUserData()

        val requestOptions = RequestOptions()
            .placeholder(R.drawable.ic_profile) //load photo profile from drawable
            .error(R.drawable.ic_profile)

        // Tampilkan atau sembunyikan elemen UI berdasarkan apakah ini profil sendiri atau teman
        if (userId == FirebaseAuth.getInstance().currentUser?.uid) {
            // Ini adalah profil sendiri - tampilkan UI untuk pemilik profil
            binding.tvAccountTitle.visibility = View.VISIBLE
            binding.ivSettings.visibility = View.VISIBLE
            binding.ivInfo.visibility = View.VISIBLE
            binding.llOwnerButtons.visibility = View.VISIBLE
            binding.llVisitorButtons.visibility = View.GONE
            binding.tvLatestViewTitle.visibility = View.VISIBLE
            binding.tvViewAllLatestPlay.visibility = View.VISIBLE
            binding.rvLatestView.visibility = View.VISIBLE
            
            binding.ivSettings.setOnClickListener {
                val fragment = ProfileSettingsFragment()
                requireActivity()
                    .supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack("profile_fragment")
                    .commit()
            }

            binding.btnEditProfile.setOnClickListener {
                val fragment = EditProfileFragment()
                requireActivity()
                    .supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack("profile_fragment")
                    .commit()
            }
            
            // Setup Latest View RecyclerView hanya untuk profil sendiri
            setupLatestView()
        } else {
            // Ini adalah profil teman - tampilkan UI untuk pengunjung
            binding.tvAccountTitle.visibility = View.GONE
            binding.ivSettings.visibility = View.GONE
            binding.ivInfo.visibility = View.GONE
            binding.llOwnerButtons.visibility = View.GONE
            binding.llVisitorButtons.visibility = View.VISIBLE
            binding.btnFollowFriend.visibility = View.VISIBLE
            binding.btnMessagesVisitor.visibility = View.VISIBLE
            binding.tvLatestViewTitle.visibility = View.GONE
            binding.tvViewAllLatestPlay.visibility = View.GONE
            binding.rvLatestView.visibility = View.GONE
            binding.tvEmptyLatestView.visibility = View.GONE
            
            // Set teks tombol berdasarkan status pertemanan
            if (isFriend) {
                binding.btnFollowFriend.text = "Unfollow"
            } else {
                binding.btnFollowFriend.text = "Follow"
            }
            
            // Set listener untuk tombol follow/unfollow
            binding.btnFollowFriend.setOnClickListener {
                if (isFriend) {
                    unfollowFriend()
                } else {
                    followFriend()
                }
            }
            
            // Set listener untuk tombol messages
            binding.btnMessagesVisitor.setOnClickListener {
                // Implementasi chat dengan teman akan ditambahkan nanti
                Toast.makeText(context, "Chat feature coming soon", Toast.LENGTH_SHORT).show()
            }
        }
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
                binding.tvEmptyLatestView.visibility = View.VISIBLE
                binding.rvLatestView.visibility = View.GONE
            } else {
                binding.tvEmptyLatestView.visibility = View.GONE
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
        // Hanya setup latest view jika ini adalah profil sendiri
        if (userId == FirebaseAuth.getInstance().currentUser?.uid) {
            setupLatestView()
        }
    }
    
    private fun loadUserData() {
        // Ambil data dari Firestore untuk memastikan data terbaru
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        if (currentUserId != null && userId != null) {
            FirebaseFirestore.getInstance().collection("users").document(userId!!)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        // Update UI dengan data terbaru dari Firestore
                        val name = document.getString("name")
                        val profilePicUrl = document.getString("profilePic")
                        val role = document.getString("role") ?: "Beginner"
                        val followingCount = document.getLong("followingCount")?.toInt() ?: 0
                        val followersCount = document.getLong("followersCount")?.toInt() ?: 0
                        
                        // Update nama dan foto profil
                        binding.tvUserName.text = name ?: "User"
                        binding.chipRoleStatus.text = role
                        
                        // Ambil jumlah jawaban dari koleksi answers
                        getAnswerCount(userId!!) { answerCount ->
                            // Update statistik
                            updateStatistics(answerCount, followingCount, followersCount)
                        }
                        
                        Glide.with(this)
                            .load(profilePicUrl)
                            .apply(RequestOptions()
                                .placeholder(R.drawable.ic_profile)
                                .error(R.drawable.ic_profile))
                            .into(binding.ivProfilePicture)
                            
                        // Jika ini adalah profil teman, periksa status pertemanan
                        if (userId != currentUserId) {
                            checkFriendshipStatus()
                        }
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(context, "Failed to load profile: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
    
    private fun getAnswerCount(userId: String, callback: (Int) -> Unit) {
        FirebaseFirestore.getInstance().collection("answers")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { documents ->
                callback(documents.size())
            }
            .addOnFailureListener { e ->
                callback(0)
                Toast.makeText(context, "Failed to load answer count: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
    
    private fun updateStatistics(answerCount: Int, followingCount: Int, followersCount: Int) {
        // Dapatkan semua TextView dalam LinearLayout stats
        val statsLayout = binding.llStats
        
        // Update Answer count menggunakan ID yang sudah ditambahkan
        binding.tvAnswerCount.text = answerCount.toString()
        
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
    
    private fun checkFriendshipStatus() {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        if (currentUserId != null && userId != null) {
            // Periksa apakah user saat ini mengikuti user yang profilnya sedang dilihat
            FirebaseFirestore.getInstance().collection("users")
                .document(currentUserId)
                .collection("following")
                .document(userId!!)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        // User sudah mengikuti
                        isFriend = true
                        binding.btnFollowFriend.text = "Unfollow"
                    } else {
                        // User belum mengikuti
                        isFriend = false
                        binding.btnFollowFriend.text = "Follow"
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(context, "Failed to check friendship status: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
    
    private fun followFriend() {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        if (currentUserId != null && userId != null) {
            // Tampilkan loading
            binding.btnFollowFriend.isEnabled = false
            binding.btnFollowFriend.text = "Loading..."
            
            val db = FirebaseFirestore.getInstance()
            val batch = db.batch()
            
            // Tambahkan user yang dilihat ke koleksi following user saat ini
            val followingRef = db.collection("users").document(currentUserId).collection("following").document(userId!!)
            batch.set(followingRef, hashMapOf("timestamp" to System.currentTimeMillis()))
            
            // Tambahkan user saat ini ke koleksi followers user yang dilihat
            val followerRef = db.collection("users").document(userId!!).collection("followers").document(currentUserId)
            batch.set(followerRef, hashMapOf("timestamp" to System.currentTimeMillis()))
            
            // Update followingCount pada user saat ini
            val currentUserRef = db.collection("users").document(currentUserId)
            batch.update(currentUserRef, "followingCount", com.google.firebase.firestore.FieldValue.increment(1))
            
            // Update followersCount pada user yang dilihat
            val otherUserRef = db.collection("users").document(userId!!)
            batch.update(otherUserRef, "followersCount", com.google.firebase.firestore.FieldValue.increment(1))
            
            // Commit batch
            batch.commit()
                .addOnSuccessListener {
                    // Update UI
                    isFriend = true
                    binding.btnFollowFriend.isEnabled = true
                    binding.btnFollowFriend.text = "Unfollow"
                    Toast.makeText(context, "Successfully followed user", Toast.LENGTH_SHORT).show()
                    
                    // Refresh data untuk memperbarui statistik
                    loadUserData()
                }
                .addOnFailureListener { e ->
                    binding.btnFollowFriend.isEnabled = true
                    binding.btnFollowFriend.text = "Follow"
                    Toast.makeText(context, "Failed to follow user: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
    
    private fun unfollowFriend() {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        if (currentUserId != null && userId != null) {
            // Tampilkan loading
            binding.btnFollowFriend.isEnabled = false
            binding.btnFollowFriend.text = "Loading..."
            
            val db = FirebaseFirestore.getInstance()
            val batch = db.batch()
            
            // Hapus user yang dilihat dari koleksi following user saat ini
            val followingRef = db.collection("users").document(currentUserId).collection("following").document(userId!!)
            batch.delete(followingRef)
            
            // Hapus user saat ini dari koleksi followers user yang dilihat
            val followerRef = db.collection("users").document(userId!!).collection("followers").document(currentUserId)
            batch.delete(followerRef)
            
            // Update followingCount pada user saat ini
            val currentUserRef = db.collection("users").document(currentUserId)
            batch.update(currentUserRef, "followingCount", com.google.firebase.firestore.FieldValue.increment(-1))
            
            // Update followersCount pada user yang dilihat
            val otherUserRef = db.collection("users").document(userId!!)
            batch.update(otherUserRef, "followersCount", com.google.firebase.firestore.FieldValue.increment(-1))
            
            // Commit batch
            batch.commit()
                .addOnSuccessListener {
                    // Update UI
                    isFriend = false
                    binding.btnFollowFriend.isEnabled = true
                    binding.btnFollowFriend.text = "Follow"
                    Toast.makeText(context, "Successfully unfollowed user", Toast.LENGTH_SHORT).show()
                    
                    // Refresh data untuk memperbarui statistik
                    loadUserData()
                }
                .addOnFailureListener { e ->
                    binding.btnFollowFriend.isEnabled = true
                    binding.btnFollowFriend.text = "Unfollow"
                    Toast.makeText(context, "Failed to unfollow user: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}