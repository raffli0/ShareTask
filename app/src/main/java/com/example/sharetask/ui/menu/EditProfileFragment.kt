package com.example.sharetask.ui.menu

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.sharetask.R
import com.example.sharetask.viewmodel.EditProfileViewModel
import android.app.Activity
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.sharetask.databinding.FragmentEditProfileBinding
import com.google.android.material.snackbar.Snackbar
import android.content.Intent // Pastikan import Intent
import com.example.sharetask.ui.main.MainActivity

class EditProfileFragment : Fragment() {

    private var _binding: FragmentEditProfileBinding? = null
    private val binding get() = _binding!!

    private val viewModel: EditProfileViewModel by viewModels()

    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.let { uri ->
                    viewModel.setSelectedImageUri(uri)
                    // Tampilkan pratinjau langsung
                    Glide.with(this)
                        .load(uri)
                        .placeholder(R.drawable.ic_profile) // Ganti dengan placeholder Anda
                        .error(R.drawable.avatar)     // Ganti dengan error placeholder Anda
                        .into(binding.ivEditProfilePicture)
                }
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditProfileBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner // Penting untuk LiveData dan DataBinding
        binding.viewModel = viewModel // Set ViewModel untuk DataBinding
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        setupClickListeners()
        observeViewModel()

        // Tidak perlu memanggil loadUserProfile() di sini jika sudah dipanggil di init ViewModel
        // Jika belum, Anda bisa memanggilnya di sini:
        // if (viewModel.currentName.value == null) { // Hanya load jika data belum ada
        //     viewModel.loadUserProfile()
        // }
    }

    private fun setupToolbar() {
        binding.toolbarEditProfile.setNavigationOnClickListener {
            // Gunakan requireActivity().onBackPressed() sebagai alternatif jika findNavController().navigateUp() tidak berfungsi
            requireActivity().onBackPressed()
        }
    }

    private fun setupClickListeners() {
        binding.fabChangeProfilePicture.setOnClickListener {
            openImagePicker()
        }

        binding.btnSaveProfileChanges.setOnClickListener {
            // Nilai sudah ada di ViewModel karena data binding dua arah
            val nameFromVM = viewModel.currentName.value ?: ""
            val nimFromVM = viewModel.currentNim.value // Bisa null
            // val bioFromVM = viewModel.currentBio.value // Jika Anda pakai bio

            // Anda masih bisa melakukan validasi tambahan di sini jika perlu sebelum memanggil save
            if (nameFromVM.isBlank()) {
                binding.tilDisplayName.error = "Name cannot be empty" // Tampilkan error di TextInputLayout
                return@setOnClickListener
            } else {
                binding.tilDisplayName.error = null // Hapus error jika valid
            }

            viewModel.saveProfileChanges(
                newName = nameFromVM,
                newNim = nimFromVM,
                // newBio = bioFromVM // Jika Anda pakai bio
            )
        }
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        pickImageLauncher.launch(intent)
    }

    private fun observeViewModel() {
        // Observasi untuk memuat foto profil awal
        viewModel.currentProfilePicUrl.observe(viewLifecycleOwner) { url ->
            // Hanya load jika tidak ada gambar baru yang sedang dipilih untuk pratinjau
            if (viewModel.selectedImageUri.value == null) {
                Glide.with(this)
                    .load(url)
                    .placeholder(R.drawable.ic_profile)
                    .error(R.drawable.avatar)
                    .centerCrop() // Atau fitCenter() sesuai kebutuhan
                    .into(binding.ivEditProfilePicture)
            }
        }

        // Observasi selectedImageUri jika ingin logic tambahan saat URI berubah (selain pratinjau)
        // viewModel.selectedImageUri.observe(viewLifecycleOwner) { uri ->
        //     // uri bisa null jika gambar dibatalkan atau setelah upload
        // }

        viewModel.profileUpdateStatus.observe(viewLifecycleOwner) { status ->
            when (status) {
                is EditProfileViewModel.UpdateStatus.Success -> {
                    Toast.makeText(context, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
                    // Gunakan onBackPressed sebagai alternatif jika findNavController().navigateUp() tidak berfungsi
                    requireActivity().onBackPressed()
                    
                    // Refresh ProfileFragment saat kembali
                    val mainActivity = requireActivity()
                    if (mainActivity is MainActivity) {
                        mainActivity.refreshActiveFragment()
                    }
                }
                is EditProfileViewModel.UpdateStatus.Error -> {
                    Snackbar.make(binding.root, status.message, Snackbar.LENGTH_LONG).show()
                }
                EditProfileViewModel.UpdateStatus.Idle -> {
                    // Tidak melakukan apa-apa atau reset UI tertentu
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
