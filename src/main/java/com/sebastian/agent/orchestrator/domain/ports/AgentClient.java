package com.sebastian.agent.orchestrator.domain.ports;

import com.sebastian.agent.orchestrator.domain.model.AgentResponse;
import com.sebastian.agent.orchestrator.domain.model.AgentType;

public interface AgentClient {

    AgentResponse sendMessage(AgentType agentType, String sessionId, String message);
}
