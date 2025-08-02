package com.example.sharetask.ui.menu

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.sharetask.R
import com.example.sharetask.adapter.ChatAdapter
import com.example.sharetask.databinding.FragmentChatBinding
import com.example.sharetask.viewmodel.ChatViewModel

class ChatFragment : Fragment() {

    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ChatViewModel by viewModels()
    private lateinit var chatAdapter: ChatAdapter
    
    private var userId: String? = null
    private var userName: String? = null
    private var userProfilePic: String? = null

    companion object {
        fun newInstance(userId: String, userName: String, userProfilePic: String? = null): ChatFragment {
            val fragment = ChatFragment()
            val args = Bundle().apply {
                putString("userId", userId)
                putString("userName", userName)
                putString("userProfilePic", userProfilePic)
            }
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            userId = it.getString("userId")
            userName = it.getString("userName")
            userProfilePic = it.getString("userProfilePic")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupToolbar()
        setupRecyclerView()
        setupSendButton()
        observeViewModel()
        
        // Load chat partner info and messages
        userId?.let { partnerId ->
            viewModel.loadChatPartner(partnerId)
            viewModel.loadMessages(partnerId)
        }
    }
    
    private fun setupToolbar() {
        binding.tvChatPartnerName.text = userName ?: "Chat"
        
        // Load profile picture if available
        if (!userProfilePic.isNullOrEmpty()) {
            Glide.with(requireContext())
                .load(userProfilePic)
                .placeholder(R.drawable.ic_profile)
                .error(R.drawable.ic_profile)
                .into(binding.ivChatPartnerProfile)
        }
        
        binding.btnBack.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }
    }
    
    private fun setupRecyclerView() {
        chatAdapter = ChatAdapter()
        binding.rvMessages.apply {
            adapter = chatAdapter
            layoutManager = LinearLayoutManager(context).apply {
                stackFromEnd = true // Scroll to bottom
            }
        }
    }
    
    private fun setupSendButton() {
        binding.btnSend.setOnClickListener {
            val messageText = binding.etMessage.text.toString().trim()
            if (messageText.isNotEmpty() && userId != null) {
                viewModel.sendMessage(userId!!, messageText)
                binding.etMessage.text.clear()
            }
        }
    }
    
    private fun observeViewModel() {
        viewModel.messages.observe(viewLifecycleOwner) { messages ->
            chatAdapter.submitList(messages)
            if (messages.isNotEmpty()) {
                binding.rvMessages.scrollToPosition(messages.size - 1)
            }
        }
        
        viewModel.chatPartner.observe(viewLifecycleOwner) { user ->
            user?.let {
                binding.tvChatPartnerName.text = it.name
                
                if (!it.profilePic.isNullOrEmpty()) {
                    Glide.with(requireContext())
                        .load(it.profilePic)
                        .placeholder(R.drawable.ic_profile)
                        .error(R.drawable.ic_profile)
                        .into(binding.ivChatPartnerProfile)
                }
            }
        }
        
        viewModel.error.observe(viewLifecycleOwner) { errorMessage ->
            errorMessage?.let {
                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                viewModel.clearError()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}