package com.baubap.challenge.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.baubap.challenge.data.ErrorResponse
import com.baubap.challenge.data.LoginRequest
import com.baubap.challenge.data.RegisterRequest
import com.baubap.challenge.data.ApiClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.json.Json
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import java.net.SocketTimeoutException
import java.net.UnknownHostException

data class AuthState(
    val isLoading: Boolean = false,
    val user: User? = null,
    val errorMessage: String? = null,
)

sealed class MainFlowScreens {
    object login: MainFlowScreens()
    object register: MainFlowScreens()
    object home: MainFlowScreens()
}

sealed class AuthSideEffect {
    object NavigateToHome : AuthSideEffect()
    data class ShowError(val message: String) : AuthSideEffect()
}

data class User(
    val id: Int? = null,
    val token: String,
    val email: String
)

class AuthViewModel : ViewModel(), ContainerHost<AuthState, AuthSideEffect> {

    private val _screenState: MutableStateFlow<MainFlowScreens> = MutableStateFlow(MainFlowScreens.login)
    var screenState: StateFlow<MainFlowScreens> = _screenState.asStateFlow()

    private val json = Json {
        ignoreUnknownKeys = true // ¡Esta es la línea clave!
        isLenient = true         // Permite JSON malformado (opcional pero útil)
    }

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
                navToHome()
            } else {
                val errorBody = response.errorBody()?.string()
                val errorMessage = try {
                    val errorResponse = json.decodeFromString<ErrorResponse>(errorBody!!)
                    "Error de login: ${errorResponse.error}"
                } catch (e: Exception) {
                    when (response.code()) {
                        400 -> "Error de login: Datos inválidos. Verifica email y contraseña."
                        401 -> "Error de login: Credenciales incorrectas."
                        403 -> "Error de login: Acceso denegado. Verifica tu API key."
                        404 -> "Error de login: Servicio no encontrado."
                        500 -> "Error del servidor. Intenta más tarde."
                        else -> "Error de login desconocido. Código: ${response.code()}"
                    }
                }

                reduce {
                    state.copy(
                        isLoading = false,
                        errorMessage = errorMessage
                    )
                }
                postSideEffect(AuthSideEffect.ShowError(errorMessage))
            }
        } catch (e: Exception) {
            val errorMessage = when (e) {
                is UnknownHostException -> "Error de conexión: Verifica tu conexión a internet"
                is SocketTimeoutException -> "Error de conexión: Tiempo de espera agotado"
                else -> "Error de conexión: ${e.message}"
            }
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
                navToHome()
            } else {
                val errorBody = response.errorBody()?.string()
                val errorMessage = try {
                    val errorResponse = json.decodeFromString<ErrorResponse>(errorBody!!)
                    "Error de registro: ${errorResponse.error}"
                } catch (e: Exception) {
                    when (response.code()) {
                        400 -> "Error de registro: Email ya registrado o datos inválidos."
                        401 -> "Error de registro: Credenciales incorrectas."
                        403 -> "Error de registro: Acceso denegado. Verifica tu API key."
                        404 -> "Error de registro: Servicio no encontrado."
                        500 -> "Error del servidor. Intenta más tarde."
                        else -> "Error de registro desconocido. Código: ${response.code()}"
                    }
                }

                reduce {
                    state.copy(
                        isLoading = false,
                        errorMessage = errorMessage
                    )
                }
                postSideEffect(AuthSideEffect.ShowError(errorMessage))
            }
        } catch (e: Throwable) {
            e.printStackTrace()
            val errorMessage = when (e) {
                is UnknownHostException -> "Error de conexión: Verifica tu conexión a internet"
                is SocketTimeoutException -> "Error de conexión: Tiempo de espera agotado"
                else -> "Error de conexión: ${e.message}"
            }
            reduce {
                state.copy(
                    isLoading = false,
                    errorMessage = errorMessage
                )
            }
            postSideEffect(AuthSideEffect.ShowError(errorMessage))
        }
    }

    fun clearError() = intent {
        reduce { state.copy(errorMessage = null) }
    }

    fun logout() = intent {
        reduce { state.copy(user = null, errorMessage = null) }
    }

    fun navToRegister() {
        _screenState.value = MainFlowScreens.register
    }

    fun navToHome() {
        _screenState.value = MainFlowScreens.home
    }

    fun navToLogin() {
        _screenState.value = MainFlowScreens.login
    }
}