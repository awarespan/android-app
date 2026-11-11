package com.awarespan.guardian.ui.screen.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.awarespan.guardian.data.mapper.IncidentMapper
import com.awarespan.guardian.data.network.NetworkMonitor
import com.awarespan.guardian.data.remote.api.SipApiService
import com.awarespan.guardian.data.remote.dto.DashboardDto
import com.awarespan.guardian.domain.model.Incident
import com.awarespan.guardian.domain.model.IncidentState
import com.awarespan.guardian.domain.model.Node
import com.awarespan.guardian.domain.repository.IncidentRepository
import com.awarespan.guardian.domain.repository.NodeRepository
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
    val systemHealth: String = "NOMINAL",
    val recentIncidents: List<Incident> = emptyList(),
    val isOffline: Boolean = false,
    val isServingCache: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val api: SipApiService,
    private val incidentRepository: IncidentRepository,
    private val nodeRepository: NodeRepository,
    private val networkMonitor: NetworkMonitor,
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
            val hasNetwork = networkMonitor.isCurrentlyConnected()
            var isOffline = !hasNetwork
            var incidents: List<Incident> = emptyList()
            var nodes: List<Node> = emptyList()

            try {
                // Single source of truth for node states and heartbeats
                nodes = nodeRepository.getNodes()
            } catch (_: Exception) {
                isOffline = true
            }

            try {
                incidents = incidentRepository.getIncidents(0, 5, null, null, null, null, null)
            } catch (_: Exception) {
                isOffline = true
            }

            // Optional dashboard summary probe to verify API health
            if (hasNetwork) {
                try {
                    val r = api.getDashboard().execute()
                    if (r.isSuccessful && r.body() != null) {
                        val body = r.body()!!
                        if (body.recentIncidents != null && body.recentIncidents.isNotEmpty()) {
                            incidents = body.recentIncidents.mapNotNull {
                                try { incidentMapper.toDomain(it) } catch (_: Exception) { null }
                            }
                        }
                    }
                } catch (_: Exception) {
                    isOffline = true
                }
            }

            val totalNodes = nodes.size
            // Use heartbeat freshness policy to evaluate online nodes consistently
            val onlineNodes = nodes.count { it.isFreshlyOnline() }

            val activeThreatCount = incidents.count {
                it.state == IncidentState.DETECTED ||
                it.state == IncidentState.PENDING_VERIFICATION ||
                it.state == IncidentState.EVIDENCE_CAPTURED
            }

            val health = when {
                isOffline -> "OFFLINE"
                activeThreatCount > 0 -> "ATTENTION"
                else -> "NOMINAL"
            }

            _uiState.value = DashboardUiState(
                loading = false,
                totalNodes = totalNodes,
                onlineNodes = onlineNodes,
                activeThreats = activeThreatCount,
                systemHealth = health,
                recentIncidents = incidents,
                isOffline = isOffline,
                isServingCache = isOffline,
                error = if (isOffline) "Connection unavailable" else null,
            )
        }
    }
}
