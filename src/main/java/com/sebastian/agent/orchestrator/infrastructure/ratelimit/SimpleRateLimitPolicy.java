package com.sebastian.agent.orchestrator.infrastructure.ratelimit;

import com.sebastian.agent.orchestrator.domain.model.ChatIntent;
import com.sebastian.agent.orchestrator.domain.model.RateLimitContext;
import com.sebastian.agent.orchestrator.domain.ports.RateLimitPolicy;
import com.sebastian.agent.orchestrator.infrastructure.config.OrchestratorProperties;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile({"simple"})
public class SimpleRateLimitPolicy implements RateLimitPolicy {
    private final OrchestratorProperties orchestratorProperties;

    public SimpleRateLimitPolicy(OrchestratorProperties orchestratorProperties) {
        this.orchestratorProperties = orchestratorProperties;
    }

    @Override
    public boolean isAllowed(RateLimitContext rateLimitContext) {
        if (rateLimitContext.intent() == ChatIntent.CONTACT) {
            return true;
        }
        return rateLimitContext.messageCount() <= orchestratorProperties.rateLimit().maxMessagesPerSession();
    }
}
