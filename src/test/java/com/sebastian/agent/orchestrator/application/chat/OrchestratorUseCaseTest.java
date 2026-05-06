package com.sebastian.agent.orchestrator.application.chat;

import com.sebastian.agent.orchestrator.domain.model.*;
import com.sebastian.agent.orchestrator.domain.ports.AgentClient;
import com.sebastian.agent.orchestrator.domain.ports.IntentClassifier;
import com.sebastian.agent.orchestrator.domain.ports.RateLimitPolicy;
import com.sebastian.agent.orchestrator.domain.ports.SessionRepository;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static org.assertj.core.api.Assertions.assertThat;

class OrchestratorUseCaseTest {

    @Test
    void delegatesProfileIntentToProfileAgent() {
        InMemorySessionRepository sessionRepository = new InMemorySessionRepository();
        RecordingAgentClient agentClient = new RecordingAgentClient(
                new AgentResponse(
                        "profile reply",
                        AgentType.PROFILE,
                        AgentResponseStatus.OK));
        OrchestratorUseCase useCase = newUseCase(
                sessionRepository,
                message -> ChatIntent.PROFILE,
                context -> true,
                agentClient
        );

        OrchestratorResult result = useCase.handle("session-1", "Tell me about Sebastian", "hero", "127.0.0.1");

        assertThat(result.sessionId()).isEqualTo("session-1");
        assertThat(result.reply()).isEqualTo("profile reply");
        assertThat(result.intent()).isEqualTo(ChatIntent.PROFILE);
        assertThat(result.agentUsed()).isEqualTo(AgentType.PROFILE);
        assertThat(result.rateLimitStatus()).isEqualTo(RateLimitStatus.ALLOWED);
        assertThat(result.responseType()).isEqualTo(ResponseType.AGENT_REPLY);
        assertThat(agentClient.lastAgentType).isEqualTo(AgentType.PROFILE);
        assertThat(agentClient.lastSessionId).isEqualTo("session-1");
        assertThat(agentClient.lastMessage).isEqualTo("Tell me about Sebastian");
        assertThat(sessionRepository.findById("session-1"))
                .get()
                .extracting(VisitorSession::messageCount, VisitorSession::currentAgent)
                .containsExactly(1, AgentType.PROFILE);
    }

    @Test
    void returnsClarificationForUnclearIntentWithoutDelegating() {
        InMemorySessionRepository sessionRepository = new InMemorySessionRepository();
        RecordingAgentClient agentClient = new RecordingAgentClient(new AgentResponse(
                "should not be used",
                AgentType.ORCHESTRATOR,
                AgentResponseStatus.OK));
        OrchestratorUseCase useCase = newUseCase(
                sessionRepository,
                message -> ChatIntent.UNCLEAR,
                context -> true,
                agentClient
        );

        OrchestratorResult result = useCase.handle("session-2", "hola", "hero", "127.0.0.1");

        assertThat(result.sessionId()).isEqualTo("session-2");
        assertThat(result.intent()).isEqualTo(ChatIntent.UNCLEAR);
        assertThat(result.agentUsed()).isEqualTo(AgentType.ORCHESTRATOR);
        assertThat(result.rateLimitStatus()).isEqualTo(RateLimitStatus.ALLOWED);
        assertThat(result.responseType()).isEqualTo(ResponseType.CLARIFICATION);
        assertThat(result.reply()).contains("Puedo ayudarte");
        assertThat(agentClient.wasCalled).isFalse();
        assertThat(sessionRepository.findById("session-2"))
                .get()
                .extracting(VisitorSession::messageCount, VisitorSession::currentAgent)
                .containsExactly(1, AgentType.ORCHESTRATOR);
    }

