package com.sebastian.agent.orchestrator.infrastructure.ratelimit;

import com.sebastian.agent.orchestrator.domain.model.ChatIntent;
import com.sebastian.agent.orchestrator.domain.model.VisitorSession;
import com.sebastian.agent.orchestrator.domain.ports.RateLimitPolicy;
import com.sebastian.agent.orchestrator.infrastructure.config.OrchestratorProperties;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile({"simple", "redis"})
public class SimpleRateLimitPolicy implements RateLimitPolicy {
    private final OrchestratorProperties orchestratorProperties;

    public SimpleRateLimitPolicy(OrchestratorProperties orchestratorProperties) {
        this.orchestratorProperties = orchestratorProperties;
    }

    @Override
    public boolean isAllowed(VisitorSession session, ChatIntent intent) {
        if (intent == ChatIntent.CONTACT) {
            return true;
        }
        return session.messageCount() < orchestratorProperties.rateLimit().maxMessagesPerSession();
    }
}
