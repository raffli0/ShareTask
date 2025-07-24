package com.example.sharetask.ui.menu

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.sharetask.R
import com.example.sharetask.adapter.LatestViewAdapter
import com.example.sharetask.databinding.FragmentProfileBinding
import com.example.sharetask.ui.auth.LoginActivity
import com.example.sharetask.viewmodel.ProfileViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ProfileViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.vm = viewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val user = FirebaseAuth.getInstance().currentUser
        binding.tvUserName.text = user?.displayName ?: "User"

        val requestOptions = RequestOptions()
            .placeholder(R.drawable.ic_profile) //load photo profile from drawable
            .error(R.drawable.ic_profile)

        Glide.with(this)
            .load(user?.photoUrl) // load photo profile from google
            .apply(requestOptions)
            .into(binding.ivProfilePicture)


        binding.ivSettings.setOnClickListener {
            binding.btnLogout.visibility = if (binding.btnLogout.visibility == View.VISIBLE) {
                View.GONE
            } else {
                View.VISIBLE
            }
        }

        binding.btnLogout.setOnClickListener {
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id)) // Gunakan ID yang sama
                .requestEmail()
                .build()
            val googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)

            googleSignInClient.signOut().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(
                        requireContext(),
                        "Logout berhasil.",
                        Toast.LENGTH_SHORT
                    ).show()
                    // Logout Google berhasil
                    // Sekarang arahkan pengguna kembali ke LoginActivity
                    val intent = Intent(requireActivity(), LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    requireActivity().finish() // Tutup Activity saat ini (misalnya MainActivity)
                } else {
                    // Handle error gagal logout
                }
            }
        }

        binding.rvLatestView.adapter = LatestViewAdapter(getSampleLatestView())
    }

    private fun getSampleLatestView(): List<String> {
        return listOf("View 1", "View 2", "View 3")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun refreshData() {
        binding.rvLatestView.adapter = LatestViewAdapter(getSampleLatestView())
    }
}