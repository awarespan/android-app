package com.awarespan.guardian.data.repository;

import com.awarespan.guardian.data.local.dao.IncidentDao;
import com.awarespan.guardian.data.mapper.IncidentMapper;
import com.awarespan.guardian.data.remote.api.SipApiService;
import com.awarespan.guardian.data.remote.dto.IncidentDto;
import com.awarespan.guardian.data.remote.dto.PageDto;
import com.awarespan.guardian.data.remote.dto.VerificationRequest;
import com.awarespan.guardian.domain.model.HumanAnnotation;
import com.awarespan.guardian.domain.model.HumanLabel;
import com.awarespan.guardian.domain.model.Incident;
import com.awarespan.guardian.domain.model.IncidentState;
import com.awarespan.guardian.domain.model.ThreatType;
import com.awarespan.guardian.domain.repository.IncidentRepository;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.rxjava3.core.Flowable;
import retrofit2.Response;

@Singleton
public class IncidentRepositoryImpl implements IncidentRepository {

    private final SipApiService api;
    private final IncidentDao dao;
    private final IncidentMapper mapper;
    private final com.awarespan.guardian.data.logging.DiagnosticLogger logger;

    @Inject
    public IncidentRepositoryImpl(SipApiService api, IncidentDao dao, IncidentMapper mapper,
                                  com.awarespan.guardian.data.logging.DiagnosticLogger logger) {
        this.api = api;
        this.dao = dao;
        this.mapper = mapper;
        this.logger = logger;
    }

    @Override
    public List<Incident> getIncidents(Integer page, Integer size, IncidentState state,
                                       ThreatType threatType, String nodeId,
                                       Instant from, Instant to) {
        try {
            Response<PageDto<IncidentDto>> response = api.getIncidents(
                    page, size,
                    state != null ? state.name() : null,
                    threatType != null ? threatType.name() : null,
                    nodeId,
                    from != null ? from.toString() : null,
                    to != null ? to.toString() : null).execute();
            if (response.isSuccessful() && response.body() != null) {
                List<IncidentDto> dtos = response.body().content;
                dao.upsertAll(mapper.toEntities(dtos));
                List<Incident> out = new ArrayList<>(dtos.size());
                for (IncidentDto d : dtos) out.add(mapper.toDomain(d));
                logger.logSyncEvent("IncidentSync", out.size(), 0);
                return out;
            } else {
                logger.warn("Sync", "Incident query returned status " + response.code() + "; falling back to cache");
            }
        } catch (IOException e) {
            logger.warn("Sync", "Incident query network failure; falling back to cache", e);
        }

        int fallbackPage = page != null ? page : 0;
        int fallbackSize = size != null ? size : 50;
        if (fallbackPage < 0 || fallbackSize < 1 || fallbackSize > 100) {
            throw new IllegalArgumentException("Invalid incident page or size");
        }
        long offsetLong = (long) fallbackPage * fallbackSize;
        if (offsetLong > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Incident page offset is too large");
        }

        List<Incident> cached = new ArrayList<>();
        for (var e : dao.getCachedFiltered(
                state != null ? state.name() : null,
                threatType != null ? threatType.name() : null,
                nodeId,
                from != null ? from.toEpochMilli() : null,
                to != null ? to.toEpochMilli() : null,
                fallbackSize,
                (int) offsetLong)) {
            cached.add(mapper.toDomain(mapper.toDto(e)));
        }
        return cached;
    }

    @Override
    public Incident getIncidentById(String id) {
        try {
            Response<IncidentDto> response = api.getIncident(id).execute();
            if (response.isSuccessful() && response.body() != null) {
                IncidentDto dto = response.body();
                dao.upsert(mapper.toEntity(dto));
                return mapper.toDomain(dto);
            } else {
                logger.warn("Sync", "Incident detail request for " + id + " returned status " + response.code());
            }
        } catch (IOException e) {
            logger.warn("Sync", "Incident detail request network failure", e);
        }
        var entity = dao.getById(id);
        return entity == null ? null : mapper.toDomain(mapper.toDto(entity));
    }

    @Override
    public Incident verifyIncident(String incidentId, HumanAnnotation annotation) {
        String verdict = annotation.getLabel() == HumanLabel.TRUE_POSITIVE
                ? "VERIFIED" : "REJECTED";
        VerificationRequest request = new VerificationRequest(
                verdict, annotation.getLabel().name(), annotation.getNotes());
        try {
            Response<IncidentDto> response =
                    api.verifyIncident(incidentId, request).execute();
            if (response.isSuccessful() && response.body() != null) {
                IncidentDto dto = response.body();
                dao.upsert(mapper.toEntity(dto));
                logger.info("Verification", "Incident " + incidentId + " verified with verdict: " + verdict);
                return mapper.toDomain(dto);
            }
            logger.warn("Verification", "Verification failed for " + incidentId + ": HTTP " + response.code());
            throw new IllegalStateException("Verification could not be completed. Please try again.");
        } catch (IOException e) {
            logger.warn("Verification", "Verification network error for " + incidentId, e);
            throw new IllegalStateException("Verification network error. Please verify connection and retry.", e);
        }
    }

    @Override
    public Flowable<List<Incident>> observeIncidents() {
        return dao.observeAll()
                .map(entities -> {
                    List<Incident> out = new ArrayList<>(entities.size());
                    for (var e : entities) out.add(mapper.toDomain(mapper.toDto(e)));
                    return out;
                });
    }

    @Override
    public Flowable<Incident> observeIncidentById(String id) {
        return dao.observeById(id)
                .map(e -> mapper.toDomain(mapper.toDto(e)));
    }
}
