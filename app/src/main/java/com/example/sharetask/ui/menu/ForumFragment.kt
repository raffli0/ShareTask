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
//        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
//            shimerView.visibility = if (isLoading) View.VISIBLE else View.GONE
//            rvQuestions.visibility = if (isLoading) View.GONE else View.VISIBLE
//        }
        viewModel.questions.observe(viewLifecycleOwner) { questions ->
            questionAdapter.submitList(questions)
        }
        viewModel.loadQuestions()
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