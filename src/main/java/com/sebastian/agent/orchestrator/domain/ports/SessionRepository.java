package com.sebastian.agent.orchestrator.domain.ports;

import com.sebastian.agent.orchestrator.domain.model.VisitorSession;

import java.util.Optional;

public interface SessionRepository {
    Optional<VisitorSession> findById(String sessionId);

    VisitorSession save(VisitorSession session);
}
