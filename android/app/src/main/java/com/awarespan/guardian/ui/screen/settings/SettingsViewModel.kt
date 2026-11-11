package com.awarespan.guardian.ui.screen.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.awarespan.guardian.BuildConfig
import com.awarespan.guardian.data.local.dao.IncidentDao
import com.awarespan.guardian.domain.model.User
import com.awarespan.guardian.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class SettingsUiState(
    val user: User? = null,
    val endpointUrl: String = BuildConfig.API_BASE_URL,
    val appVersion: String = "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})",
    val cachedIncidentCount: Int = 0,
    val loggedOut: Boolean = false,
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val incidentDao: IncidentDao,
) : ViewModel() {
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState

    init {
        refresh()
    }

    fun refresh() {
        val user = authRepository.currentUser()
        viewModelScope.launch(Dispatchers.IO) {
            val recent = incidentDao.getRecent(1000)
            _uiState.value = _uiState.value.copy(
                user = user,
                cachedIncidentCount = recent.size,
                loggedOut = false
            )
        }
    }

    fun clearCache() {
        viewModelScope.launch(Dispatchers.IO) {
            incidentDao.pruneOlderThan(System.currentTimeMillis() + 86400000L) // wipe
            refresh()
        }
    }

    fun logout() {
        viewModelScope.launch(Dispatchers.IO) {
            authRepository.logout()
            _uiState.value = _uiState.value.copy(loggedOut = true)
        }
    }
}
