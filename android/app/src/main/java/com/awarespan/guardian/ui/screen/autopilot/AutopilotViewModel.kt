package com.awarespan.guardian.ui.screen.autopilot

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.awarespan.guardian.domain.model.AutopilotPolicy
import com.awarespan.guardian.domain.model.AutopilotState
import com.awarespan.guardian.domain.model.EscalationRules
import com.awarespan.guardian.domain.model.SafetyConstraints
import com.awarespan.guardian.domain.model.ThreatType
import com.awarespan.guardian.domain.usecase.GetAutopilotPolicyUseCase
import com.awarespan.guardian.domain.usecase.UpdateAutopilotPolicyUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Instant
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

enum class PolicySyncStatus {
    SYNCHRONIZED,
    PENDING_LOCAL_CHANGES,
    APPLYING_CHANGES,
    SYNC_FAILED,
}

data class AutopilotUiState(
    val loading: Boolean = true,
    val nodeId: String = "",
    val enabled: Boolean = false,
    val confidenceThreshold: Double = 0.85,
    val deterrenceTimeoutSeconds: Int = 5,
    val allowedThreatTypes: Set<ThreatType> = setOf(ThreatType.WEAPON, ThreatType.ASSAULT),
    val lastSyncTimestamp: Instant? = null,
    val syncStatus: PolicySyncStatus = PolicySyncStatus.SYNCHRONIZED,
    val statusMessage: String? = null,
    val error: String? = null,
)

@HiltViewModel
class AutopilotViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getPolicyUseCase: GetAutopilotPolicyUseCase,
    private val updatePolicyUseCase: UpdateAutopilotPolicyUseCase,
) : ViewModel() {
    private val initialNodeId: String = savedStateHandle["nodeId"] ?: "global"
    private val _uiState = MutableStateFlow(AutopilotUiState(nodeId = initialNodeId))
    val uiState: StateFlow<AutopilotUiState> = _uiState

    init {
        loadPolicy(initialNodeId)
    }

    fun loadPolicy(nodeId: String) {
        _uiState.value = _uiState.value.copy(loading = true, error = null, nodeId = nodeId)
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val policy = getPolicyUseCase.execute(nodeId)
                _uiState.value = _uiState.value.copy(
                    loading = false,
                    enabled = policy.isEnabled,
                    confidenceThreshold = policy.confidenceThreshold,
                    deterrenceTimeoutSeconds = policy.deterrenceTimeoutSeconds,
                    allowedThreatTypes = policy.allowedThreatTypes,
                    lastSyncTimestamp = policy.lastSyncTimestamp,
                    syncStatus = PolicySyncStatus.SYNCHRONIZED,
                    statusMessage = "Policy synchronized with backend system of record.",
                    error = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    loading = false,
                    error = "Failed to load policy: ${e.message ?: "unknown"}",
                    syncStatus = PolicySyncStatus.SYNC_FAILED,
                )
            }
        }
    }

    fun toggleEnabled(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(
            enabled = enabled,
            syncStatus = PolicySyncStatus.PENDING_LOCAL_CHANGES
        )
    }

    fun setConfidenceThreshold(threshold: Double) {
        _uiState.value = _uiState.value.copy(
            confidenceThreshold = threshold.coerceIn(0.5, 0.99),
            syncStatus = PolicySyncStatus.PENDING_LOCAL_CHANGES
        )
    }

    fun setDeterrenceTimeout(seconds: Int) {
        _uiState.value = _uiState.value.copy(
            deterrenceTimeoutSeconds = seconds.coerceIn(1, 60),
            syncStatus = PolicySyncStatus.PENDING_LOCAL_CHANGES
        )
    }

    fun toggleThreatType(type: ThreatType) {
        val current = _uiState.value.allowedThreatTypes.toMutableSet()
        if (current.contains(type)) current.remove(type) else current.add(type)
        _uiState.value = _uiState.value.copy(
            allowedThreatTypes = current,
            syncStatus = PolicySyncStatus.PENDING_LOCAL_CHANGES
        )
    }

    fun applyPolicyChanges() {
        val s = _uiState.value
        _uiState.value = s.copy(
            syncStatus = PolicySyncStatus.APPLYING_CHANGES,
            error = null,
            statusMessage = "Transmitting policy rules to backend…"
        )

        val newPolicy = AutopilotPolicy(
            s.enabled,
            s.allowedThreatTypes,
            s.confidenceThreshold,
            s.deterrenceTimeoutSeconds,
            EscalationRules.defaults(),
            SafetyConstraints.defaults(),
            AutopilotState.ACTIVE,
            Instant.now()
        )

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val confirmed = updatePolicyUseCase.execute(s.nodeId, newPolicy)
                _uiState.value = _uiState.value.copy(
                    enabled = confirmed.isEnabled,
                    confidenceThreshold = confirmed.confidenceThreshold,
                    deterrenceTimeoutSeconds = confirmed.deterrenceTimeoutSeconds,
                    allowedThreatTypes = confirmed.allowedThreatTypes,
                    lastSyncTimestamp = confirmed.lastSyncTimestamp ?: Instant.now(),
                    syncStatus = PolicySyncStatus.SYNCHRONIZED,
                    statusMessage = "Confirmed: Policy applied and active on edge node.",
                    error = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    syncStatus = PolicySyncStatus.SYNC_FAILED,
                    error = "Policy synchronisation rejected by backend: ${e.message}",
                    statusMessage = null
                )
            }
        }
    }
}
