package com.sebastian.agent.orchestrator.domain.model;

public record AgentResponse(
        String reply,
        AgentType agentType,
        AgentResponseStatus status
) {
}
