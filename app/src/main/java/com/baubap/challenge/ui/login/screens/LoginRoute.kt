package com.baubap.challenge.ui.login.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.baubap.challenge.ui.login.screens.state.AuthSideEffect
import com.baubap.challenge.ui.login.screens.state.MainFlowScreens
import com.baubap.challenge.ui.login.viewmodels.AuthViewModel
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@Composable
fun LoginRoute(
    viewModel: AuthViewModel,
    onNavigateToLogin: () -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToRegister: () -> Unit,
    logout: () -> Unit,
    modifier: Modifier
) {

    val screenState by viewModel.screenState.collectAsState()
    val state by viewModel.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    viewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is AuthSideEffect.NavigateToHome -> {
                onNavigateToHome()
            }

            is AuthSideEffect.ShowError -> {
                snackbarHostState.showSnackbar(
                    message = sideEffect.message,
                    withDismissAction = true
                )
            }
        }
    }

    Scaffold(
        modifier = modifier,
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (screenState) {
                is MainFlowScreens.login -> {
                    LoginScreen(
                        onNavigateToRegister = onNavigateToRegister,
                        attemptLogin = { email, password -> viewModel.login(email, password) },
                        state = state,
                        clearError = { viewModel.clearError() },
                    )
                }
                is MainFlowScreens.register -> {
                    RegisterScreen(
                        state = state,
                        onNavigateToLogin = onNavigateToLogin,
                        attemptRegister = { email, password -> viewModel.register(email, password) },
                    )
                }
                is MainFlowScreens.home -> {
                    HomeScreen(
                        onLogout = { logout() },
                        state = state,
                    )
                }
            }
        }
    }
}