package com.awarespan.guardian.ui.screen.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.awarespan.guardian.data.logging.DiagnosticLogger
import com.awarespan.guardian.domain.model.User
import com.awarespan.guardian.domain.usecase.LoginUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.IOException
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class LoginUiState(
    val loading: Boolean = false,
    val error: String? = null,
    val loggedInUser: User? = null,
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val logger: DiagnosticLogger,
) : ViewModel() {
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState

    fun login(username: String, password: String) {
        if (username.isBlank() || password.isBlank()) {
            _uiState.value = _uiState.value.copy(
                loading = false,
                error = "Please enter both operator identifier and access key"
            )
            return
        }
        _uiState.value = _uiState.value.copy(loading = true, error = null)
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val user = loginUseCase.execute(username.trim(), password)
                logger.logAuthEvent("login", true, "User: ${user.username}, Role: ${user.role}")
                _uiState.value = _uiState.value.copy(
                    loading = false,
                    loggedInUser = user,
                    error = null
                )
            } catch (e: SecurityException) {
                logger.logAuthEvent("login", false, "SecurityException: ${e.message}")
                _uiState.value = _uiState.value.copy(
                    loading = false,
                    error = "Invalid operator identifier or access key."
                )
            } catch (e: IOException) {
                logger.warn("Auth", "Network communication failure during sign in", e)
                _uiState.value = _uiState.value.copy(
                    loading = false,
                    error = "Unable to connect to service. Please verify your network connection."
                )
            } catch (e: Exception) {
                logger.error("Auth", "Unexpected failure during sign in", e)
                _uiState.value = _uiState.value.copy(
                    loading = false,
                    error = "Sign in could not be completed. Please try again."
                )
            }
        }
    }
}
