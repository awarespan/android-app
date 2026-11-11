package com.awarespan.guardian.ui.screen.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.awarespan.guardian.BuildConfig
import com.awarespan.guardian.domain.model.User
import com.awarespan.guardian.domain.usecase.LoginUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class LoginUiState(
    val loading: Boolean = false,
    val error: String? = null,
    val loggedInUser: User? = null,
    val endpointUrl: String = BuildConfig.API_BASE_URL,
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState

    fun login(username: String, password: String) {
        if (username.isBlank() || password.isBlank()) {
            _uiState.value = _uiState.value.copy(
                loading = false,
                error = "Operator identifier and access token/key required"
            )
            return
        }
        _uiState.value = _uiState.value.copy(loading = true, error = null)
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val user = loginUseCase.execute(username.trim(), password)
                _uiState.value = _uiState.value.copy(
                    loading = false,
                    loggedInUser = user,
                    error = null
                )
            } catch (e: SecurityException) {
                _uiState.value = _uiState.value.copy(
                    loading = false,
                    error = e.message ?: "Authentication failed: invalid credentials"
                )
            } catch (e: IllegalStateException) {
                _uiState.value = _uiState.value.copy(
                    loading = false,
                    error = e.message ?: "Backend rejected credentials"
                )
            } catch (e: UnknownHostException) {
                _uiState.value = _uiState.value.copy(
                    loading = false,
                    error = "Host resolution failed: unable to resolve backend DNS"
                )
            } catch (e: ConnectException) {
                _uiState.value = _uiState.value.copy(
                    loading = false,
                    error = "Connection refused: backend service is unreachable"
                )
            } catch (e: SocketTimeoutException) {
                _uiState.value = _uiState.value.copy(
                    loading = false,
                    error = "Connection timed out connecting to backend"
                )
            } catch (e: IOException) {
                _uiState.value = _uiState.value.copy(
                    loading = false,
                    error = "Network transmission failed: ${e.message ?: "check connectivity"}"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    loading = false,
                    error = "Authentication exception: ${e.javaClass.simpleName} (${e.message ?: "unknown"})"
                )
            }
        }
    }
}
