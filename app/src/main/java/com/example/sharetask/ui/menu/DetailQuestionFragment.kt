package com.example.sharetask.ui.menu

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.sharetask.viewmodel.DetailQuestionViewModel
import com.example.sharetask.databinding.FragmentDetailQuestionBinding
import com.bumptech.glide.Glide

class DetailQuestionFragment : Fragment() {


    companion object {
        fun newInstance() = DetailQuestionFragment()
    }

    private var _binding: FragmentDetailQuestionBinding? = null
    private val binding get() = _binding!!
    private val viewModel: DetailQuestionViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: Use the ViewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDetailQuestionBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val questionId = arguments?.getString("questionId") ?: ""
        viewModel.loadQuestion(questionId)
        viewModel.loadAnswers(questionId)

        // Observe question detail
        viewModel.question.observe(viewLifecycleOwner) { question ->
            binding.tvAskerName.text = question.uploadedBy
            binding.tvQuestionBodyDetail.text = question.description
            binding.tvQuestionTitleDetail.text = question.subjectName
            // Load photo jika ada
            Glide.with(requireContext())
                .load(question.uploadedByPhotoUrl)
                .into(binding.ivAskerAvatar)
        }
        // Observe answers (dummy, tampilkan count)
        viewModel.answers.observe(viewLifecycleOwner) { answers ->
            binding.tvAnswersSectionTitle.text = "Answers (${answers.size})"
            // TODO: Set adapter RecyclerView jika sudah ada
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun refreshData() {
        TODO("Not yet implemented")
    }
}