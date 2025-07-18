package com.example.sharetask.ui.menu

import android.app.Activity
import android.content.Intent
import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.example.sharetask.databinding.FragmentUploadBinding
import com.example.sharetask.ui.main.MainActivity
import com.example.sharetask.viewmodel.UploadState
import com.example.sharetask.viewmodel.UploadViewModel
import com.google.android.material.snackbar.Snackbar

class UploadFragment : Fragment() {

    private var _binding: FragmentUploadBinding? = null
    private val binding get() = _binding!!
    private val viewModel: UploadViewModel by viewModels()

    // Remove file picker as we don't need it anymore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUploadBinding.inflate(inflater, container, false).apply {
            vm = this@UploadFragment
            lifecycleOwner = viewLifecycleOwner
        }

        setupViews()
        observeViewModel()

        return binding.root
    }

    private fun setupViews() {
        binding.uploadButton.setOnClickListener {
            onUploadButtonClicked()
        }
    }

    private fun observeViewModel() {
        viewModel.uploadState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UploadState.Loading -> {
                    binding.uploadButton.isEnabled = false
                    binding.uploadButton.text = "Uploading..."
                }
                is UploadState.Success -> {
                    binding.uploadButton.isEnabled = true
                    binding.uploadButton.text = "Upload"
                    binding.descriptionEditText.setText("")
                    Snackbar.make(binding.root, state.message, Snackbar.LENGTH_LONG).show()
                    // Refresh HomeFragment
                    (requireActivity() as MainActivity).refreshActiveFragment()
                }
                is UploadState.Error -> {
                    binding.uploadButton.isEnabled = true
                    binding.uploadButton.text = "Upload"
                    Snackbar.make(binding.root, state.message, Snackbar.LENGTH_LONG)
                        .setAction("Retry") { onUploadButtonClicked() }
                        .show()
                }
                null -> {
                    // Initial state
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun onUploadButtonClicked() {
        val description = binding.descriptionEditText.text.toString()
        viewModel.uploadTask(description)
    }

    fun refreshData() {
        binding.uploadButton.isEnabled = true
        binding.uploadButton.text = "Upload"
        binding.descriptionEditText.setText("")
    }
}