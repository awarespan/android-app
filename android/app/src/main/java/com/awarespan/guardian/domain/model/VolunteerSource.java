package com.awarespan.guardian.domain.model;

import java.time.Instant;

/**
 * A volunteer device contributing camera, microphone, or sensor streams to an Edge Node.
 * Threat inference is executed on the Edge Node (Raspberry Pi); the Android console
 * receives and inspects the incident telemetry and source attribution.
 */
public final class VolunteerSource {

    public enum SourceType {
        CAMERA,
        MICROPHONE,
        SENSOR,
        ENVIRONMENTAL;

        public static SourceType fromString(String value) {
            if (value == null) return SENSOR;
            try {
                return SourceType.valueOf(value.trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                return SENSOR;
            }
        }
    }

    public enum SourceStatus {
        ACTIVE,
        IDLE,
        DISCONNECTED,
        ERROR;

        public static SourceStatus fromString(String value) {
            if (value == null) return IDLE;
            try {
                return SourceStatus.valueOf(value.trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                return IDLE;
            }
        }
    }

    private final String sourceId;
    private final String deviceName;
    private final SourceType sourceType;
    private final SourceStatus status;
    private final String deviceModel;
    private final Instant lastSeen;

    public VolunteerSource(String sourceId, String deviceName, SourceType sourceType,
                           SourceStatus status, String deviceModel, Instant lastSeen) {
        this.sourceId = sourceId;
        this.deviceName = deviceName != null ? deviceName : (sourceId != null ? sourceId : "Unknown Source");
        this.sourceType = sourceType != null ? sourceType : SourceType.SENSOR;
        this.status = status != null ? status : SourceStatus.IDLE;
        this.deviceModel = deviceModel != null ? deviceModel : "";
        this.lastSeen = lastSeen;
    }

    public String getSourceId() { return sourceId; }
    public String getDeviceName() { return deviceName; }
    public SourceType getSourceType() { return sourceType; }
    public SourceStatus getStatus() { return status; }
    public String getDeviceModel() { return deviceModel; }
    public Instant getLastSeen() { return lastSeen; }
    public boolean isActive() { return status == SourceStatus.ACTIVE; }
}
