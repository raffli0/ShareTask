package com.example.sharetask.ui.menu

import android.app.Activity
import android.content.Intent
import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.example.sharetask.databinding.FragmentUploadBinding
import com.example.sharetask.viewmodel.UploadViewModel

class UploadFragment : Fragment() {

    private var _binding: FragmentUploadBinding? = null
    private val binding get() = _binding!!
    private val viewModel: UploadViewModel by viewModels()

    private val filePickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            // TODO: Handle the selected file URI from data.data
            data?.data?.let { selectedFileUri ->
                viewModel.setSelectedFile(selectedFileUri)
                binding.selectFileButton.text = "File Selected" // Indicate a file is selected
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUploadBinding.inflate(inflater, container, false).apply {
            vm = this@UploadFragment
            lifecycleOwner = viewLifecycleOwner
        }

        binding.selectFileButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply { type = "*/*" } // Allow all file types for now.  Consider filtering.
            filePickerLauncher.launch(intent)
        }

        return binding.root // Return the root view of the binding
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // This function will be called from the layout when the upload button is clicked
    fun onUploadButtonClicked() {
        val description = binding.descriptionEditText.text.toString()
        // TODO: Implement the upload logic using viewModel.selectedFile and description
        viewModel.uploadFile(description)
    }

    // Consider adding refreshData() if you need to reload data within the fragment.
}