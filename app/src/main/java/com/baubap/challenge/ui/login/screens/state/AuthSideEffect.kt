package com.baubap.challenge.ui.login.screens.state

sealed class AuthSideEffect {
    object NavigateToHome : AuthSideEffect()
    data class ShowError(val message: String) : AuthSideEffect()
}