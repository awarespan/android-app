package com.awarespan.guardian.ui.screen.incidents

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.awarespan.guardian.data.network.NetworkMonitor
import com.awarespan.guardian.domain.model.Incident
import com.awarespan.guardian.domain.model.IncidentState
import com.awarespan.guardian.domain.model.ThreatType
import com.awarespan.guardian.domain.repository.IncidentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

enum class FeedFilterCategory(val label: String) {
    ALL("ALL"),
    PENDING("PENDING"),
    VERIFIED("VERIFIED"),
    REJECTED("REJECTED"),
    ESCALATED("ESCALATED");

    fun matches(state: IncidentState?): Boolean {
        if (state == null) return false
        return when (this) {
            ALL -> true
            PENDING -> state == IncidentState.PENDING_VERIFICATION ||
                    state == IncidentState.DETECTED ||
                    state == IncidentState.EVIDENCE_CAPTURED
            VERIFIED -> state == IncidentState.VERIFIED
            REJECTED -> state == IncidentState.REJECTED
            ESCALATED -> state == IncidentState.ESCALATED
        }
    }
}

data class FeedUiState(
    val loading: Boolean = true,
    val incidents: List<Incident> = emptyList(),
    val filteredIncidents: List<Incident> = emptyList(),
    val filterCategory: FeedFilterCategory = FeedFilterCategory.ALL,
    val typeFilter: ThreatType? = null,
    val searchQuery: String = "",
    val isOffline: Boolean = false,
    val isServingCache: Boolean = false,
)

@HiltViewModel
class IncidentFeedViewModel @Inject constructor(
    private val incidentRepository: IncidentRepository,
    private val networkMonitor: NetworkMonitor,
) : ViewModel() {
    private val _uiState = MutableStateFlow(FeedUiState())
    val uiState: StateFlow<FeedUiState> = _uiState

    init {
        refresh()
    }

    fun refresh() {
        val s = _uiState.value
        _uiState.value = s.copy(loading = true)
        viewModelScope.launch(Dispatchers.IO) {
            val isConnected = networkMonitor.isCurrentlyConnected()
            try {
                // Fetch up to 100 recent incidents (repository handles network fetch and Room fallback)
                val rawIncidents = incidentRepository.getIncidents(
                    0, 100, null, s.typeFilter, null, null, null
                )

                // Deduplicate by ID and sort chronologically descending
                val deduplicated = rawIncidents
                    .distinctBy { it.id }
                    .sortedByDescending { it.timestamp }

                val isServingCache = !isConnected
                val filtered = applyFilter(deduplicated, s.filterCategory, s.searchQuery)

                _uiState.value = s.copy(
                    loading = false,
                    incidents = deduplicated,
                    filteredIncidents = filtered,
                    isOffline = !isConnected,
                    isServingCache = isServingCache,
                )
            } catch (_: Exception) {
                // Network or repository exception: serve cached data from memory/Room
                val current = _uiState.value
                val filtered = applyFilter(current.incidents, current.filterCategory, current.searchQuery)
                _uiState.value = current.copy(
                    loading = false,
                    filteredIncidents = filtered,
                    isOffline = true,
                    isServingCache = true,
                )
            }
        }
    }

    fun setFilterCategory(category: FeedFilterCategory) {
        val s = _uiState.value
        val filtered = applyFilter(s.incidents, category, s.searchQuery)
        _uiState.value = s.copy(filterCategory = category, filteredIncidents = filtered)
    }

    fun setSearchQuery(query: String) {
        val s = _uiState.value
        val filtered = applyFilter(s.incidents, s.filterCategory, query)
        _uiState.value = s.copy(searchQuery = query, filteredIncidents = filtered)
    }

    private fun applyFilter(
        list: List<Incident>,
        category: FeedFilterCategory,
        query: String,
    ): List<Incident> {
        return list.filter { incident ->
            // Category filter
            val matchesCategory = category == FeedFilterCategory.ALL || category.matches(incident.state)

            // Search query filter across threat type, description, node ID, and location
            val matchesQuery = if (query.isBlank()) {
                true
            } else {
                val q = query.trim().lowercase()
                (incident.id.lowercase().contains(q)) ||
                (incident.threat?.type?.name?.lowercase()?.contains(q) == true) ||
                (incident.threat?.description?.lowercase()?.contains(q) == true) ||
                (incident.nodeId?.lowercase()?.contains(q) == true) ||
                (incident.sourceDeviceId?.lowercase()?.contains(q) == true) ||
                (incident.location?.humanReadable?.lowercase()?.contains(q) == true)
            }

            matchesCategory && matchesQuery
        }
    }
}
