package com.sebastian.agent.orchestrator.domain.model;

public record RateLimitContext(
        String sessionId,
        String clientIp,
        ChatIntent intent,
        int messageCount
) {
}
