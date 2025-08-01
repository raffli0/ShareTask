package com.example.sharetask.ui.menu

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.sharetask.R
import com.example.sharetask.databinding.FragmentForumBinding
import com.example.sharetask.adapter.QuestionAdapter
import com.example.sharetask.viewmodel.ForumViewModel

class ForumFragment : Fragment() {

    private var _binding: FragmentForumBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ForumViewModel by viewModels()
    private val questionAdapter = QuestionAdapter { question ->
        val bundle = Bundle().apply {
            putString("questionId", question.id)
        }
        findNavController().navigate(R.id.fragment_detail_question, bundle)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentForumBinding.inflate(inflater, container, false).apply {
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
//                findNavController().navigate(R.id.fragment_upload)
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
        binding.rvQuestions.adapter = null
        _binding = null
    }
} 