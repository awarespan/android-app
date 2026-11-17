package com.awarespan.guardian.ui.screen.events

import androidx.lifecycle.ViewModel
import com.awarespan.guardian.data.remote.websocket.SipWebSocketClient
import com.awarespan.guardian.data.remote.websocket.WebSocketEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Instant
import java.time.OffsetDateTime
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class LiveEventEntry(
    val timestamp: Instant,
    val type: String,
    val title: String,
    val details: String,
    val isAlert: Boolean = false,
)

data class EventHistoryUiState(
    val events: List<LiveEventEntry> = emptyList(),
    val connectionState: WebSocketEvent.ConnectionState = WebSocketEvent.ConnectionState.DISCONNECTED,
)

@HiltViewModel
class EventHistoryViewModel @Inject constructor(
    private val webSocketClient: SipWebSocketClient,
) : ViewModel() {
    private val _uiState = MutableStateFlow(EventHistoryUiState())
    val uiState: StateFlow<EventHistoryUiState> = _uiState

    private val listener = SipWebSocketClient.Listener { event ->
        val now = Instant.now()
        val entry = when (event) {
            is WebSocketEvent.OnIncidentReceived -> {
                val incident = event.incident
                val responseEntries = incident.responseEvents.orEmpty()
                    .filter { it.actionType.equals("DETERRENCE", ignoreCase = true) }
                    .map { response ->
                        val responseTime = response.timestamp?.let {
                            runCatching { Instant.parse(it) }.getOrNull()
                                ?: runCatching { OffsetDateTime.parse(it).toInstant() }.getOrNull()
                        } ?: now
                        LiveEventEntry(
                            responseTime,
                            "PI_DETERRENCE",
                            if (response.result?.startsWith("STARTED") == true)
                                "Pi speaker warning started"
                            else "Pi speaker warning not started",
                            response.result ?: "The Pi recorded a deterrence response.",
                            isAlert = response.result?.startsWith("STARTED") == true,
                        )
                    }
                val detectionEntry = LiveEventEntry(
                    now,
                    "INCIDENT_NEW",
                    "New threat detected by ${incident.nodeId ?: "sensor"}",
                    buildString {
                        val detection = incident.detection
                        append("Classification: ${incident.threatType ?: "UNKNOWN"}")
                        if (detection?.predictedClass != null) {
                            append(" (${detection.predictedClass}")
                            append(detection.confidence.let { ", ${(it * 100).toInt()}%" })
                            append(")")
                        }
                        append(", State: ${incident.state ?: "DETECTED"}")
                    },
                    isAlert = true
                )
                _uiState.value = _uiState.value.copy(
                    events = (responseEntries + detectionEntry + _uiState.value.events).take(100)
                )
                null
            }
            is WebSocketEvent.OnIncidentUpdated -> LiveEventEntry(
                now,
                "INCIDENT_UPDATED",
                "Incident ${event.incidentId ?: ""} status updated",
                "New State: ${event.state ?: "UPDATED"}"
            )
            is WebSocketEvent.OnNodeStatusChanged -> LiveEventEntry(
                now,
                "NODE_STATUS",
                "Node ${event.nodeId} heartbeat: ${event.status}",
                "Timestamp: ${event.lastHeartbeat ?: "now"}"
            )
            is WebSocketEvent.OnPolicySynced -> LiveEventEntry(
                now,
                "POLICY_SYNCED",
                "Autopilot policy confirmed on ${event.nodeId}",
                "Sync State: ${event.syncState ?: "NOMINAL"}"
            )
            is WebSocketEvent.OnSystemAlert -> LiveEventEntry(
                now,
                "SYSTEM_ALERT",
                "System Alert [${event.level}]: ${event.message}",
                "Origin Node: ${event.nodeId ?: "CORE"}",
                isAlert = true
            )
            is WebSocketEvent.OnConnectionStateChanged -> {
                _uiState.value = _uiState.value.copy(connectionState = event.state)
                LiveEventEntry(
                    now,
                    "CONNECTION_CHANGE",
                    "Telemetry channel: ${event.state.name}",
                    event.reason ?: ""
                )
            }
            else -> null
        }

        if (entry != null) {
            val current = _uiState.value.events.toMutableList()
            current.add(0, entry)
            if (current.size > 100) current.removeAt(current.size - 1)
            _uiState.value = _uiState.value.copy(events = current)
        }
    }

    init {
        webSocketClient.addListener(listener)
    }

    override fun onCleared() {
        super.onCleared()
        webSocketClient.removeListener(listener)
    }

    fun clearLog() {
        _uiState.value = _uiState.value.copy(events = emptyList())
    }
}
