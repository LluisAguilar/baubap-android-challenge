package com.baubap.challenge.ui.login.viewmodels

import androidx.lifecycle.ViewModel
import com.baubap.challenge.data.ErrorResponse
import com.baubap.challenge.data.LoginRequest
import com.baubap.challenge.data.RegisterRequest
import com.baubap.challenge.data.ApiClient
import com.baubap.challenge.ui.login.models.AuthState
import com.baubap.challenge.ui.login.models.User
import com.baubap.challenge.ui.login.screens.state.AuthSideEffect
import com.baubap.challenge.ui.login.screens.state.MainFlowScreens
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.json.Json
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class AuthViewModel : ViewModel(), ContainerHost<AuthState, AuthSideEffect> {

    private val _screenState: MutableStateFlow<MainFlowScreens> =
        MutableStateFlow(MainFlowScreens.login)
    var screenState: StateFlow<MainFlowScreens> = _screenState.asStateFlow()

    override val container = container<AuthState, AuthSideEffect>(AuthState())

    fun login(email: String, password: String) = intent {
        reduce { state.copy(isLoading = true, errorMessage = null) }

        try {
            val request = LoginRequest(email, password)
            val response = ApiClient.apiService.login(request)

            if (response.isSuccessful && response.body() != null) {
                val loginResponse = response.body()!!
                val user = User(
                    token = loginResponse.token,
                    email = email
                )
                reduce {
                    state.copy(
                        isLoading = false,
                        user = user,
                        errorMessage = null
                    )
                }
                postSideEffect(AuthSideEffect.NavigateToHome)
            } else {
                val errorMessage = handleRequestErrors(
                        errorBody = response.errorBody()?.string(),
                responseCode = response.code(),
                action = "login"
                )
                reduce { state.copy( isLoading = false, errorMessage = errorMessage) }
                postSideEffect(AuthSideEffect.ShowError(errorMessage))
            }
        } catch (e: Exception) {
            val errorMessage = handleGeneralErrors(e)
            reduce {
                state.copy(
                    isLoading = false,
                    errorMessage = errorMessage
                )
            }
            postSideEffect(AuthSideEffect.ShowError(errorMessage))
        }
    }

    fun register(email: String, password: String) = intent {
        reduce { state.copy(isLoading = true, errorMessage = null) }

        try {
            val request = RegisterRequest(email, password)
            val response = ApiClient.apiService.register(request)

            if (response.isSuccessful && response.body() != null) {
                val registerResponse = response.body()!!
                val user = User(
                    id = registerResponse.id,
                    token = registerResponse.token,
                    email = email
                )
                reduce {
                    state.copy(
                        isLoading = false,
                        user = user,
                        errorMessage = null
                    )
                }
                postSideEffect(AuthSideEffect.NavigateToHome)
            } else {
                val errorMessage = handleRequestErrors(
                    errorBody = response.errorBody()?.string(),
                    responseCode = response.code(),
                    action = "registro"
                )
                reduce { state.copy( isLoading = false, errorMessage = errorMessage) }
                postSideEffect(AuthSideEffect.ShowError(errorMessage))
            }
        } catch (e: Exception) {
            val errorMessage = handleGeneralErrors(e)
            reduce {
                state.copy(
                    isLoading = false,
                    errorMessage = errorMessage
                )
            }
            postSideEffect(AuthSideEffect.ShowError(errorMessage))
        }
    }

    private fun handleRequestErrors(
        errorBody: String?,
        responseCode: Int,
        action: String
    ): String {

        try {
            if (errorBody != null) {
                val errorResponse = Json.decodeFromString<ErrorResponse>(errorBody)
                return "Error de $action: ${errorResponse.error}"
            }
        } catch (e: Exception) {
            // This catch block is intentionally left empty.
            // If JSON parsing fails (e.g., the error body is not the expected format),
            // we simply ignore the exception and let the code "fall through"
            // to the more generic error handling below, which is the desired behavior.
        }

        return when (responseCode) {
            400 -> "Error de $action: Datos inválidos."
            401 -> "Error de $action: Credenciales incorrectas."
            403 -> "Error de $action: Acceso denegado."
            404 -> "Error de $action: Servicio no encontrado."
            500 -> "Error del servidor. Intenta más tarde."
            else -> "Error de $action desconocido. Código: $responseCode"
        }
    }

    private fun handleGeneralErrors(e: Exception): String {
       return when (e) {
            is UnknownHostException -> "Error de conexión: Verifica tu conexión a internet"
            is SocketTimeoutException -> "Error de conexión: Tiempo de espera agotado"
            else -> "Error de conexión: ${e.message}"
        }
    }

    fun clearError() = intent {
        reduce { state.copy(errorMessage = null) }
    }

    fun logout() = intent {
        reduce { state.copy(user = null, errorMessage = null) }
        onNavigateToLogin()
    }

    fun onNavigateToRegister() {
        _screenState.value = MainFlowScreens.register
    }

    fun onNavigateToHome() {
        _screenState.value = MainFlowScreens.home
    }

    fun onNavigateToLogin() {
        _screenState.value = MainFlowScreens.login
    }
}