package com.sebastian.agent.orchestrator.api.chat.dto;

public record ChatResponse(
        String sessionId,
        String reply,
        String intent,
        String agentUsed,
        String rateLimitStatus
) {
}
