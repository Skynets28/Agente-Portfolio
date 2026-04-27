package com.sebastian.agent.orchestrator.domain.model;

public record OrchestratorResult(
        String sessionId,
        String reply,
        ChatIntent intent,
        AgentType agentUsed,
        String rateLimitStatus
) {
}