    @Test
    void returnsGuardianWhenRateLimitBlocksProfileIntentWithoutDelegating() {
        InMemorySessionRepository sessionRepository = new InMemorySessionRepository();
        RecordingAgentClient agentClient = new RecordingAgentClient(new AgentResponse(
                "should not be used",
                AgentType.ORCHESTRATOR,
                AgentResponseStatus.OK));
        OrchestratorUseCase useCase = newUseCase(
                sessionRepository,
                message -> ChatIntent.PROFILE,
                context -> false,
                agentClient
        );

        OrchestratorResult result = useCase.handle("session-3", "Tell me about Java", "projects", "127.0.0.1");

        assertThat(result.sessionId()).isEqualTo("session-3");
        assertThat(result.intent()).isEqualTo(ChatIntent.PROFILE);
        assertThat(result.agentUsed()).isEqualTo(AgentType.ORCHESTRATOR);
        assertThat(result.rateLimitStatus()).isEqualTo(RateLimitStatus.LIMITED);
        assertThat(result.responseType()).isEqualTo(ResponseType.GUARDIAN);
        assertThat(result.reply()).contains("Has explorado bastante");
        assertThat(agentClient.wasCalled).isFalse();
        assertThat(sessionRepository.findById("session-3"))
                .get()
                .extracting(VisitorSession::messageCount, VisitorSession::currentAgent)
                .containsExactly(1, AgentType.ORCHESTRATOR);
    }

    @Test
    void delegatesContactIntentAndMarksContactCaptureStarted() {
        InMemorySessionRepository sessionRepository = new InMemorySessionRepository();
        RecordingAgentClient agentClient = new RecordingAgentClient(new AgentResponse(
                "contact reply",
                AgentType.CONTACT,
                AgentResponseStatus.OK));
        OrchestratorUseCase useCase = newUseCase(
                sessionRepository,
                message -> ChatIntent.CONTACT,
                context -> true,
                agentClient
        );

        OrchestratorResult result = useCase.handle("session-4", "I want to contact Sebastian", "contact", "127.0.0.1");

        assertThat(result.sessionId()).isEqualTo("session-4");
        assertThat(result.reply()).isEqualTo("contact reply");
        assertThat(result.intent()).isEqualTo(ChatIntent.CONTACT);
        assertThat(result.agentUsed()).isEqualTo(AgentType.CONTACT);
        assertThat(result.rateLimitStatus()).isEqualTo(RateLimitStatus.ALLOWED);
        assertThat(result.responseType()).isEqualTo(ResponseType.AGENT_REPLY);
        assertThat(agentClient.lastAgentType).isEqualTo(AgentType.CONTACT);
        assertThat(agentClient.lastSessionId).isEqualTo("session-4");
        assertThat(agentClient.lastMessage).isEqualTo("I want to contact Sebastian");
        assertThat(sessionRepository.findById("session-4"))
                .get()
                .extracting(
                        VisitorSession::messageCount,
                        VisitorSession::currentAgent,
                        VisitorSession::contactCaptureStarted
                )
                .containsExactly(1, AgentType.CONTACT, true);
    }

    @Test
    void delegatesContactIntentEvenWhenRateLimitPolicyBlocks() {
        InMemorySessionRepository sessionRepository = new InMemorySessionRepository();
        RecordingAgentClient agentClient = new RecordingAgentClient(new AgentResponse(
                "contact reply",
                AgentType.CONTACT,
                AgentResponseStatus.OK));
        OrchestratorUseCase useCase = newUseCase(
                sessionRepository,
                message -> ChatIntent.CONTACT,
                context -> false,
                agentClient
        );

        OrchestratorResult result = useCase.handle("session-contact-limit", "I want to contact Sebastian", "contact", "127.0.0.1");

        assertThat(result.sessionId()).isEqualTo("session-contact-limit");
        assertThat(result.reply()).isEqualTo("contact reply");
        assertThat(result.intent()).isEqualTo(ChatIntent.CONTACT);
        assertThat(result.agentUsed()).isEqualTo(AgentType.CONTACT);
        assertThat(result.rateLimitStatus()).isEqualTo(RateLimitStatus.ALLOWED);
        assertThat(result.responseType()).isEqualTo(ResponseType.AGENT_REPLY);
        assertThat(agentClient.lastAgentType).isEqualTo(AgentType.CONTACT);
        assertThat(sessionRepository.findById("session-contact-limit"))
                .get()
                .extracting(
                        VisitorSession::messageCount,
                        VisitorSession::currentAgent,
                        VisitorSession::contactCaptureStarted
                )
                .containsExactly(1, AgentType.CONTACT, true);
    }

