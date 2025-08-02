package com.example.sharetask.ui.menu

import androidx.fragment.app.viewModels
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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

    private lateinit var questionId: String
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        try {
            setupToolbar()
            setupBackPressHandler()
            setupSwipeRefresh()

            questionId = arguments?.getString("questionId") ?: ""
            // Log untuk debugging
            android.util.Log.d("DetailQuestionFragment", "QuestionId: $questionId")
            
            if (questionId.isEmpty()) {
                android.util.Log.e("DetailQuestionFragment", "questionId is empty, cannot load question details")
                Toast.makeText(context, "Error: Tidak dapat memuat detail pertanyaan", Toast.LENGTH_SHORT).show()
                requireActivity().supportFragmentManager.popBackStack()
                return
            }
            
            viewModel.loadQuestion(questionId)
            viewModel.loadAnswers(questionId)

            // Observe question detail
            viewModel.question.observe(viewLifecycleOwner) { question ->
                try {
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

                    // Hentikan animasi refresh jika sedang refresh
                    if (binding.swipeRefreshLayout.isRefreshing) {
                        binding.swipeRefreshLayout.isRefreshing = false
                    }
                } catch (e: Exception) {
                    android.util.Log.e("DetailQuestionFragment", "Error updating UI with question data: ${e.message}")
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("DetailQuestionFragment", "Error in onViewCreated: ${e.message}")
            Toast.makeText(context, "Error: Tidak dapat memuat detail pertanyaan", Toast.LENGTH_SHORT).show()
            try {
                requireActivity().supportFragmentManager.popBackStack()
            } catch (e2: Exception) {
                android.util.Log.e("DetailQuestionFragment", "Error navigating back: ${e2.message}")
            }
        }
        // Setup RecyclerView untuk answers
        val answerAdapter = AnswerAdapter()
        binding.rvAnswers.adapter = answerAdapter
        
        // Observe answers
        viewModel.answers.observe(viewLifecycleOwner) { answers ->
            try {
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
                
                // Hentikan animasi refresh jika sedang refresh
                if (_binding != null && binding.swipeRefreshLayout.isRefreshing) {
                    binding.swipeRefreshLayout.isRefreshing = false
                }
            } catch (e: Exception) {
                android.util.Log.e("DetailQuestionFragment", "Error updating UI with answers data: ${e.message}")
            }
        }
        
        // Setup FAB untuk menambahkan jawaban
        try {
            binding.fabAnswerQuestion.setOnClickListener {
                try {
                    showAddAnswerDialog(questionId)
                } catch (e: Exception) {
                    android.util.Log.e("DetailQuestionFragment", "Error showing answer dialog: ${e.message}")
                    Toast.makeText(context, "Failed to open answer dialog", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("DetailQuestionFragment", "Error setting up FAB: ${e.message}")
        }
        
        // Atur behavior SwipeRefreshLayout berdasarkan posisi scroll
        try {
            binding.nestedScrollView.setOnScrollChangeListener { _, _, scrollY, _, _ ->
                try {
                    // Aktifkan SwipeRefreshLayout hanya ketika scroll di posisi paling atas
                    // untuk mencegah konflik dengan scroll up
                    binding.swipeRefreshLayout.isEnabled = scrollY == 0
                } catch (e: Exception) {
                    android.util.Log.e("DetailQuestionFragment", "Error in scroll listener: ${e.message}")
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("DetailQuestionFragment", "Error setting up scroll listener: ${e.message}")
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
        try {
            binding.toolbarDetailQuestion.setNavigationOnClickListener {
                try {
                    // Gunakan popBackStack untuk kembali ke fragment sebelumnya
                    // dengan mempertahankan posisi scroll
                    requireActivity().supportFragmentManager.popBackStack()
                } catch (e: Exception) {
                    android.util.Log.e("DetailQuestionFragment", "Error navigating back: ${e.message}")
                    activity?.onBackPressed()
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("DetailQuestionFragment", "Error setting up toolbar: ${e.message}")
        }
    }
    
    private fun setupBackPressHandler() {
        try {
            // Menangani tombol back device
            val callback = object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    try {
                        // Gunakan popBackStack untuk mempertahankan posisi scroll
                        requireActivity().supportFragmentManager.popBackStack()
                    } catch (e: Exception) {
                        android.util.Log.e("DetailQuestionFragment", "Error in handleOnBackPressed: ${e.message}")
                        try {
                            // Fallback jika popBackStack gagal
                            this.remove()
                            requireActivity().onBackPressed()
                        } catch (e2: Exception) {
                            android.util.Log.e("DetailQuestionFragment", "Error navigating back: ${e2.message}")
                        }
                    }
                }
            }
            requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
        } catch (e: Exception) {
            android.util.Log.e("DetailQuestionFragment", "Error setting up back press handler: ${e.message}")
        }
    }
    
    private fun setupSwipeRefresh() {
        try {
            // Setup SwipeRefreshLayout
            binding.swipeRefreshLayout.setOnRefreshListener {
                // Refresh data
                refreshData()
            }
            
            // Atur warna indikator refresh
            binding.swipeRefreshLayout.setColorSchemeResources(
                android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light
            )
            
            // Pastikan NestedScrollView dapat di-scroll ke atas
            binding.nestedScrollView.isFocusable = true
            binding.nestedScrollView.isFocusableInTouchMode = true
            
            // Atur behavior scroll untuk memudahkan scroll up
            binding.swipeRefreshLayout.setProgressViewOffset(true, 0, 120)
            
            // Nonaktifkan SwipeRefreshLayout secara default
            // Akan diaktifkan hanya ketika scroll di posisi paling atas
            binding.swipeRefreshLayout.isEnabled = false
        } catch (e: Exception) {
            android.util.Log.e("DetailQuestionFragment", "Error setting up swipe refresh: ${e.message}")
        }
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
        try {
            super.onDestroyView()
            _binding = null
        } catch (e: Exception) {
            android.util.Log.e("DetailQuestionFragment", "Error in onDestroyView: ${e.message}")
        }
    }

    fun refreshData() {
        val questionId = arguments?.getString("questionId") ?: ""
        // Log untuk debugging
        android.util.Log.d("DetailQuestionFragment", "refreshData - QuestionId: $questionId")
        
        if (questionId.isNotEmpty()) {
            try {
                // Tampilkan indikator loading jika belum ditampilkan
                if (_binding != null && !binding.swipeRefreshLayout.isRefreshing) {
                    binding.swipeRefreshLayout.isRefreshing = true
                }
                
                // Load data
                viewModel.loadQuestion(questionId)
                viewModel.loadAnswers(questionId)
                
                // Hentikan animasi refresh setelah beberapa waktu jika data belum selesai dimuat
                // untuk mencegah refresh terlalu lama
                Handler(Looper.getMainLooper()).postDelayed({
                    try {
                        if (isAdded && _binding != null && binding.swipeRefreshLayout.isRefreshing) {
                            binding.swipeRefreshLayout.isRefreshing = false
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("DetailQuestionFragment", "Error stopping refresh: ${e.message}")
                    }
                }, 3000) // Timeout setelah 3 detik
            } catch (e: Exception) {
                android.util.Log.e("DetailQuestionFragment", "Error refreshing data: ${e.message}")
                try {
                    if (_binding != null) {
                        binding.swipeRefreshLayout.isRefreshing = false
                    }
                } catch (e2: Exception) {
                    // Ignore
                }
            }
        } else {
            // Jika tidak ada questionId, hentikan animasi refresh
            try {
                if (_binding != null) {
                    binding.swipeRefreshLayout.isRefreshing = false
                }
            } catch (e: Exception) {
                android.util.Log.e("DetailQuestionFragment", "Error stopping refresh: ${e.message}")
            }
        }
    }
    
    /**
     * Menambahkan pertanyaan ke daftar Latest View
     */
    private fun addToLatestView(question: Question) {
        try {
            // Gunakan LatestViewManager untuk menambahkan pertanyaan ke daftar
            if (isAdded && context != null) {
                val latestViewManager = LatestViewManager.getInstance(requireContext())
                latestViewManager.addQuestion(question)
                // Tidak perlu memanggil saveLatestViewList() karena sudah ditangani di dalam addQuestion()
            }
        } catch (e: Exception) {
            android.util.Log.e("DetailQuestionFragment", "Error adding to latest view: ${e.message}")
        }
    }
    
    /**
     * Mengupdate dan menampilkan jumlah view untuk pertanyaan
     */

    /**
     * Menampilkan dialog untuk menambahkan jawaban
     */
    private fun showAddAnswerDialog(questionId: String) {
        try {
            if (questionId.isEmpty()) {
                android.util.Log.e("DetailQuestionFragment", "Cannot show answer dialog: questionId is empty")
                Toast.makeText(context, "Tidak dapat menambahkan jawaban saat ini", Toast.LENGTH_SHORT).show()
                return
            }
            
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
                try {
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
                } catch (e: Exception) {
                    android.util.Log.e("DetailQuestionFragment", "Error submitting answer: ${e.message}")
                    Toast.makeText(context, "Gagal menambahkan jawaban", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                }
            }
            
            dialog.show()
        } catch (e: Exception) {
            android.util.Log.e("DetailQuestionFragment", "Error showing answer dialog: ${e.message}")
            Toast.makeText(context, "Tidak dapat menampilkan dialog jawaban", Toast.LENGTH_SHORT).show()
        }
    }
}