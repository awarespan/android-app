package com.awarespan.guardian.ui.screen.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.awarespan.guardian.data.mapper.IncidentMapper
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
    private val incidentMapper: IncidentMapper,
    private val logger: com.awarespan.guardian.data.logging.DiagnosticLogger,
) : ViewModel() {
    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState

    init {
        refresh()
    }

    fun refresh() {
        _uiState.value = _uiState.value.copy(loading = true, error = null)
        viewModelScope.launch(Dispatchers.IO) {
            // Android's INTERNET capability is not a backend reachability check.
            // A node may be reachable over a private LAN/VPN without general internet.
            var isOffline = false
            var isServingCache = false
            var incidents: List<Incident> = emptyList()
            var nodes: List<Node> = emptyList()

            try {
                // Single source of truth for node states and heartbeats
                nodes = nodeRepository.getNodes()
            } catch (e: Exception) {
                isOffline = true
                isServingCache = true
                logger.warn("Dashboard", "Failed to load nodes", e)
            }

            try {
                incidents = incidentRepository.getIncidents(0, 5, null, null, null, null, null)
            } catch (e: Exception) {
                isOffline = true
                isServingCache = true
                logger.warn("Dashboard", "Failed to load incidents", e)
            }

            // Probe the actual backend regardless of Android's generic INTERNET flag.
            // The API may be reachable over LAN/VPN even when Android reports no internet.
            try {
                val r = api.getDashboard().execute()
                if (r.isSuccessful && r.body() != null) {
                    val body = r.body()!!
                    isOffline = false
                    if (body.recentIncidents != null && body.recentIncidents.isNotEmpty()) {
                        incidents = body.recentIncidents.mapNotNull {
                            try { incidentMapper.toDomain(it) } catch (_: Exception) { null }
                        }
                    }
                } else {
                    isOffline = true
                    isServingCache = true
                    logger.warn("Dashboard", "Backend health probe returned HTTP ${r.code()}")
                }
            } catch (e: Exception) {
                isOffline = true
                isServingCache = true
                logger.warn("Dashboard", "Backend health probe failed", e)
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

            logger.info("Dashboard", "Telemetry refreshed: $onlineNodes/$totalNodes online, $activeThreatCount active threats (Offline: $isOffline, Cached: $isServingCache)")

            _uiState.value = DashboardUiState(
                loading = false,
                totalNodes = totalNodes,
                onlineNodes = onlineNodes,
                activeThreats = activeThreatCount,
                systemHealth = health,
                recentIncidents = incidents,
                isOffline = isOffline,
                isServingCache = isServingCache,
                error = if (isOffline) "Showing previously saved information." else null,
            )
        }
    }
}
