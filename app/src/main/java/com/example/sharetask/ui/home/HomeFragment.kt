package com.example.sharetask.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.sharetask.R
import com.example.sharetask.adapter.FriendAdapter
import com.example.sharetask.data.model.Subject
import com.example.sharetask.databinding.FragmentHomeBinding
import com.example.sharetask.ui.menu.ForumFragment
import com.example.sharetask.adapter.QuestionAdapter
import com.example.sharetask.ui.menu.DetailQuestionFragment
import com.example.sharetask.ui.menu.UploadFragment
import com.example.sharetask.viewmodel.HomeViewModel
import com.google.android.material.chip.Chip
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HomeViewModel by viewModels()
    private var selectedSubject: Subject? = null
    private val friendAdapter = FriendAdapter()
    private val questionAdapter = QuestionAdapter { question ->
        val bundle = Bundle().apply {
            putString("questionId", question.id)
        }
        // Gunakan FragmentManager langsung karena MainActivity tidak menggunakan NavHost
        val fragment = DetailQuestionFragment().apply {
            arguments = bundle
        }
        requireActivity().supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragment_container, fragment, "detail_question_fragment")
            .addToBackStack("detail_question_fragment")
            .commit()
    }

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

        val currentUid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        viewModel.loadFriendList(currentUid)

        setupViews()
        observeViewModel()
        refreshData() // Load fresh data including user name from Firestore
    }

    private fun setupViews() {
        with(binding) {
            viewModel.loadQuestion() // Load question when fragment is created
            rvDiscovery.adapter = questionAdapter
            rvFriendlist.adapter = friendAdapter

            // Setup Categories
            setupCategories()

            // Share Now button
            btnShareNow.setOnClickListener {
                val fragment = UploadFragment()
                requireActivity().supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment, "upload_fragment")
                    .addToBackStack("upload_fragment")
                    .commit()
            }

            // View All button
            tvViewAll.setOnClickListener {
                val fragment = ForumFragment()
                requireActivity().supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment, "forum_fragment")
                    .addToBackStack("forum_fragment")
                    .commit()
            }

            btnAddFriendEmptyState.setOnClickListener {
                showAddFriendDialog()
            }
            btnAddFriendHeader.setOnClickListener {
                showAddFriendDialog()
            }
        }
    }

    private fun setupCategories() {
        binding.chipGroupCategory.apply {
            removeAllViews() // Clear existing chips
            
            // Set single selection to false to allow uncheck
            isSingleSelection = true
            setOnCheckedStateChangeListener { group, checkedIds ->
                if (checkedIds.isEmpty()) {
                    // No chip selected, show all items
                    selectedSubject = null
                    viewModel.filterQuestionBySubject("")
                }
            }
        }
        
        getSubjects().forEach { subject ->
            val chip = layoutInflater.inflate(
                R.layout.item_chip_choice,
                binding.chipGroupCategory,
                false
            ) as Chip

            chip.apply {
                text = subject.name
                id = View.generateViewId() // Generate unique ID for the chip
                
                setOnCheckedChangeListener { buttonView, isChecked ->
                    if (isChecked) {
                        selectedSubject = subject
                        viewModel.filterQuestionBySubject(subject.id)
                    } else if (binding.chipGroupCategory.checkedChipIds.isEmpty()) {
                        // If this was the last checked chip, show all items
                        selectedSubject = null
                        viewModel.filterQuestionBySubject("")
                    }
                }
            }

            binding.chipGroupCategory.addView(chip)
        }
    }

    private fun getSubjects(): List<Subject> {
        return listOf(
            Subject("1", "Computer Security", "CS"),
            Subject("2", "Mobile Development", "MOB"),
            Subject("3", "Web Development", "WEB"),
            Subject("4", "Artificial Intelligence", "AI"),
            Subject("5", "Architecture Computer", "ARC"),
            Subject("6", "Cyber Security", "SEC"),
            Subject("7", "Web Development", "WEB"),
            Subject("8", "Project Management", "PM")
        )
    }

    private fun observeViewModel() {
        // Observe question and limit to 5 items
        viewModel.question.observe(viewLifecycleOwner) { question ->
            // Submit the first 5 tasks to the adapter
            questionAdapter.submitList(question.take(5))
        }

        // Observe friend list and update UI accordingly
        viewModel.friendList.observe(viewLifecycleOwner) { friends ->
            if (friends.isNullOrEmpty()) {
                binding.rvFriendlist.visibility = View.GONE
                binding.emptyFriendListLayout.visibility = View.VISIBLE
                binding.btnAddFriendHeader.visibility = View.GONE // Sembunyikan juga tombol "+" di header
            } else {
                binding.rvFriendlist.visibility = View.VISIBLE
                binding.emptyFriendListLayout.visibility = View.GONE
                binding.btnAddFriendHeader.visibility = View.VISIBLE // Tampilkan tombol "+" di header
                friendAdapter.submitList(friends)
            }
        }
    }

    fun refreshData() {
        viewModel.loadQuestion()
        // Load user data from Firestore to update the name in UI
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            FirebaseFirestore.getInstance().collection("users")
                .document(currentUser.uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val name = document.getString("name") ?: ""
                        viewModel.setUserName(name)
                    }
                }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun getSampleFriends(): List<String> {
        return listOf("Friend 1", "Friend 2", "Friend 3", "Friend 4", "Friend 5")
    }
    
    private fun showAddFriendDialog() {
        val dialogFragment = AddFriendDialogFragment()
        dialogFragment.show(childFragmentManager, "AddFriendDialog")
    }
}