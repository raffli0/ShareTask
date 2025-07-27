package com.example.sharetask.data.model

sealed class AuthResult {
    object Loading : AuthResult()
    data class Success(val message: String, val isNewUser: Boolean = false, val isGoogleSignIn: Boolean = false) : AuthResult()
    data class Error(val message: String) : AuthResult()
}