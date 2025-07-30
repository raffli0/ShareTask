package com.example.sharetask.ui.community

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.sharetask.R
import com.example.sharetask.databinding.FragmentCommunityBinding
import com.example.sharetask.viewmodel.CommunityViewModel

class CommunityFragment : Fragment() {

    private var _binding: FragmentCommunityBinding? = null
    private val binding get() = _binding!!
    private val viewModel: CommunityViewModel by viewModels()
    private val questionAdapter = QuestionAdapter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCommunityBinding.inflate(inflater, container, false).apply {
            lifecycleOwner = viewLifecycleOwner
            vm = viewModel
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        observeViewModel()
    }

    private fun setupViews() {
        with(binding) {
            // Setup RecyclerView
            rvQuestions.adapter = questionAdapter

            // Ask Question Button
            btnAskQuestion.setOnClickListener {
//                findNavController().navigate(R.id.fragment_detail_question)
            }
        }
    }

    private fun observeViewModel() {
        viewModel.questions.observe(viewLifecycleOwner) { questions ->
            questionAdapter.submitList(questions)
        }
    }

    fun refreshData() {
        viewModel.loadQuestions()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 