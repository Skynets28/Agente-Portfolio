package com.sebastian.agent.orchestrator.domain.model;

import java.time.Instant;

public record VisitorSession(
        String sessionId,
        AgentType currentAgent,
        int messageCount,
        boolean contactCaptureStarted,
        Instant createdAt,
        Instant updatedAt
) {
}
