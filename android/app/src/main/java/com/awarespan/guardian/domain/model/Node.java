package com.awarespan.guardian.domain.model;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.List;

/** A Raspberry Pi edge node in the mesh (plan §20 heartbeat logic). */
public final class Node {
    public static final Duration HEARTBEAT_INTERVAL = Duration.ofSeconds(30);
    public static final Duration DEGRADED_AFTER = Duration.ofSeconds(90);   // 1 missed cycle
    public static final Duration OFFLINE_AFTER = Duration.ofSeconds(90);    // 3+ missed cycles

    private final String nodeId;
    private final String name;
    private final Location location;
    private final NodeStatus status;
    private final Instant lastHeartbeat;
    private final Integer batteryLevel;
    private final String firmwareVersion;
    private final AutopilotPolicy autopilotPolicy;
    private final List<VolunteerSource> volunteerSources;

    public Node(String nodeId, String name, Location location, NodeStatus status,
                Instant lastHeartbeat, Integer batteryLevel, String firmwareVersion,
                AutopilotPolicy autopilotPolicy) {
        this(nodeId, name, location, status, lastHeartbeat, batteryLevel, firmwareVersion, autopilotPolicy, Collections.emptyList());
    }

    public Node(String nodeId, String name, Location location, NodeStatus status,
                Instant lastHeartbeat, Integer batteryLevel, String firmwareVersion,
                AutopilotPolicy autopilotPolicy, List<VolunteerSource> volunteerSources) {
        this.nodeId = nodeId;
        this.name = name;
        this.location = location;
        this.status = status != null ? status : NodeStatus.UNKNOWN;
        this.lastHeartbeat = lastHeartbeat;
        this.batteryLevel = batteryLevel;
        this.firmwareVersion = firmwareVersion;
        this.autopilotPolicy = autopilotPolicy;
        this.volunteerSources = volunteerSources != null ? Collections.unmodifiableList(volunteerSources) : Collections.emptyList();
    }

    public String getNodeId() { return nodeId; }
    public String getName() { return name; }
    public Location getLocation() { return location; }
    public NodeStatus getStatus() { return status; }
    public Instant getLastHeartbeat() { return lastHeartbeat; }
    public Integer getBatteryLevel() { return batteryLevel; }
    public String getFirmwareVersion() { return firmwareVersion; }
    public AutopilotPolicy getAutopilotPolicy() { return autopilotPolicy; }
    public List<VolunteerSource> getVolunteerSources() { return volunteerSources; }

    /**
     * Determines whether the node is online according to the heartbeat freshness policy.
     * A node must report status ONLINE, have a valid heartbeat timestamp, and not exceed the stale threshold.
     */
    public boolean isOnline() {
        return isFreshlyOnline(OFFLINE_AFTER);
    }

    public boolean isFreshlyOnline() {
        return isFreshlyOnline(OFFLINE_AFTER);
    }

    public boolean isFreshlyOnline(Duration threshold) {
        if (status != NodeStatus.ONLINE) return false;
        if (lastHeartbeat == null) return false;
        return !isStale(threshold);
    }

    /** Returns the evaluated effective status factoring in heartbeat freshness. */
    public NodeStatus getEffectiveStatus() {
        return getEffectiveStatus(OFFLINE_AFTER);
    }

    public NodeStatus getEffectiveStatus(Duration threshold) {
        if (status == NodeStatus.OFFLINE) return NodeStatus.OFFLINE;
        if (lastHeartbeat == null || isStale(threshold)) return NodeStatus.OFFLINE;
        return status;
    }

    /** Heartbeat staleness — mirrors NodeService evaluation on the gateway. */
    public boolean isStale(Duration silence) {
        if (lastHeartbeat == null) return true;
        return Duration.between(lastHeartbeat, Instant.now()).compareTo(silence) > 0;
    }

    public Duration getTimeSinceHeartbeat() {
        if (lastHeartbeat == null) return Duration.ZERO;
        return Duration.between(lastHeartbeat, Instant.now());
    }
}
