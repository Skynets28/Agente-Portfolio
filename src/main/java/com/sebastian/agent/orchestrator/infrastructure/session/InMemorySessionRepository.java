package com.sebastian.agent.orchestrator.infrastructure.session;

import com.sebastian.agent.orchestrator.domain.model.VisitorSession;
import com.sebastian.agent.orchestrator.domain.ports.SessionRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Profile("simple")
public class InMemorySessionRepository implements SessionRepository {
    private final Map<String, VisitorSession> sessions = new ConcurrentHashMap<>();

    @Override
    public Optional<VisitorSession> findById(String sessionId) {
        return  Optional.ofNullable(sessions.get(sessionId));
    }

    @Override
    public VisitorSession save(VisitorSession session) {
        sessions.put(session.sessionId(), session);
        return session;
    }
}
