package com.baubap.challenge.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.baubap.challenge.ui.screens.RegisterScreen
import com.baubap.challenge.ui.screens.HomeScreen
import com.baubap.challenge.ui.screens.LoginScreen
import com.baubap.challenge.ui.theme.BaubapChallengeTheme
import com.baubap.challenge.ui.viewmodels.AuthViewModel
import com.baubap.challenge.ui.viewmodels.MainFlowScreens

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BaubapChallengeTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    AuthApp(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun AuthApp(
    modifier: Modifier = Modifier,
    authViewModel: AuthViewModel = viewModel()
) {
    val currentScreen = authViewModel.screenState.collectAsState()

    when (currentScreen.value) {
        MainFlowScreens.login -> {
            LoginScreen(
                onNavigateToRegister = { authViewModel.navToRegister() },
                onNavigateToHome = { authViewModel.navToHome() },
                viewModel = authViewModel
            )
        }
        MainFlowScreens.register -> {
            BackHandler {
                authViewModel.navToLogin()
            }
            RegisterScreen(
                onNavigateToLogin = { authViewModel.navToLogin() },
                onNavigateToHome = { authViewModel.navToHome() },
                viewModel = authViewModel
            )
        }
        MainFlowScreens.home -> {
            HomeScreen(
                onLogout = { authViewModel.navToLogin() },
                viewModel = authViewModel
            )
        }
    }
}