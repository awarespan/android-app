package com.awarespan.guardian.domain.repository;

import com.awarespan.guardian.domain.model.EvidenceBundle;

import java.io.File;

/** Fetches evidence binaries from authenticated gateway endpoints (plan §16). */
public interface EvidenceRepository {
    /** Local cached file for an evidence URL; downloads on miss. */
    File getEvidenceFile(String incidentId, EvidenceBundle bundle, com.awarespan.guardian.domain.model.EvidenceType type);
}
