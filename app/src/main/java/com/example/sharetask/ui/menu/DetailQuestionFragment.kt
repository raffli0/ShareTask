package com.example.sharetask.ui.menu

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import com.example.sharetask.adapter.AnswerAdapter
import com.example.sharetask.utils.LatestViewManager
import com.example.sharetask.data.model.Question
import com.example.sharetask.viewmodel.DetailQuestionViewModel
import com.example.sharetask.databinding.FragmentDetailQuestionBinding
import com.example.sharetask.databinding.DialogAddAnswerBinding
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

        setupToolbar()
        setupBackPressHandler()

        val questionId = arguments?.getString("questionId") ?: ""
        viewModel.loadQuestion(questionId)
        viewModel.loadAnswers(questionId)

        // Observe question detail
        viewModel.question.observe(viewLifecycleOwner) { question ->
            binding.tvAskerName.text = question.uploadedBy
            binding.tvQuestionBodyDetail.text = question.description
            binding.tvQuestionTitleDetail.text = question.subjectName
            // Tampilkan timestamp
            binding.tvQuestionTime.text = getTimeAgo(question.timestamp)
            // Load photo jika ada
            Glide.with(requireContext())
                .load(question.uploadedByPhotoUrl)
                .into(binding.ivAskerAvatar)
                
            // Tambahkan pertanyaan ke Latest View
            addToLatestView(question)
        }
        // Setup RecyclerView untuk answers
        val answerAdapter = AnswerAdapter()
        binding.rvAnswers.adapter = answerAdapter
        
        // Observe answers
        viewModel.answers.observe(viewLifecycleOwner) { answers ->
            binding.tvAnswersSectionTitle.text = "Answers (${answers.size})"
            answerAdapter.submitList(answers)
            
            // Tampilkan atau sembunyikan empty state
            if (answers.isEmpty()) {
                binding.emptyAnswersLayout.visibility = View.VISIBLE
                binding.rvAnswers.visibility = View.GONE
            } else {
                binding.emptyAnswersLayout.visibility = View.GONE
                binding.rvAnswers.visibility = View.VISIBLE
            }
        }
        
        // Setup FAB untuk menambahkan jawaban
        binding.fabAnswerQuestion.setOnClickListener {
            showAddAnswerDialog(questionId)
        }
        
        // Observe status penambahan jawaban
        viewModel.isAddingAnswer.observe(viewLifecycleOwner) { isLoading ->
            // Jika diperlukan, tampilkan loading indicator
        }
        
        viewModel.addAnswerSuccess.observe(viewLifecycleOwner) { isSuccess ->
            if (isSuccess == true) {
                Toast.makeText(requireContext(), "Answer added successfully", Toast.LENGTH_SHORT).show()
            } else if (isSuccess == false) {
                Toast.makeText(requireContext(), "Failed to add answer", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupToolbar() {
        binding.toolbarDetailQuestion.setNavigationOnClickListener {
            // Gunakan popBackStack untuk kembali ke fragment sebelumnya
            // dengan mempertahankan posisi scroll
            requireActivity().supportFragmentManager.popBackStack()
        }
    }
    
    private fun setupBackPressHandler() {
        // Menangani tombol back device
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Gunakan popBackStack untuk mempertahankan posisi scroll
                requireActivity().supportFragmentManager.popBackStack()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
    }
    
    private fun getTimeAgo(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp
        return when {
            diff < java.util.concurrent.TimeUnit.MINUTES.toMillis(1) -> "Posted recently"
            diff < java.util.concurrent.TimeUnit.HOURS.toMillis(1) -> {
                val minutes = java.util.concurrent.TimeUnit.MILLISECONDS.toMinutes(diff)
                "Posted $minutes minute${if (minutes > 1) "s" else ""} ago"
            }
            diff < java.util.concurrent.TimeUnit.DAYS.toMillis(1) -> {
                val hours = java.util.concurrent.TimeUnit.MILLISECONDS.toHours(diff)
                "Posted $hours hour${if (hours > 1) "s" else ""} ago"
            }
            else -> {
                val days = java.util.concurrent.TimeUnit.MILLISECONDS.toDays(diff)
                "Posted $days day${if (days > 1) "s" else ""} ago"
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun refreshData() {
        val questionId = arguments?.getString("questionId") ?: ""
        if (questionId.isNotEmpty()) {
            viewModel.loadQuestion(questionId)
            viewModel.loadAnswers(questionId)
        }
    }
    
    /**
     * Menambahkan pertanyaan ke daftar Latest View
     */
    private fun addToLatestView(question: Question) {
        // Gunakan LatestViewManager untuk menambahkan pertanyaan ke daftar
        val latestViewManager = LatestViewManager.getInstance(requireContext())
        latestViewManager.addQuestion(question)
        // Tidak perlu memanggil saveLatestViewList() karena sudah ditangani di dalam addQuestion()
    }
    
    /**
     * Menampilkan dialog untuk menambahkan jawaban
     */
    private fun showAddAnswerDialog(questionId: String) {
        val dialogBinding = DialogAddAnswerBinding.inflate(layoutInflater)
        
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogBinding.root)
            .create()
        
        // Setup tombol cancel
        dialogBinding.btnCancel.setOnClickListener {
            dialog.dismiss()
        }
        
        // Setup tombol submit
        dialogBinding.btnSubmit.setOnClickListener {
            val answerContent = dialogBinding.etAnswerContent.text.toString().trim()
            
            if (answerContent.isEmpty()) {
                dialogBinding.tilAnswerContent.error = "Answer cannot be empty"
                return@setOnClickListener
            }
            
            // Reset error
            dialogBinding.tilAnswerContent.error = null
            
            // Tampilkan loading
            dialogBinding.progressBar.visibility = View.VISIBLE
            dialogBinding.btnSubmit.visibility = View.INVISIBLE
            
            // Kirim jawaban ke Firestore
            viewModel.addAnswer(questionId, answerContent)
            
            // Tutup dialog setelah submit
            dialog.dismiss()
        }
        
        dialog.show()
    }
}