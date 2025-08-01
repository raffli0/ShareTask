package com.example.sharetask.ui.menu

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.sharetask.R
import com.example.sharetask.databinding.FragmentProfileSettingsBinding
import com.example.sharetask.ui.auth.LoginActivity
import com.example.sharetask.utils.LatestViewManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileSettingsFragment : Fragment() {

    private var _binding: FragmentProfileSettingsBinding? = null
    private val binding get() = _binding!!
    private val TAG = "ProfileSettingsFragment"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        setupClickListeners()
    }

    private fun setupToolbar() {
        binding.toolbarProfileSettings.setNavigationOnClickListener {
            requireActivity().onBackPressed()
        }
    }

    private fun setupClickListeners() {
        // Logout button
        binding.btnLogout.setOnClickListener {
            showLogoutConfirmationDialog()
        }

        // Delete account button
        binding.btnDeleteAccount.setOnClickListener {
            showDeleteAccountConfirmationDialog()
        }

        // Clear latest view button
        binding.btnClearLatestView.setOnClickListener {
            showClearLatestViewConfirmationDialog()
        }
    }

    private fun showLogoutConfirmationDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Logout")
            .setMessage("Apakah Anda yakin ingin keluar dari akun?")
            .setPositiveButton("Ya") { _, _ ->
                performLogout()
            }
            .setNegativeButton("Tidak", null)
            .show()
    }

    private fun showDeleteAccountConfirmationDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Hapus Akun")
            .setMessage("Apakah Anda yakin ingin menghapus akun? Tindakan ini tidak dapat dibatalkan.")
            .setPositiveButton("Ya") { _, _ ->
                deleteAccount()
            }
            .setNegativeButton("Tidak", null)
            .show()
    }

    private fun showClearLatestViewConfirmationDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Hapus Riwayat")
            .setMessage("Apakah Anda yakin ingin menghapus semua riwayat pertanyaan yang dilihat?")
            .setPositiveButton("Ya") { _, _ ->
                clearLatestView()
            }
            .setNegativeButton("Tidak", null)
            .show()
    }

    private fun performLogout() {
        // Sign out dari Google terlebih dahulu
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        val googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)

        googleSignInClient.signOut().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // Logout dari Firebase Auth
                FirebaseAuth.getInstance().signOut()
                
                Toast.makeText(
                    requireContext(),
                    "Logout berhasil.",
                    Toast.LENGTH_SHORT
                ).show()
                
                // Navigasi ke LoginActivity
                val intent = Intent(requireActivity(), LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                requireActivity().finish()
            } else {
                Toast.makeText(
                    requireContext(),
                    "Gagal logout. Silakan coba lagi.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun deleteAccount() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        val userId = currentUser?.uid
        
        if (userId == null) {
            Toast.makeText(requireContext(), "Tidak dapat menemukan akun", Toast.LENGTH_SHORT).show()
            return
        }
        
        // 1. Hapus data user dari Firestore
        val db = FirebaseFirestore.getInstance()
        db.collection("users").document(userId)
            .delete()
            .addOnSuccessListener {
                Log.d(TAG, "User data deleted from Firestore")
                
                // 2. Hapus akun Firebase Auth
                currentUser.delete()
                    .addOnSuccessListener {
                        Log.d(TAG, "User account deleted from Firebase Auth")
                        Toast.makeText(requireContext(), "Akun berhasil dihapus", Toast.LENGTH_SHORT).show()
                        
                        // Navigasi ke LoginActivity
                        val intent = Intent(requireActivity(), LoginActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        requireActivity().finish()
                    }
                    .addOnFailureListener { e ->
                        Log.e(TAG, "Error deleting user account", e)
                        Toast.makeText(requireContext(), "Gagal menghapus akun: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error deleting user data", e)
                Toast.makeText(requireContext(), "Gagal menghapus data pengguna: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun clearLatestView() {
        val latestViewManager = LatestViewManager.getInstance(requireContext())
        latestViewManager.clearLatestViewList()
        Toast.makeText(requireContext(), "Riwayat berhasil dihapus", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}