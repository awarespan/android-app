package com.awarespan.guardian.ui.screen.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.awarespan.guardian.BuildConfig
import com.awarespan.guardian.data.local.dao.IncidentDao
import com.awarespan.guardian.domain.model.User
import com.awarespan.guardian.domain.repository.AuthRepository
import com.awarespan.guardian.service.IncidentSyncWorker
import com.awarespan.guardian.service.NotificationService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class SettingsUiState(
    val user: User? = null,
    val endpointUrl: String = BuildConfig.API_BASE_URL,
    val appVersion: String = "${BuildConfig.VERSION_NAME} (Build ${BuildConfig.VERSION_CODE})",
    val cachedIncidentCount: Int = 0,
    val notificationsEnabled: Boolean = true,
    val cacheCleared: Boolean = false,
    val loggedOut: Boolean = false,
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
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
        val notifEnabled = NotificationService.isNotificationsEnabled(context)
        viewModelScope.launch(Dispatchers.IO) {
            val recent = incidentDao.getRecent(1000)
            _uiState.value = _uiState.value.copy(
                user = user,
                cachedIncidentCount = recent.size,
                notificationsEnabled = notifEnabled,
                cacheCleared = false,
                loggedOut = false,
            )
        }
    }

    fun toggleNotifications(enabled: Boolean) {
        NotificationService.setNotificationsEnabled(context, enabled)
        _uiState.value = _uiState.value.copy(notificationsEnabled = enabled)
    }

    fun clearCache() {
        viewModelScope.launch(Dispatchers.IO) {
            incidentDao.pruneOlderThan(System.currentTimeMillis() + 86400000L)
            val remaining = incidentDao.getRecent(1000).size
            _uiState.value = _uiState.value.copy(
                cachedIncidentCount = remaining,
                cacheCleared = true,
            )
        }
    }

    fun logout() {
        viewModelScope.launch(Dispatchers.IO) {
            IncidentSyncWorker.cancel(context)
            authRepository.logout()
            _uiState.value = _uiState.value.copy(loggedOut = true)
        }
    }
}
