package com.awarespan.guardian.domain.usecase;

import com.awarespan.guardian.domain.model.AutopilotPolicy;
import com.awarespan.guardian.domain.repository.AutopilotRepository;

import javax.inject.Inject;

public class GetAutopilotPolicyUseCase {
    private final AutopilotRepository repository;

    @Inject
    public GetAutopilotPolicyUseCase(AutopilotRepository repository) {
        this.repository = repository;
    }

    public AutopilotPolicy execute(String nodeId) {
        return repository.getPolicy(nodeId);
    }
}
