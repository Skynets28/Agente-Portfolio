package com.sebastian.agent.orchestrator.infrastructure.session;

import com.sebastian.agent.orchestrator.domain.model.VisitorSession;
import com.sebastian.agent.orchestrator.domain.ports.SessionRepository;
import com.sebastian.agent.orchestrator.infrastructure.config.OrchestratorProperties;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@Profile("redis")
public class RedisSessionRepository implements SessionRepository {
    private final RedisTemplate<String, VisitorSession> redisTemplate;
    private final OrchestratorProperties orchestratorProperties;

    public RedisSessionRepository(
            RedisTemplate<String, VisitorSession> redisTemplate,
            OrchestratorProperties orchestratorProperties
    ) {
        this.redisTemplate = redisTemplate;
        this.orchestratorProperties = orchestratorProperties;
    }

    @Override
    public Optional<VisitorSession> findById(String sessionId){
        String key = buildSessionKey(sessionId);

        VisitorSession session = redisTemplate.opsForValue().get(key);
        return Optional.ofNullable(session);
    }

    public VisitorSession save(VisitorSession session) {
        String key = buildSessionKey(session.sessionId());
        redisTemplate.opsForValue().set(
                key,
                session,
                orchestratorProperties.session().ttl()
        );

        return session;
    }

    private String buildSessionKey(String sessionId){
        return orchestratorProperties.session().keyPrefix() + sessionId;
    }
}
