package com.sebastian.agent.orchestrator.infrastructure.agents;

import com.sebastian.agent.orchestrator.domain.model.AgentType;
import com.sebastian.agent.orchestrator.domain.ports.AgentClient;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile({"simple", "redis"})
public class SimpleAgentClient implements AgentClient {
    @Override
    public String sendMessage(AgentType agentType, String sessionId, String message) {
        return switch (agentType) {
            case PROFILE -> "Respuesta temporal del Profile Agent.";
            case CONTACT -> "Respuesta temporal del Contact Agent. Puedo ayudarte a dejarle un mensaje a Sebastian.";
            case ORCHESTRATOR -> "Respuesta temporal del Orchestrator.";
        };
    }
}
