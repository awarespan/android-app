package com.awarespan.guardian.ui.screen.incidents

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.awarespan.guardian.domain.model.HumanLabel
import com.awarespan.guardian.domain.model.Incident
import com.awarespan.guardian.domain.repository.IncidentRepository
import com.awarespan.guardian.domain.repository.EvidenceRepository
import com.awarespan.guardian.domain.model.EvidenceType
import java.io.File
import com.awarespan.guardian.domain.usecase.VerifyIncidentUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class IncidentDetailUiState(
    val loading: Boolean = true,
    val incident: Incident? = null,
    val error: String? = null,
    val submitting: Boolean = false,
    val completed: Boolean = false,
    val evidenceImage: File? = null,
)

@HiltViewModel
class IncidentDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: IncidentRepository,
    private val evidenceRepository: EvidenceRepository,
    private val verifyIncident: VerifyIncidentUseCase,
) : ViewModel() {
    private val incidentId: String = checkNotNull(savedStateHandle["incidentId"])
    private val _uiState = MutableStateFlow(IncidentDetailUiState())
    val uiState: StateFlow<IncidentDetailUiState> = _uiState

    init { refresh() }

    fun refresh() {
        _uiState.value = _uiState.value.copy(loading = true, error = null)
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val incident = repository.getIncidentById(incidentId)
                if (incident == null) {
                    _uiState.value = IncidentDetailUiState(loading = false, error = "Incident not found")
                } else {
                    val image = if (incident.evidenceBundle?.hasImage() == true) {
                        runCatching { evidenceRepository.getEvidenceFile(incidentId, incident.evidenceBundle, EvidenceType.IMAGE) }.getOrNull()
                    } else null
                    _uiState.value = IncidentDetailUiState(loading = false, incident = incident, evidenceImage = image)
                }
            } catch (e: Exception) {
                _uiState.value = IncidentDetailUiState(
                    loading = false,
                    error = e.message ?: "Unable to load incident",
                )
            }
        }
    }

    fun verify(label: HumanLabel, notes: String, deterrenceAction: String) {
        val combinedNotes = buildString {
            if (notes.isNotBlank()) append(notes.trim())
            if (deterrenceAction.isNotBlank()) {
                if (isNotEmpty()) append("\n")
                append("Deterrence action taken: ").append(deterrenceAction.trim())
            }
        }
        _uiState.value = _uiState.value.copy(submitting = true, error = null)
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val result = verifyIncident.execute(incidentId, label, combinedNotes)
                _uiState.value = if (result.isSuccess) {
                    IncidentDetailUiState(
                        loading = false,
                        incident = result.incident,
                        completed = true,
                    )
                } else {
                    _uiState.value.copy(
                        submitting = false,
                        error = result.error ?: "Verification failed",
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    submitting = false,
                    error = e.message ?: "Verification failed",
                )
            }
        }
    }
}
