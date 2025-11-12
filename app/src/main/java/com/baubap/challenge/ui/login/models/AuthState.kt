package com.baubap.challenge.ui.login.models

data class AuthState(
    val isLoading: Boolean = false,
    val user: User? = null,
    val errorMessage: String? = null,
)