package com.awarespan.guardian.domain.model;

import org.junit.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static org.junit.Assert.*;

public class NodeTest {

    @Test
    public void nodeWithRecentHeartbeatIsOnline() {
        Node node = new Node("node-1", "Alpha Node",
                new Location(12.0, 77.0, "Loc", 1.0),
                NodeStatus.ONLINE,
                Instant.now().minus(Duration.ofSeconds(10)),
                95, "v1.0.0", AutopilotPolicy.disabled());

        assertTrue(node.isOnline());
        assertTrue(node.isFreshlyOnline());
        assertEquals(NodeStatus.ONLINE, node.getEffectiveStatus());
    }

    @Test
    public void nodeWithExpiredHeartbeatIsOfflineEvenIfReportedOnline() {
        Node node = new Node("node-2", "Beta Node",
                new Location(12.0, 77.0, "Loc", 1.0),
                NodeStatus.ONLINE,
                Instant.now().minus(Duration.ofSeconds(120)), // 2 minutes ago (> 90s)
                80, "v1.0.0", AutopilotPolicy.disabled());

        // Heartbeat policy detects expired silence
        assertFalse(node.isFreshlyOnline());
        assertEquals(NodeStatus.OFFLINE, node.getEffectiveStatus());
    }

    @Test
    public void nodeWithoutHeartbeatIsOffline() {
        Node node = new Node("node-3", "Gamma Node",
                new Location(12.0, 77.0, "Loc", 1.0),
                NodeStatus.ONLINE,
                null,
                50, "v1.0.0", AutopilotPolicy.disabled());

        assertFalse(node.isFreshlyOnline());
        assertEquals(NodeStatus.OFFLINE, node.getEffectiveStatus());
    }

    @Test
    public void nodePreservesVolunteerSources() {
        VolunteerSource cam = new VolunteerSource("cam-01", "Front Gate Camera",
                VolunteerSource.SourceType.CAMERA, VolunteerSource.SourceStatus.ACTIVE,
                "Pixel 8", Instant.now());
        VolunteerSource mic = new VolunteerSource("mic-01", "Perimeter Mic",
                VolunteerSource.SourceType.MICROPHONE, VolunteerSource.SourceStatus.IDLE,
                "USB Array", Instant.now());

        Node node = new Node("node-4", "Delta Node",
                new Location(12.0, 77.0, "Loc", 1.0),
                NodeStatus.ONLINE, Instant.now(), 100, "v1.0.0",
                AutopilotPolicy.disabled(), List.of(cam, mic));

        assertEquals(2, node.getVolunteerSources().size());
        assertEquals("cam-01", node.getVolunteerSources().get(0).getSourceId());
        assertEquals(VolunteerSource.SourceType.CAMERA, node.getVolunteerSources().get(0).getSourceType());
        assertTrue(node.getVolunteerSources().get(0).isActive());
    }
}
