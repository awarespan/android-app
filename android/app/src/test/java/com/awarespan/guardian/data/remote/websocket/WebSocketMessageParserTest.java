package com.awarespan.guardian.data.remote.websocket;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import org.junit.Test;

public class WebSocketMessageParserTest {
    private final WebSocketMessageParser parser = new WebSocketMessageParser();

    @Test public void parsesIncidentEvent() {
        WebSocketEvent event = parser.parse(
                "{\"type\":\"INCIDENT_NEW\",\"payload\":{\"id\":\"i-1\",\"state\":\"DETECTED\",\"threatType\":\"WEAPON\"}}");
        assertNotNull(event);
        assertNotNull(((WebSocketEvent.OnIncidentReceived) event).incident);
        assertEquals("WEAPON", ((WebSocketEvent.OnIncidentReceived) event).incident.threatType);
    }

    @Test public void acceptsBackendIncidentUpdatedPayload() {
        WebSocketEvent event = parser.parse(
                "{\"type\":\"INCIDENT_UPDATED\",\"payload\":{\"id\":\"i-2\",\"state\":\"VERIFIED\"}}");
        assertNotNull(event);
        WebSocketEvent.OnIncidentUpdated updated = (WebSocketEvent.OnIncidentUpdated) event;
        assertEquals("i-2", updated.incidentId);
        assertEquals("VERIFIED", updated.state);
        assertNotNull(updated.incident);
        assertEquals("i-2", updated.incident.id);
        assertEquals("VERIFIED", updated.incident.state);
    }

    @Test public void acceptsBackendNodeStatusEventName() {
        WebSocketEvent event = parser.parse(
                "{\"type\":\"NODE_STATUS_CHANGED\",\"payload\":{\"id\":\"node-1\",\"status\":\"ONLINE\"}}");
        assertNotNull(event);
        WebSocketEvent.OnNodeStatusChanged status = (WebSocketEvent.OnNodeStatusChanged) event;
        assertEquals("node-1", status.nodeId);
        assertEquals("ONLINE", status.status);
    }

    @Test public void parsesPersistedPiDetectionLabelsAndConfidence() {
        WebSocketEvent event = parser.parse(
                "{\"type\":\"INCIDENT_NEW\",\"payload\":{" +
                "\"id\":\"i-detection\",\"threatType\":\"WEAPON\"," +
                "\"detection\":{" +
                "\"predictedClass\":\"gun,knife\",\"confidence\":0.93," +
                "\"modelVersion\":\"awarespan-weapon-yolov8n:1.0.0\"}}}");
        assertNotNull(event);
        var detection = ((WebSocketEvent.OnIncidentReceived) event).incident.detection;
        assertNotNull(detection);
        assertEquals("gun,knife", detection.predictedClass);
        assertEquals(0.93, detection.confidence, 0.0001);
    }

    @Test public void dropsMalformedMessages() {
        assertNull(parser.parse("{not-json"));
        assertNull(parser.parse("{\"type\":\"UNKNOWN\"}"));
    }
    @Test public void parsesPersistedPiDeterrenceResultInIncidentBroadcast() {
        WebSocketEvent event = parser.parse(
                "{\"type\":\"INCIDENT_NEW\",\"payload\":{" +
                "\"id\":\"i-speaker\",\"state\":\"DETECTED\"," +
                "\"responseEvents\":[{" +
                "\"id\":\"r-speaker\",\"actionType\":\"DETERRENCE\"," +
                "\"timestamp\":\"2026-11-16T10:00:00Z\"," +
                "\"result\":\"STARTED; frequency=977Hz; duration=1.5s\"," +
                "\"autonomous\":true}]}}");
        assertNotNull(event);
        var incident = ((WebSocketEvent.OnIncidentReceived) event).incident;
        assertEquals(1, incident.responseEvents.size());
        assertEquals("DETERRENCE", incident.responseEvents.get(0).actionType);
        assertEquals("STARTED; frequency=977Hz; duration=1.5s", incident.responseEvents.get(0).result);
    }

}
