package com.sebastian.agent.orchestrator.infrastructure.ratelimit;

import com.sebastian.agent.orchestrator.domain.model.ChatIntent;
import com.sebastian.agent.orchestrator.domain.model.VisitorSession;
import com.sebastian.agent.orchestrator.domain.ports.RateLimitPolicy;
import org.springframework.stereotype.Component;

@Component
public class SimpleRateLimitPolicy implements RateLimitPolicy {
    private static final int MAX_MESSAGES_PER_SESSION = 40;

    @Override
    public boolean isAllowed(VisitorSession session, ChatIntent intent) {
        if (intent == ChatIntent.CONTACT) {
            return true;
        }
        return session.messageCount() < MAX_MESSAGES_PER_SESSION;
    }
}
