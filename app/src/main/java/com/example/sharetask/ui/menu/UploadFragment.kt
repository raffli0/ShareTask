package com.example.sharetask.ui.menu

import android.app.Activity
import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.PopupMenu
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.sharetask.R
import com.example.sharetask.data.model.Subject
import com.example.sharetask.databinding.FragmentUploadBinding
import com.example.sharetask.ui.main.MainActivity
import com.example.sharetask.viewmodel.UploadState
import com.example.sharetask.viewmodel.UploadViewModel
import com.google.android.material.snackbar.Snackbar

class UploadFragment : Fragment() {

    private var _binding: FragmentUploadBinding? = null
    private val binding get() = _binding!!
    private val viewModel: UploadViewModel by viewModels()
    private var selectedImageUri: Uri? = null
    private var selectedSubject: Subject? = null

    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            binding.imagePreview.apply {
                setImageURI(it)
                visibility = View.VISIBLE
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUploadBinding.inflate(inflater, container, false).apply {
            lifecycleOwner = viewLifecycleOwner
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
            // Subject selection
            categoryButton.setOnClickListener {
                showSubjectMenu()
            }

            // Image selection
            selectImageButton.setOnClickListener {
                imagePickerLauncher.launch("image/*")
            }

            // Upload button
            uploadButton.setOnClickListener {
                if (validateInput()) {
                    uploadTask()
                }
            }

            // Image preview click to remove
            imagePreview.setOnClickListener {
                selectedImageUri = null
                imagePreview.visibility = View.GONE
            }
        }
    }

    private fun showSubjectMenu() {
        val popup = PopupMenu(requireContext(), binding.categoryButton)
        
        // Add subjects to menu
        getSubjects().forEachIndexed { index, subject ->
            popup.menu.add(0, index, index, subject.name)
        }

        // Rotate dropdown icon when showing menu
        val rotateAnimation = AnimationUtils.loadAnimation(requireContext(), R.anim.rotate_dropdown)
        binding.categoryButton.startAnimation(rotateAnimation)
        
        // Change drawable to rotated version
        binding.categoryButton.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_dropdown_rotated, 0)
        // Ensure tint color is maintained
        binding.categoryButton.compoundDrawables[2]?.setTint(resources.getColor(R.color.white, null))

        popup.setOnMenuItemClickListener { menuItem ->
            val subject = getSubjects()[menuItem.itemId]
            selectedSubject = subject
            binding.categoryButton.text = subject.name
            
            // Rotate back when menu item is selected
            val rotateBackAnimation = AnimationUtils.loadAnimation(requireContext(), R.anim.rotate_dropdown_back)
            binding.categoryButton.startAnimation(rotateBackAnimation)
            
            // Change drawable back to original
            binding.categoryButton.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_dropdown, 0)
            // Ensure tint color is maintained
            binding.categoryButton.compoundDrawables[2]?.setTint(resources.getColor(R.color.white, null))
            
            true
        }

        popup.setOnDismissListener {
            // Rotate back when menu is dismissed without selection
            val rotateBackAnimation = AnimationUtils.loadAnimation(requireContext(), R.anim.rotate_dropdown_back)
            binding.categoryButton.startAnimation(rotateBackAnimation)
            
            // Change drawable back to original
            binding.categoryButton.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_dropdown, 0)
            // Ensure tint color is maintained
            binding.categoryButton.compoundDrawables[2]?.setTint(resources.getColor(R.color.white, null))
        }

        popup.show()
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

    private fun validateInput(): Boolean {
        //validasi subject
        if (selectedSubject == null) {
            showError("Please select a subject")
            return false
        }

        // Validasi teks wajib diisi
        if (binding.descriptionEditText.text.isNullOrBlank()) {
            showError("Please enter your question")
            return false
        }
        return true
    }

    private fun uploadTask() {
        val description = binding.descriptionEditText.text.toString().trim()
        viewModel.uploadTask(
            description = description,
            imageUri = selectedImageUri,
            subject = selectedSubject
        )
    }

    private fun observeViewModel() {
        viewModel.uploadState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UploadState.Loading -> {
                    setLoadingState(true)
                }
                is UploadState.Success -> {
                    setLoadingState(false)
                    clearForm()
                    showSuccess(state.message)
                    findNavController().navigateUp()
                }
                is UploadState.Error -> {
                    setLoadingState(false)
                    showError(state.message)
                }
                null -> {
                    // Initial state
                }
            }
        }
    }

    private fun setLoadingState(isLoading: Boolean) {
        binding.uploadButton.isEnabled = !isLoading
        binding.uploadButton.text = if (isLoading) "Uploading..." else "ASK"
        binding.selectImageButton.isEnabled = !isLoading
        binding.categoryButton.isEnabled = !isLoading
        binding.descriptionEditText.isEnabled = !isLoading
    }

    private fun clearForm() {
        binding.descriptionEditText.setText("")
        binding.imagePreview.visibility = View.GONE
        selectedImageUri = null
        selectedSubject = null
        binding.categoryButton.text = "SUBJECT"
    }

    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
            .setAction("OK") { }
            .show()
    }

    private fun showSuccess(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }

    fun refreshData() {
        clearForm()
        setLoadingState(false)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}