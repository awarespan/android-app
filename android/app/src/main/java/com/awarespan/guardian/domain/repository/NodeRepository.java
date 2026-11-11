package com.awarespan.guardian.domain.repository;

import com.awarespan.guardian.domain.model.Node;

import java.util.List;

public interface NodeRepository {
    List<Node> getNodes();
    Node getNodeById(String nodeId);
}
