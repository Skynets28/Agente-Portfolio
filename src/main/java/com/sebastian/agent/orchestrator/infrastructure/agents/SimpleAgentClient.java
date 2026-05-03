package com.sebastian.agent.orchestrator.infrastructure.agents;

import com.sebastian.agent.orchestrator.domain.model.AgentResponse;
import com.sebastian.agent.orchestrator.domain.model.AgentResponseStatus;
import com.sebastian.agent.orchestrator.domain.model.AgentType;
import com.sebastian.agent.orchestrator.domain.ports.AgentClient;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("simple")
public class SimpleAgentClient implements AgentClient {
    @Override
    public AgentResponse sendMessage(AgentType agentType, String sessionId, String message) {
        return switch (agentType) {
            case PROFILE -> new AgentResponse(
                    "Respuesta temporal del Profile Agent.",
                    AgentType.PROFILE,
                    AgentResponseStatus.OK
            );
            case CONTACT -> new AgentResponse(
                    "Respuesta temporal del Contact Agent. Puedo ayudarte a dejarle un mensaje a Sebastian.",
                    AgentType.CONTACT,
                    AgentResponseStatus.OK
            );
            case ORCHESTRATOR -> new AgentResponse(
                    "Respuesta temporal del Orchestrator.",
                    AgentType.ORCHESTRATOR,
                    AgentResponseStatus.OK
            );
        };
    }
}
