package com.awarespan.guardian.ui.screen.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.awarespan.guardian.data.mapper.IncidentMapper
import com.awarespan.guardian.data.remote.api.SipApiService
import com.awarespan.guardian.data.remote.dto.DashboardDto
import com.awarespan.guardian.domain.model.Incident
import com.awarespan.guardian.domain.repository.IncidentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class DashboardUiState(
    val loading: Boolean = true,
    val totalNodes: Int = 0,
    val onlineNodes: Int = 0,
    val activeThreats: Int = 0,
    val systemHealth: String = "UNKNOWN",
    val recentIncidents: List<Incident> = emptyList(),
    val isOffline: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val api: SipApiService,
    private val incidentRepository: IncidentRepository,
    private val incidentMapper: IncidentMapper,
) : ViewModel() {
    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState

    init {
        refresh()
    }

    fun refresh() {
        _uiState.value = _uiState.value.copy(loading = true, error = null)
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val r = api.getDashboard().execute()
                val body: DashboardDto? = if (r.isSuccessful) r.body() else null
                if (body != null) {
                    val incidents = if (body.recentIncidents != null) {
                        body.recentIncidents.mapNotNull {
                            try { incidentMapper.toDomain(it) } catch (_: Exception) { null }
                        }
                    } else {
                        incidentRepository.getIncidents(0, 5, null, null, null, null, null)
                    }

                    _uiState.value = DashboardUiState(
                        loading = false,
                        totalNodes = body.totalNodes,
                        onlineNodes = body.onlineNodes,
                        activeThreats = body.activeThreats,
                        systemHealth = body.systemHealth ?: "GREEN",
                        recentIncidents = incidents,
                        isOffline = false,
                        error = null,
                    )
                } else {
                    fallbackToCache("Backend returned HTTP ${r.code()}")
                }
            } catch (e: Exception) {
                fallbackToCache("Backend unreachable: ${e.message ?: "offline"}")
            }
        }
    }

    private fun fallbackToCache(reason: String) {
        val cached = incidentRepository.getIncidents(0, 5, null, null, null, null, null)
        val activeCount = cached.count { it.state.name == "DETECTED" || it.state.name == "PENDING_VERIFICATION" }
        _uiState.value = _uiState.value.copy(
            loading = false,
            systemHealth = if (activeCount > 0) "YELLOW" else "GREEN",
            activeThreats = activeCount,
            recentIncidents = cached,
            isOffline = true,
            error = reason,
        )
    }
}
