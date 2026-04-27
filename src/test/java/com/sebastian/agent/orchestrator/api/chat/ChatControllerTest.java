package com.sebastian.agent.orchestrator.api.chat;

import com.sebastian.agent.orchestrator.application.chat.OrchestratorUseCase;
import com.sebastian.agent.orchestrator.domain.model.AgentType;
import com.sebastian.agent.orchestrator.domain.model.ChatIntent;
import com.sebastian.agent.orchestrator.domain.model.OrchestratorResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ChatController.class)
class ChatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private StubOrchestratorUseCase orchestratorUseCase;

    @BeforeEach
    void setUp() {
        orchestratorUseCase.reset();
    }

    @Test
    void chatReturnsUseCaseResultAsJson() throws Exception {
        orchestratorUseCase.result = new OrchestratorResult(
                "session-1",
                "profile reply",
                ChatIntent.PROFILE,
                AgentType.PROFILE,
                "ALLOWED"
        );

        mockMvc.perform(post("/api/agent/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "sessionId": "session-1",
                                  "message": "Tell me about Sebastian",
                                  "section": "hero"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sessionId").value("session-1"))
                .andExpect(jsonPath("$.reply").value("profile reply"))
                .andExpect(jsonPath("$.intent").value("PROFILE"))
                .andExpect(jsonPath("$.agentUsed").value("PROFILE"))
                .andExpect(jsonPath("$.rateLimitStatus").value("ALLOWED"));

        assertThat(orchestratorUseCase.wasCalled).isTrue();
        assertThat(orchestratorUseCase.lastSessionId).isEqualTo("session-1");
        assertThat(orchestratorUseCase.lastMessage).isEqualTo("Tell me about Sebastian");
        assertThat(orchestratorUseCase.lastSection).isEqualTo("hero");
    }

    @Test
    void chatAcceptsRequestWithoutSessionId() throws Exception {
        orchestratorUseCase.result = new OrchestratorResult(
                "generated-session",
                "contact reply",
                ChatIntent.CONTACT,
                AgentType.CONTACT,
                "ALLOWED"
        );

        mockMvc.perform(post("/api/agent/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "message": "Quiero dejarle un mensaje",
                                  "section": "contact"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sessionId").value("generated-session"))
                .andExpect(jsonPath("$.intent").value("CONTACT"))
                .andExpect(jsonPath("$.agentUsed").value("CONTACT"));

        assertThat(orchestratorUseCase.wasCalled).isTrue();
        assertThat(orchestratorUseCase.lastSessionId).isNull();
        assertThat(orchestratorUseCase.lastMessage).isEqualTo("Quiero dejarle un mensaje");
        assertThat(orchestratorUseCase.lastSection).isEqualTo("contact");
    }

    @Test
    void chatRejectsBlankMessage() throws Exception {
        mockMvc.perform(post("/api/agent/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "sessionId": "session-1",
                                  "message": "   ",
                                  "section": "hero"
                                }
                """))
                .andExpect(status().isBadRequest());

        assertThat(orchestratorUseCase.wasCalled).isFalse();
    }

    @Test
    void chatRejectsMessageLongerThanLimit() throws Exception {
        String longMessage = "a".repeat(2_001);

        mockMvc.perform(post("/api/agent/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "sessionId": "session-1",
                                  "message": "%s",
                                  "section": "hero"
                                }
                """.formatted(longMessage)))
                .andExpect(status().isBadRequest());

        assertThat(orchestratorUseCase.wasCalled).isFalse();
    }

    @Test
    void chatRejectsSectionLongerThanLimit() throws Exception {
        String longSection = "a".repeat(81);

        mockMvc.perform(post("/api/agent/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "sessionId": "session-1",
                                  "message": "Tell me about Sebastian",
                                  "section": "%s"
                                }
                """.formatted(longSection)))
                .andExpect(status().isBadRequest());

        assertThat(orchestratorUseCase.wasCalled).isFalse();
    }

    @TestConfiguration
    static class ControllerTestConfiguration {

        @Bean
        StubOrchestratorUseCase stubOrchestratorUseCase() {
            return new StubOrchestratorUseCase();
        }
    }

    static class StubOrchestratorUseCase extends OrchestratorUseCase {
        private OrchestratorResult result;
        private boolean wasCalled;
        private String lastSessionId;
        private String lastMessage;
        private String lastSection;

        StubOrchestratorUseCase() {
            super(null, null, null, null);
        }

        @Override
        public OrchestratorResult handle(String sessionId, String message, String section) {
            wasCalled = true;
            lastSessionId = sessionId;
            lastMessage = message;
            lastSection = section;
            return result;
        }

        private void reset() {
            result = null;
            wasCalled = false;
            lastSessionId = null;
            lastMessage = null;
            lastSection = null;
        }
    }
}
