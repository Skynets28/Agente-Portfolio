package com.sebastian.agent.orchestrator.domain.ports;

import com.sebastian.agent.orchestrator.domain.model.AgentType;

public interface AgentClient {

    String sendMessage(AgentType agentType, String sessionId, String message);
}
