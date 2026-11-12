package com.awarespan.guardian.ui.screen.settings

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.awarespan.guardian.BuildConfig
import com.awarespan.guardian.data.local.dao.IncidentDao
import com.awarespan.guardian.data.local.dao.NodeDao
import com.awarespan.guardian.data.logging.DiagnosticLogger
import com.awarespan.guardian.data.network.NetworkMonitor
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
import kotlinx.coroutines.withContext

data class SettingsUiState(
    val user: User? = null,
    val appVersion: String = "${BuildConfig.VERSION_NAME} (Build ${BuildConfig.VERSION_CODE})",
    val cachedIncidentCount: Int = 0,
    val notificationsEnabled: Boolean = true,
    val cacheCleared: Boolean = false,
    val isExporting: Boolean = false,
    val exportMessage: String? = null,
    val loggedOut: Boolean = false,
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val authRepository: AuthRepository,
    private val incidentDao: IncidentDao,
    private val nodeDao: NodeDao,
    private val networkMonitor: NetworkMonitor,
    private val diagnosticLogger: DiagnosticLogger,
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
                isExporting = false,
                exportMessage = null,
                loggedOut = false,
            )
        }
    }

    fun toggleNotifications(enabled: Boolean) {
        NotificationService.setNotificationsEnabled(context, enabled)
        diagnosticLogger.info("Settings", "Notification preferences toggled: $enabled")
        _uiState.value = _uiState.value.copy(notificationsEnabled = enabled)
    }

    fun clearCache() {
        viewModelScope.launch(Dispatchers.IO) {
            incidentDao.pruneOlderThan(System.currentTimeMillis() + 86400000L)
            val remaining = incidentDao.getRecent(1000).size
            diagnosticLogger.info("Storage", "Operator cleared incident cache. Remaining: $remaining")
            _uiState.value = _uiState.value.copy(
                cachedIncidentCount = remaining,
                cacheCleared = true,
            )
        }
    }

    fun exportDiagnostics(onShareIntentReady: (Intent) -> Unit) {
        _uiState.value = _uiState.value.copy(isExporting = true, exportMessage = null)
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val metadata = mapOf(
                    "networkConnected" to networkMonitor.isCurrentlyConnected().toString(),
                    "cachedIncidents" to incidentDao.getRecent(1000).size.toString(),
                    "cachedNodes" to nodeDao.getAll().size.toString(),
                    "operatorUser" to (authRepository.currentUser()?.username ?: "ANONYMOUS"),
                )
                val zip = diagnosticLogger.createDiagnosticArchive(metadata)
                diagnosticLogger.info("Diagnostics", "Diagnostic archive generated: ${zip.name} (${zip.length()} bytes)")
                val shareIntent = diagnosticLogger.createShareIntent(zip)
                withContext(Dispatchers.Main) {
                    _uiState.value = _uiState.value.copy(
                        isExporting = false,
                        exportMessage = "Diagnostics archive ready for export"
                    )
                    onShareIntentReady(shareIntent)
                }
            } catch (e: Exception) {
                diagnosticLogger.error("Diagnostics", "Failed to create diagnostic export archive", e)
                withContext(Dispatchers.Main) {
                    _uiState.value = _uiState.value.copy(
                        isExporting = false,
                        exportMessage = "Unable to generate diagnostics archive"
                    )
                }
            }
        }
    }

    fun logout() {
        viewModelScope.launch(Dispatchers.IO) {
            diagnosticLogger.logAuthEvent("logout", true, "Operator signed out of console")
            IncidentSyncWorker.cancel(context)
            authRepository.logout()
            _uiState.value = _uiState.value.copy(loggedOut = true)
        }
    }
}
