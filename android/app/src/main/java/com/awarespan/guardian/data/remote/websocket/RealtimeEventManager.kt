package com.awarespan.guardian.data.remote.websocket

import android.content.Context
import com.awarespan.guardian.BuildConfig
import com.awarespan.guardian.data.local.dao.IncidentDao
import com.awarespan.guardian.data.mapper.IncidentMapper
import com.awarespan.guardian.service.NotificationService
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages the real-time WebSocket connection lifecycle.
 * Active strictly while the operator is in the foreground.
 * Shuts down upon backgrounding to eliminate battery and network drain.
 */
@Singleton
class RealtimeEventManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val webSocketClient: SipWebSocketClient,
    private val incidentDao: IncidentDao,
    private val incidentMapper: IncidentMapper,
    private val logger: com.awarespan.guardian.data.logging.DiagnosticLogger,
) {
    private var isConnected = false

    private val listener = SipWebSocketClient.Listener { event ->
        when (event) {
            is WebSocketEvent.OnConnectionStateChanged -> {
                logger.logRealtimeState(event.state.name, event.reason)
            }
            is WebSocketEvent.OnIncidentReceived -> {
                val incident = event.incident ?: return@Listener
                if (incident.id == null) return@Listener
                incidentDao.upsert(incidentMapper.toEntity(incident))
                logger.info("Realtime", "Received real-time incident ${incident.id} (Threat: ${incident.threatType})")

                val threatType = incident.threat?.type
                    ?: incident.threatType ?: "UNKNOWN"
                val description = incident.threat?.description
                    ?: incident.threatDescription ?: ""

                NotificationService.notifyNewIncident(
                    context,
                    incident.id,
                    threatType,
                    description
                )
            }
            is WebSocketEvent.OnIncidentUpdated -> {
                val incident = event.incident ?: return@Listener
                if (incident.id != null) {
                    incidentDao.upsert(incidentMapper.toEntity(incident))
                    logger.info("Realtime", "Updated real-time incident ${incident.id}")
                }
            }
            is WebSocketEvent.OnSystemAlert -> {
                logger.warn("Realtime", "System alert [${event.level}]: ${event.message}")
                NotificationService.notifySystemAlert(
                    context,
                    event.level,
                    event.message
                )
            }
            else -> {}
        }
    }

    fun startForegroundSession(baseUrl: String = BuildConfig.API_BASE_URL) {
        if (!isConnected) {
            logger.info("Realtime", "Starting foreground WebSocket telemetry session")
            webSocketClient.addListener(listener)
            webSocketClient.connect(baseUrl)
            isConnected = true
        }
    }

    fun stopForegroundSession() {
        if (isConnected) {
            logger.info("Realtime", "Stopping foreground WebSocket telemetry session")
            webSocketClient.removeListener(listener)
            webSocketClient.disconnect()
            isConnected = false
        }
    }
}
