package com.baubap.challenge.ui.login.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.baubap.challenge.ui.login.navigation.LoginRoute.LOGIN_ROUTE
import com.baubap.challenge.ui.login.screens.LoginRoute
import com.baubap.challenge.ui.login.viewmodels.AuthViewModel

@Composable
fun LoginNavHost(
    authViewModel: AuthViewModel,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = LOGIN_ROUTE
    ) {

        composable(LOGIN_ROUTE) {
            LoginRoute(
                modifier = modifier,
                viewModel = authViewModel,
                onNavigateToRegister = { authViewModel.onNavigateToRegister() },
                onNavigateToHome = { authViewModel.onNavigateToHome() },
                onNavigateToLogin = { authViewModel.onNavigateToLogin() },
                logout = { authViewModel.logout() }
            )
        }
    }
}

object LoginRoute {
    const val LOGIN_ROUTE = "login"
}