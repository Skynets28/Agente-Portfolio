package com.sebastian.agent.orchestrator.infrastructure.agents;

import com.sebastian.agent.orchestrator.domain.model.AgentResponse;
import com.sebastian.agent.orchestrator.domain.model.AgentResponseStatus;
import com.sebastian.agent.orchestrator.domain.model.AgentType;
import com.sebastian.agent.orchestrator.domain.ports.AgentClient;
import com.sebastian.agent.orchestrator.infrastructure.agents.dto.RemoteAgentRequest;
import com.sebastian.agent.orchestrator.infrastructure.agents.dto.RemoteAgentResponse;
import com.sebastian.agent.orchestrator.infrastructure.config.OrchestratorProperties;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
@Profile("remote")
public class RemoteAgentClient implements AgentClient {
    private static final String AGENT_MESSAGE_PATH = "/api/agent/message";

    private final RestClient profileAgentClient;
    private final RestClient contactAgentClient;

    public RemoteAgentClient(
            RestClient.Builder restClientBuilder,
            OrchestratorProperties orchestratorProperties
    ) {
        this.profileAgentClient = restClientBuilder.clone()
                .baseUrl(orchestratorProperties.agents().profileBaseUrl())
                .build();
        this.contactAgentClient = restClientBuilder.clone()
                .baseUrl(orchestratorProperties.agents().contactBaseUrl())
                .build();
    }

    @Override
    public AgentResponse sendMessage(AgentType agentType, String sessionId, String message) {
        if (agentType == AgentType.ORCHESTRATOR) {
            return errorResponse(agentType);
        }

        try {
            RemoteAgentResponse response = clientFor(agentType)
                    .post()
                    .uri(AGENT_MESSAGE_PATH)
                    .body(new RemoteAgentRequest(sessionId, message))
                    .retrieve()
                    .body(RemoteAgentResponse.class);

            if (response == null || response.reply() == null || response.reply().isBlank()) {
                return errorResponse(agentType);
            }

            return new AgentResponse(
                    response.reply(),
                    agentType,
                    AgentResponseStatus.OK
            );
        } catch (RestClientException exception) {
            return errorResponse(agentType);
        }
    }

    private RestClient clientFor(AgentType agentType) {
        return switch (agentType) {
            case PROFILE -> profileAgentClient;
            case CONTACT -> contactAgentClient;
            case ORCHESTRATOR -> throw new IllegalArgumentException("Orchestrator is not a remote agent");
        };
    }

    private AgentResponse errorResponse(AgentType agentType) {
        return new AgentResponse(
                "Agent unavailable",
                agentType,
                AgentResponseStatus.ERROR
        );
    }
}