    @Test
    void createsSessionIdWhenRequestDoesNotProvideOne() {
        InMemorySessionRepository sessionRepository = new InMemorySessionRepository();
        OrchestratorUseCase useCase = newUseCase(
                sessionRepository,
                message -> ChatIntent.PROFILE,
                context -> true,
                new RecordingAgentClient(new AgentResponse(
                                "profile reply",
                                AgentType.PROFILE,
                                AgentResponseStatus.OK))
        );

        OrchestratorResult result = useCase.handle(" ", "Tell me about Sebastian", "hero", "127.0.0.1");

        assertThat(result.sessionId()).isNotBlank();
        assertThat(result.sessionId()).isNotEqualTo(" ");
        assertThat(sessionRepository.findById(result.sessionId())).isPresent();
    }

    @Test
    void incrementsExistingSessionMessageCount() {
        InMemorySessionRepository sessionRepository = new InMemorySessionRepository();
        Instant now = Instant.now();
        sessionRepository.save(new VisitorSession(
                "session-5",
                AgentType.PROFILE,
                7,
                false,
                now,
                now
        ));
        OrchestratorUseCase useCase = newUseCase(
                sessionRepository,
                message -> ChatIntent.PROFILE,
                context -> true,
                 new RecordingAgentClient(new AgentResponse(
                                "profile reply",
                                AgentType.PROFILE,
                                AgentResponseStatus.OK))
        );

        useCase.handle("session-5", "Tell me about projects", "projects", "127.0.0.1");

        assertThat(sessionRepository.findById("session-5"))
                .get()
                .extracting(VisitorSession::messageCount, VisitorSession::currentAgent)
                .containsExactly(8, AgentType.PROFILE);
    }

    @Test
    void returnsErrorWhenDelegatedAgentFails() {
        InMemorySessionRepository sessionRepository = new InMemorySessionRepository();
        RecordingAgentClient agentClient = new RecordingAgentClient(new AgentResponse(
                "internal agent error",
                AgentType.PROFILE,
                AgentResponseStatus.ERROR
        ));
        OrchestratorUseCase useCase = newUseCase(
                sessionRepository,
                message -> ChatIntent.PROFILE,
                context -> true,
                agentClient
        );

        OrchestratorResult result = useCase.handle(
                "session-error",
                "Tell me about Sebastian",
                "hero",
                "127.0.0.1"
        );

        assertThat(result.sessionId()).isEqualTo("session-error");
        assertThat(result.agentUsed()).isEqualTo(AgentType.ORCHESTRATOR);
        assertThat(result.rateLimitStatus()).isEqualTo(RateLimitStatus.ALLOWED);
        assertThat(result.responseType()).isEqualTo(ResponseType.ERROR);
        assertThat(result.reply()).contains("No pude contactar");
    }

    private OrchestratorUseCase newUseCase(
            SessionRepository sessionRepository,
            IntentClassifier intentClassifier,
            RateLimitPolicy rateLimitPolicy,
            AgentClient agentClient
    ) {
        return new OrchestratorUseCase(
                sessionRepository,
                intentClassifier,
                rateLimitPolicy,
                agentClient
        );
    }

    private static class InMemorySessionRepository implements SessionRepository {
        private final ConcurrentMap<String, VisitorSession> sessions = new ConcurrentHashMap<>();

        @Override
        public Optional<VisitorSession> findById(String sessionId) {
            return Optional.ofNullable(sessions.get(sessionId));
        }

        @Override
        public VisitorSession save(VisitorSession session) {
            sessions.put(session.sessionId(), session);
            return session;
        }
    }

    private static class RecordingAgentClient implements AgentClient {
        private final AgentResponse agentResponse;
        private boolean wasCalled;
        private AgentType lastAgentType;
        private String lastSessionId;
        private String lastMessage;

        private RecordingAgentClient(AgentResponse agentResponse) {
            this.agentResponse = agentResponse;
        }

        @Override
        public AgentResponse sendMessage(AgentType agentType, String sessionId, String message) {
            wasCalled = true;
            lastAgentType = agentType;
            lastSessionId = sessionId;
            lastMessage = message;
            return agentResponse;
        }
    }
}
