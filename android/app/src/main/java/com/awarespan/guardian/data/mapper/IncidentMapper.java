package com.awarespan.guardian.data.mapper;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.awarespan.guardian.data.local.entity.IncidentEntity;
import com.awarespan.guardian.data.remote.dto.IncidentDto;
import com.awarespan.guardian.domain.model.DetectionResult;
import com.awarespan.guardian.domain.model.EvidenceBundle;
import com.awarespan.guardian.domain.model.HumanAnnotation;
import com.awarespan.guardian.domain.model.HumanLabel;
import com.awarespan.guardian.domain.model.Incident;
import com.awarespan.guardian.domain.model.IncidentState;
import com.awarespan.guardian.domain.model.Location;
import com.awarespan.guardian.domain.model.ResponseEvent;
import com.awarespan.guardian.domain.model.DeterrenceType;
import com.awarespan.guardian.domain.model.Threat;
import com.awarespan.guardian.domain.model.ThreatSeverity;
import com.awarespan.guardian.domain.model.ThreatType;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.inject.Inject;

/** DTO ↔ Entity ↔ Domain mapping. Supports legacy nested and current backend DTOs. */
public class IncidentMapper {
    private final Gson gson = new Gson();

    @Inject
    public IncidentMapper() {}

    public Incident toDomain(IncidentDto dto) {
        Threat threat = new Threat(
                safeEnum(ThreatType.class, dto.threat != null ? dto.threat.type : dto.threatType,
                        ThreatType.UNKNOWN),
                safeEnum(ThreatSeverity.class,
                        dto.threat != null ? dto.threat.severity : dto.threatSeverity,
                        ThreatSeverity.DETERRABLE),
                safe(dto.threat != null ? dto.threat.description : dto.threatDescription));

        Location location = null;
        if (dto.location != null) {
            location = new Location(dto.location.latitude, dto.location.longitude,
                    dto.location.humanReadable, dto.location.accuracy);
        } else if (dto.latitude != null || dto.longitude != null) {
            location = new Location(dto.latitude != null ? dto.latitude : 0.0,
                    dto.longitude != null ? dto.longitude : 0.0,
                    dto.locationReadable, dto.locationAccuracy != null ? dto.locationAccuracy : 0.0);
        }

        IncidentDto.EvidenceDto evDto = dto.evidence;
        if (evDto == null && dto.detail != null && dto.detail.evidence != null && !dto.detail.evidence.isEmpty()) {
            evDto = dto.detail.evidence.get(0);
        }
        EvidenceBundle evidence = evDto != null
                ? new EvidenceBundle(evDto.imageUrl, evDto.videoUrl,
                        evDto.audioUrl, evDto.thumbnailUrl,
                        parseInstant(evDto.captureTimestamp),
                        parseInstant(evDto.retentionExpiry), evDto.pendingUpload)
                : null;

        DetectionResult detection = dto.detection != null
                ? new DetectionResult(dto.detection.predictedClass, dto.detection.confidence,
                        dto.detection.modelVersion, parseInstant(dto.detection.detectionTimestamp),
                        dto.detection.sensorModalities != null
                                ? dto.detection.sensorModalities : Collections.emptyList(),
                        Collections.emptyMap())
                : null;

        List<IncidentDto.AnnotationDto> annotationDtos = dto.annotations != null ? dto.annotations
                : dto.detail != null ? dto.detail.annotations : null;
        List<HumanAnnotation> annotations = new ArrayList<>();
        if (annotationDtos != null) {
            for (IncidentDto.AnnotationDto a : annotationDtos) {
                annotations.add(new HumanAnnotation(a.id,
                        safeEnum(HumanLabel.class, a.label, HumanLabel.UNCERTAIN),
                        a.annotatorId, parseInstant(a.timestamp), a.notes, a.confidence, a.version));
            }
        }

        List<IncidentDto.ResponseEventDto> responseDtos = dto.responseEvents != null ? dto.responseEvents
                : dto.detail != null ? dto.detail.responseEvents : null;
        List<ResponseEvent> responses = new ArrayList<>();
        if (responseDtos != null) {
            for (IncidentDto.ResponseEventDto r : responseDtos) {
                responses.add(new ResponseEvent(r.id,
                        safeEnum(DeterrenceType.class, r.actionType, DeterrenceType.COMBINED),
                        parseInstant(r.timestamp), r.result, r.autonomous));
            }
        }

        return new Incident(dto.id, threat, location, evidence, detection, annotations, responses,
                safeEnum(IncidentState.class, dto.state, IncidentState.DETECTED),
                dto.nodeId, parseInstant(dto.createdAt), dto.autopilotHandled, parseInstant(dto.updatedAt));
    }

