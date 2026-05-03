package com.sebastian.agent.orchestrator.infrastructure.agents.dto;

public record RemoteAgentRequest(
        String sessionId,
        String message
) {
}
