package com.sebastian.agent.orchestrator.infrastructure.agents;

import com.sebastian.agent.orchestrator.domain.model.AgentResponse;
import com.sebastian.agent.orchestrator.domain.model.AgentResponseStatus;
import com.sebastian.agent.orchestrator.domain.model.AgentType;
import com.sebastian.agent.orchestrator.infrastructure.config.OrchestratorProperties;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class RemoteAgentClientTest {

    @Test
    void returnsOkResponseWhenRemoteProfileAgentReplies() {
        RestClient.Builder restClientBuilder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(restClientBuilder).build();
        RemoteAgentClient client = new RemoteAgentClient(restClientBuilder, properties());

        server.expect(requestTo("http://profile-agent.test/api/agent/message"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().json("""
                        {
                          "sessionId": "session-1",
                          "message": "Tell me about Sebastian"
                        }
                        """))
                .andRespond(withSuccess("""
                        {
                          "reply": "Profile remote reply"
                        }
                        """, MediaType.APPLICATION_JSON));

        AgentResponse response = client.sendMessage(
                AgentType.PROFILE,
                "session-1",
                "Tell me about Sebastian"
        );

        assertThat(response.reply()).isEqualTo("Profile remote reply");
        assertThat(response.agentType()).isEqualTo(AgentType.PROFILE);
        assertThat(response.status()).isEqualTo(AgentResponseStatus.OK);
        server.verify();
    }

    @Test
    void returnsErrorResponseWhenRemoteAgentFails() {
        RestClient.Builder restClientBuilder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(restClientBuilder).build();
        RemoteAgentClient client = new RemoteAgentClient(restClientBuilder, properties());

        server.expect(requestTo("http://contact-agent.test/api/agent/message"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withServerError());

        AgentResponse response = client.sendMessage(
                AgentType.CONTACT,
                "session-1",
                "I want to contact Sebastian"
        );

        assertThat(response.reply()).isEqualTo("Agent unavailable");
        assertThat(response.agentType()).isEqualTo(AgentType.CONTACT);
        assertThat(response.status()).isEqualTo(AgentResponseStatus.ERROR);
        server.verify();
    }

    private OrchestratorProperties properties() {
        return new OrchestratorProperties(
                new OrchestratorProperties.Session(Duration.ofHours(2), "session:"),
                new OrchestratorProperties.RateLimit(40, "rl:", Duration.ofHours(1)),
                new OrchestratorProperties.Agents(
                        "http://profile-agent.test",
                        "http://contact-agent.test",
                        Duration.ofMillis(500),
                        Duration.ofSeconds(2)
                )
        );
    }
}
