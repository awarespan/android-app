package com.awarespan.guardian.ui.screen.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.awarespan.guardian.domain.model.Incident
import com.awarespan.guardian.domain.model.Node
import com.awarespan.guardian.domain.repository.IncidentRepository
import com.awarespan.guardian.domain.repository.NodeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class MapUiState(
    val loading: Boolean = true,
    val nodesWithLocation: List<Node> = emptyList(),
    val nodesWithoutLocation: List<Node> = emptyList(),
    val incidentsWithLocation: List<Incident> = emptyList(),
    val incidentsWithoutLocation: List<Incident> = emptyList(),
    val error: String? = null,
)

@HiltViewModel
class MapViewModel @Inject constructor(
    private val nodeRepository: NodeRepository,
    private val incidentRepository: IncidentRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(MapUiState())
    val uiState: StateFlow<MapUiState> = _uiState

    init {
        refresh()
    }

    fun refresh() {
        _uiState.value = _uiState.value.copy(loading = true, error = null)
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val nodes = nodeRepository.getNodes()
                val incidents = incidentRepository.getIncidents(0, 50, null, null, null, null, null)

                val (nWithLoc, nNoLoc) = nodes.partition {
                    it.location != null && (it.location.latitude != 0.0 || it.location.longitude != 0.0)
                }

                val (iWithLoc, iNoLoc) = incidents.partition {
                    it.location != null && (it.location.latitude != 0.0 || it.location.longitude != 0.0)
                }

                _uiState.value = MapUiState(
                    loading = false,
                    nodesWithLocation = nWithLoc,
                    nodesWithoutLocation = nNoLoc,
                    incidentsWithLocation = iWithLoc,
                    incidentsWithoutLocation = iNoLoc,
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    loading = false,
                    error = e.message ?: "Failed to load sector coordinate telemetry"
                )
            }
        }
    }
}