    public IncidentEntity toEntity(IncidentDto dto) {
        IncidentEntity e = new IncidentEntity();
        e.id = dto.id;
        e.state = dto.state;
        e.threatType = dto.threat != null ? dto.threat.type : dto.threatType;
        e.threatSeverity = dto.threat != null ? dto.threat.severity : dto.threatSeverity;
        e.threatDescription = dto.threat != null ? dto.threat.description : dto.threatDescription;
        e.latitude = dto.location != null ? dto.location.latitude : dto.latitude != null ? dto.latitude : 0.0;
        e.longitude = dto.location != null ? dto.location.longitude : dto.longitude != null ? dto.longitude : 0.0;
        e.locationReadable = dto.location != null ? dto.location.humanReadable : dto.locationReadable;
        e.nodeId = dto.nodeId;
        e.autopilotHandled = dto.autopilotHandled;
        e.createdAtEpochMs = toEpochMs(dto.createdAt);
        e.updatedAtEpochMs = toEpochMs(dto.updatedAt);
        IncidentDto.EvidenceDto entityEv = dto.evidence;
        if (entityEv == null && dto.detail != null && dto.detail.evidence != null && !dto.detail.evidence.isEmpty()) {
            entityEv = dto.detail.evidence.get(0);
        }
        e.evidenceJson = entityEv != null ? gson.toJson(entityEv) : null;
        e.detectionJson = dto.detection != null ? gson.toJson(dto.detection) : null;
        List<IncidentDto.AnnotationDto> annotationDtos = dto.annotations != null ? dto.annotations
                : dto.detail != null ? dto.detail.annotations : null;
        e.annotationsJson = annotationDtos != null ? gson.toJson(annotationDtos) : null;
        List<IncidentDto.ResponseEventDto> responseDtos = dto.responseEvents != null ? dto.responseEvents
                : dto.detail != null ? dto.detail.responseEvents : null;
        e.responseEventsJson = responseDtos != null ? gson.toJson(responseDtos) : null;
        return e;
    }

    public List<IncidentEntity> toEntities(List<IncidentDto> dtos) {
        List<IncidentEntity> out = new ArrayList<>(dtos.size());
        for (IncidentDto d : dtos) out.add(toEntity(d));
        return out;
    }

    public IncidentDto toDto(IncidentEntity e) {
        IncidentDto dto = new IncidentDto();
        dto.id = e.id;
        dto.state = e.state;
        dto.threatType = e.threatType;
        dto.threatSeverity = e.threatSeverity;
        dto.threatDescription = e.threatDescription;
        dto.latitude = e.latitude;
        dto.longitude = e.longitude;
        dto.locationReadable = e.locationReadable;
        dto.nodeId = e.nodeId;
        dto.autopilotHandled = e.autopilotHandled;
        dto.createdAt = Instant.ofEpochMilli(e.createdAtEpochMs).toString();
        dto.updatedAt = Instant.ofEpochMilli(e.updatedAtEpochMs).toString();
        dto.evidence = e.evidenceJson != null
                ? gson.fromJson(e.evidenceJson, IncidentDto.EvidenceDto.class) : null;
        dto.detection = e.detectionJson != null
                ? gson.fromJson(e.detectionJson, IncidentDto.DetectionDto.class) : null;
        dto.annotations = e.annotationsJson != null
                ? gson.fromJson(e.annotationsJson,
                        new TypeToken<List<IncidentDto.AnnotationDto>>() {}.getType()) : null;
        dto.responseEvents = e.responseEventsJson != null
                ? gson.fromJson(e.responseEventsJson,
                        new TypeToken<List<IncidentDto.ResponseEventDto>>() {}.getType()) : null;
        return dto;
    }

    private static String safe(String value) { return value == null ? "" : value; }

    static Instant parseInstant(String iso) {
        if (iso == null || iso.isEmpty()) return null;
        try { return Instant.parse(iso); }
        catch (Exception ex) {
            try { return java.time.OffsetDateTime.parse(iso).toInstant(); }
            catch (Exception ignored) { return null; }
        }
    }

    static long toEpochMs(String iso) {
        Instant i = parseInstant(iso);
        return i == null ? 0 : i.toEpochMilli();
    }

    static <E extends Enum<E>> E safeEnum(Class<E> type, String name, E fallback) {
        if (name == null) return fallback;
        try { return Enum.valueOf(type, name); }
        catch (IllegalArgumentException ex) { return fallback; }
    }
}
