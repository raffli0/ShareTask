package com.example.sharetask.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.sharetask.R
import com.example.sharetask.adapter.UserSearchAdapter
import com.example.sharetask.data.model.User
import com.example.sharetask.databinding.DialogAddFriendBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AddFriendDialogFragment : DialogFragment() {

    private var _binding: DialogAddFriendBinding? = null
    private val binding get() = _binding!!
    private lateinit var userSearchAdapter: UserSearchAdapter
    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogAddFriendBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()
        setupListeners()
    }

    private fun setupUI() {
        // Set dialog width to match parent
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        // Setup RecyclerView and adapter
        userSearchAdapter = UserSearchAdapter { user ->
            addFriend(user)
        }
        binding.rvSearchResults.adapter = userSearchAdapter
    }

    private fun setupListeners() {
        // Search button click
        binding.btnSearch.setOnClickListener {
            performSearch()
        }

        // Search on keyboard action
        binding.etSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch()
                return@setOnEditorActionListener true
            }
            false
        }
    }

    private fun performSearch() {
        val query = binding.etSearch.text.toString().trim()
        if (query.isEmpty()) {
            binding.tilSearch.error = "Please enter a search term"
            return
        } else {
            binding.tilSearch.error = null
        }

        // Show loading
        showLoading(true)

        // Clear previous results
        userSearchAdapter.submitList(emptyList())
        binding.tvNoResults.visibility = View.GONE
        binding.tvSearchResultLabel.visibility = View.GONE
        binding.rvSearchResults.visibility = View.GONE

        // Search in Firestore
        searchUsers(query)
    }

    private fun searchUsers(query: String) {
        val usersRef = firestore.collection("users")
        val lowercaseQuery = query.lowercase()

        // First, check if we're already following these users
        firestore.collection("users")
            .document(currentUserId)
            .collection("following")
            .get()
            .addOnSuccessListener { followingSnapshot ->
                val followingIds = followingSnapshot.documents.mapNotNull { it.getString("uid") }

                // Now search for users by name, email, or NIM
                usersRef.get().addOnSuccessListener { snapshot ->
                    val searchResults = snapshot.documents.mapNotNull { doc ->
                        val user = doc.toObject(User::class.java)
                        val uid = doc.id

                        // Skip current user and users we're already following
                        if (user != null && uid != currentUserId && !followingIds.contains(uid)) {
                            // Check if user matches search query (case insensitive)
                            val nameMatch = user.name.lowercase().contains(lowercaseQuery)
                            val emailMatch = user.email.lowercase().contains(lowercaseQuery)
                            val nimMatch = user.nim?.lowercase()?.contains(lowercaseQuery) ?: false

                            if (nameMatch || emailMatch || nimMatch) {
                                user.copy(uid = uid) // Ensure UID is set correctly
                            } else null
                        } else null
                    }

                    showLoading(false)
                    if (searchResults.isEmpty()) {
                        showNoResults()
                    } else {
                        showResults(searchResults)
                    }
                }.addOnFailureListener { e ->
                    showLoading(false)
                    showError("Error searching users: ${e.message}")
                }
            }.addOnFailureListener { e ->
                showLoading(false)
                showError("Error checking following status: ${e.message}")
            }
    }

    private fun addFriend(user: User) {
        if (currentUserId.isEmpty()) {
            showError("You must be logged in to add friends")
            return
        }

        // Show loading state on button
        val position = userSearchAdapter.currentList.indexOf(user)
        if (position != -1) {
            val viewHolder = binding.rvSearchResults.findViewHolderForAdapterPosition(position) as? UserSearchAdapter.UserViewHolder
            viewHolder?.binding?.btnAddUser?.isEnabled = false
            viewHolder?.binding?.btnAddUser?.text = "Adding..."
        }

        // Gunakan batch untuk memastikan semua operasi berhasil atau gagal bersama
        val batch = firestore.batch()
        
        // Add to following collection
        val followingRef = firestore.collection("users")
            .document(currentUserId)
            .collection("following")
            .document(user.uid)

        val followingData = mapOf(
            "uid" to user.uid,
            "name" to user.name,
            "timestamp" to System.currentTimeMillis()
        )

        batch.set(followingRef, followingData)
        
        // Add to followers collection of the other user
        val followerRef = firestore.collection("users")
            .document(user.uid)
            .collection("followers")
            .document(currentUserId)

        val followerData = mapOf(
            "uid" to currentUserId,
            "timestamp" to System.currentTimeMillis()
        )
        
        batch.set(followerRef, followerData)
        
        // Update followingCount pada user saat ini
        val currentUserRef = firestore.collection("users").document(currentUserId)
        batch.update(currentUserRef, "followingCount", com.google.firebase.firestore.FieldValue.increment(1))
        
        // Update followersCount pada user yang ditambahkan
        val otherUserRef = firestore.collection("users").document(user.uid)
        batch.update(otherUserRef, "followersCount", com.google.firebase.firestore.FieldValue.increment(1))
        
        // Commit batch
        batch.commit()
            .addOnSuccessListener {
                // Update UI
                Toast.makeText(context, "Added ${user.name} as friend", Toast.LENGTH_SHORT).show()
                
                // Remove user from search results
                val currentList = userSearchAdapter.currentList.toMutableList()
                currentList.remove(user)
                userSearchAdapter.submitList(currentList)
                
                // Show no results if list is now empty
                if (currentList.isEmpty()) {
                    showNoResults()
                }
                
                // Notify HomeFragment to refresh friend list
                (parentFragment as? HomeFragment)?.refreshData()
            }
            .addOnFailureListener { e ->
                showError("Error adding friend: ${e.message}")
                
                // Reset button state
                if (position != -1) {
                    val viewHolder = binding.rvSearchResults.findViewHolderForAdapterPosition(position) as? UserSearchAdapter.UserViewHolder
                    viewHolder?.binding?.btnAddUser?.isEnabled = true
                    viewHolder?.binding?.btnAddUser?.text = "Add"
                }
            }
            .addOnFailureListener { e ->
                showError("Error adding friend: ${e.message}")
                
                // Reset button state
                if (position != -1) {
                    val viewHolder = binding.rvSearchResults.findViewHolderForAdapterPosition(position) as? UserSearchAdapter.UserViewHolder
                    viewHolder?.binding?.btnAddUser?.isEnabled = true
                    viewHolder?.binding?.btnAddUser?.text = "Add"
                }
            }
    }

    private fun showResults(users: List<User>) {
        binding.tvSearchResultLabel.visibility = View.VISIBLE
        binding.rvSearchResults.visibility = View.VISIBLE
        userSearchAdapter.submitList(users)
    }

    private fun showNoResults() {
        binding.tvSearchResultLabel.visibility = View.VISIBLE
        binding.tvNoResults.visibility = View.VISIBLE
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnSearch.isEnabled = !isLoading
    }

    private fun showError(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}