package com.awarespan.guardian.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.awarespan.guardian.BuildConfig;
import com.awarespan.guardian.data.local.dao.IncidentDao;
import com.awarespan.guardian.data.mapper.IncidentMapper;
import com.awarespan.guardian.data.remote.websocket.SipWebSocketClient;
import com.awarespan.guardian.data.remote.websocket.WebSocketEvent;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * Background event service. No permanent foreground notification is displayed.
 */
@AndroidEntryPoint
public class WebSocketService extends Service {

    public static final String EXTRA_BASE_URL = "base_url";

    @Inject SipWebSocketClient webSocketClient;
    @Inject IncidentDao incidentDao;
    @Inject IncidentMapper incidentMapper;

    private final SipWebSocketClient.Listener listener = new SipWebSocketClient.Listener() {
        @Override
        public void onEvent(WebSocketEvent event) {
            if (event instanceof WebSocketEvent.OnIncidentReceived) {
                WebSocketEvent.OnIncidentReceived e = (WebSocketEvent.OnIncidentReceived) event;
                if (e.incident == null || e.incident.id == null) return;
                incidentDao.upsert(incidentMapper.toEntity(e.incident));
                String threatType = e.incident.threat != null ? e.incident.threat.type
                        : e.incident.threatType != null ? e.incident.threatType : "UNKNOWN";
                String description = e.incident.threat != null ? e.incident.threat.description
                        : e.incident.threatDescription != null ? e.incident.threatDescription : "";
                NotificationService.notifyNewIncident(WebSocketService.this,
                        e.incident.id, threatType, description);
            } else if (event instanceof WebSocketEvent.OnIncidentUpdated) {
                WebSocketEvent.OnIncidentUpdated e = (WebSocketEvent.OnIncidentUpdated) event;
                if (e.incident != null && e.incident.id != null) {
                    incidentDao.upsert(incidentMapper.toEntity(e.incident));
                }
            } else if (event instanceof WebSocketEvent.OnSystemAlert) {
                WebSocketEvent.OnSystemAlert e = (WebSocketEvent.OnSystemAlert) event;
                NotificationService.notifySystemAlert(WebSocketService.this,
                        e.level, e.message);
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        NotificationService.ensureChannels(this);
        webSocketClient.addListener(listener);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String baseUrl = intent != null ? intent.getStringExtra(EXTRA_BASE_URL) : null;
        if (baseUrl == null || baseUrl.trim().isEmpty()) {
            baseUrl = BuildConfig.API_BASE_URL;
        }
        if (baseUrl != null && !baseUrl.trim().isEmpty()) {
            webSocketClient.connect(baseUrl);
        }
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        webSocketClient.removeListener(listener);
        webSocketClient.disconnect();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
