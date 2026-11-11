package com.awarespan.guardian.ui.screen.incidents

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.awarespan.guardian.domain.model.Incident
import com.awarespan.guardian.domain.model.IncidentState
import com.awarespan.guardian.domain.model.ThreatType
import com.awarespan.guardian.domain.usecase.GetIncidentsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class FeedUiState(
    val loading: Boolean = true,
    val incidents: List<Incident> = emptyList(),
    val filteredIncidents: List<Incident> = emptyList(),
    val stateFilter: IncidentState? = null,
    val typeFilter: ThreatType? = null,
    val searchQuery: String = "",
    val offline: Boolean = false,
)

@HiltViewModel
class IncidentFeedViewModel @Inject constructor(
    private val getIncidents: GetIncidentsUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(FeedUiState())
    val uiState: StateFlow<FeedUiState> = _uiState

    init { refresh() }

    fun refresh() {
        val s = _uiState.value
        _uiState.value = s.copy(loading = true, offline = false)
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val incidents = getIncidents.execute(
                    0, 100, s.stateFilter, s.typeFilter, null, null, null
                )
                val filtered = applyFilter(incidents, s.searchQuery)
                _uiState.value = s.copy(
                    loading = false,
                    incidents = incidents,
                    filteredIncidents = filtered,
                    offline = false,
                )
            } catch (_: Exception) {
                val current = _uiState.value
                val filtered = applyFilter(current.incidents, current.searchQuery)
                _uiState.value = current.copy(
                    loading = false,
                    filteredIncidents = filtered,
                    offline = true
                )
            }
        }
    }

    fun setStateFilter(filter: IncidentState?) {
        _uiState.value = _uiState.value.copy(stateFilter = filter)
        refresh()
    }

    fun setSearchQuery(query: String) {
        val s = _uiState.value
        val filtered = applyFilter(s.incidents, query)
        _uiState.value = s.copy(searchQuery = query, filteredIncidents = filtered)
    }

    private fun applyFilter(list: List<Incident>, query: String): List<Incident> {
        if (query.isBlank()) return list
        val q = query.trim().lowercase()
        return list.filter { incident ->
            incident.id.lowercase().contains(q) ||
            incident.threat.type.name.lowercase().contains(q) ||
            incident.threat.description.lowercase().contains(q) ||
            (incident.nodeId != null && incident.nodeId.lowercase().contains(q)) ||
            (incident.location?.humanReadable != null && incident.location.humanReadable.lowercase().contains(q))
        }
    }
}
