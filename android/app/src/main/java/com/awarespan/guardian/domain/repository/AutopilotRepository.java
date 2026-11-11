package com.awarespan.guardian.domain.repository;

import com.awarespan.guardian.domain.model.AutopilotPolicy;

public interface AutopilotRepository {
    AutopilotPolicy getPolicy(String nodeId);
    /** PUT policy -> gateway -> MQTT -> Pi -> ACK (plan §18 sync flow). */
    AutopilotPolicy updatePolicy(String nodeId, AutopilotPolicy policy);
}
