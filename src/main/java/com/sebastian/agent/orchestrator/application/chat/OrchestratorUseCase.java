package com.sebastian.agent.orchestrator.application.chat;

import com.sebastian.agent.orchestrator.domain.model.*;
import com.sebastian.agent.orchestrator.domain.ports.AgentClient;
import com.sebastian.agent.orchestrator.domain.ports.IntentClassifier;
import com.sebastian.agent.orchestrator.domain.ports.RateLimitPolicy;
import com.sebastian.agent.orchestrator.domain.ports.SessionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class OrchestratorUseCase {
    private static final Logger log = LoggerFactory.getLogger(OrchestratorUseCase.class);

    private final SessionRepository sessionRepository;
    private final IntentClassifier intentClassifier;
    private final RateLimitPolicy rateLimitPolicy;
    private final AgentClient agentClient;

    public OrchestratorUseCase(
            SessionRepository sessionRepository,
            IntentClassifier intentClassifier,
            RateLimitPolicy rateLimitPolicy,
            AgentClient agentClient) {
        this.sessionRepository = sessionRepository;
        this.intentClassifier = intentClassifier;
        this.rateLimitPolicy = rateLimitPolicy;
        this.agentClient = agentClient;

    }

    public OrchestratorResult handle(String sessionId, String message, String section, String clientIp) {
        long startedAt = System.nanoTime();
        String resolvedSessionId = resolveSessionId(sessionId);

        VisitorSession session = findOrCreateSession(resolvedSessionId);
        VisitorSession updatedSession = incrementMessageCount(session);

        ChatIntent intent = intentClassifier.classify(message);

        RateLimitContext rateLimitContext = createRateLimitContext(
                resolvedSessionId,
                clientIp,
                intent,
                updatedSession.messageCount()
        );

        if (!rateLimitPolicy.isAllowed(rateLimitContext)) {
            VisitorSession savedSession = sessionRepository.save(
                    updateSessionAgent(updatedSession, AgentType.ORCHESTRATOR)
            );

            logCompletion(
                    savedSession.sessionId(),
                    intent,
                    AgentType.ORCHESTRATOR,
                    RateLimitStatus.LIMITED,
                    ResponseType.GUARDIAN,
                    startedAt
            );

            return new OrchestratorResult(savedSession.sessionId(),
                    guardianReply(),
                    intent,
                    AgentType.ORCHESTRATOR,
                    RateLimitStatus.LIMITED,
                    ResponseType.GUARDIAN
            );
        }

        AgentType agentType = resolveAgent(intent);

        if (agentType == AgentType.ORCHESTRATOR) {
            VisitorSession savedSession = sessionRepository.save(
                    updateSessionAgent(updatedSession, AgentType.ORCHESTRATOR)
            );

            logCompletion(
                    savedSession.sessionId(),
                    intent,
                    AgentType.ORCHESTRATOR,
                    RateLimitStatus.ALLOWED,
                    ResponseType.CLARIFICATION,
                    startedAt
            );

            return new OrchestratorResult(
                    savedSession.sessionId(),
                    clarificationReply(),
                    intent,
                    AgentType.ORCHESTRATOR,
                    RateLimitStatus.ALLOWED,
                    ResponseType.CLARIFICATION
            );
        }

        AgentResponse agentResponse = agentClient.sendMessage(agentType, resolvedSessionId, message);

        if (agentResponse.status() == AgentResponseStatus.ERROR){
            VisitorSession savedSession = sessionRepository.save(
                    updateSessionAgent(updatedSession, AgentType.ORCHESTRATOR)
            );

            log.warn(
                    "orchestration_agent_error sessionId={} intent={} failedAgent={} agentStatus={} durationMs={}",
                    savedSession.sessionId(),
                    intent,
                    agentType,
                    agentResponse.status(),
                    durationMs(startedAt)
            );
            logCompletion(
                    savedSession.sessionId(),
                    intent,
                    AgentType.ORCHESTRATOR,
                    RateLimitStatus.ALLOWED,
                    ResponseType.ERROR,
                    startedAt
            );

            return new OrchestratorResult(
                    savedSession.sessionId(),
                    agentErrorReply(),
                    intent,
                    AgentType.ORCHESTRATOR,
                    RateLimitStatus.ALLOWED,
                    ResponseType.ERROR
            );
        }

        String reply = agentResponse.reply();

        VisitorSession savedSession = sessionRepository.save(
                updateSessionAgent(updatedSession, agentType)
        );

        logCompletion(
                savedSession.sessionId(),
                intent,
                agentType,
                RateLimitStatus.ALLOWED,
                ResponseType.AGENT_REPLY,
                startedAt
        );

        return new OrchestratorResult(
                savedSession.sessionId(),
                reply,
                intent,
                agentType,
                RateLimitStatus.ALLOWED,
                ResponseType.AGENT_REPLY
        );

    }

    private String guardianReply() {
        return "Has explorado bastante sobre Sebastian y sus proyectos. Si quieres, puedo ayudarte a dejarle un mensaje.";
    }

    private String clarificationReply() {
        return "Puedo ayudarte con información sobre Sebastian, sus proyectos o con dejarle un mensaje. ¿Qué te gustaría hacer?";
    }

    private String resolveSessionId(String sessionId){
        if (sessionId == null || sessionId.isBlank()){
            return UUID.randomUUID().toString();
        }

        return sessionId;
    }

    private VisitorSession findOrCreateSession(String sessionId){
        return sessionRepository.findById(sessionId)
                .orElseGet(()->{
                    Instant now = Instant.now();

                    return new VisitorSession(
                            sessionId,
                            null,
                            0,
                            false,
                            now,
                            now
                    );
                });
    }

    private VisitorSession incrementMessageCount(VisitorSession session){
        return new VisitorSession(
                session.sessionId(),
                session.currentAgent(),
                session.messageCount() + 1,
                session.contactCaptureStarted(),
                session.createdAt(),
                Instant.now()
        );
    }

    private AgentType resolveAgent(ChatIntent intent){
        return switch (intent) {
            case PROFILE -> AgentType.PROFILE;
            case CONTACT -> AgentType.CONTACT;
            case UNCLEAR -> AgentType.ORCHESTRATOR;
        };
    }

    private VisitorSession updateSessionAgent(VisitorSession session, AgentType agentType){
        return new VisitorSession(
                session.sessionId(),
                agentType,
                session.messageCount(),
                agentType == AgentType.CONTACT || session.contactCaptureStarted(),
                session.createdAt(),
                Instant.now()
        );
    }

    private RateLimitContext createRateLimitContext (
            String sessionId,
            String clientIp,
            ChatIntent intent,
            int messageCount) {

        return new RateLimitContext(
                sessionId,
                clientIp,
                intent,
                messageCount
                );
    }

    private String agentErrorReply() {
        return "No pude contactar al agente en este momento. Intenta de nuevo en unos minutos";
    }

    private void logCompletion(
            String sessionId,
            ChatIntent intent,
            AgentType agentUsed,
            RateLimitStatus rateLimitStatus,
            ResponseType responseType,
            long startedAt
    ) {
        log.info(
                "orchestration_completed sessionId={} intent={} agentUsed={} rateLimitStatus={} responseType={} durationMs={}",
                sessionId,
                intent,
                agentUsed,
                rateLimitStatus,
                responseType,
                durationMs(startedAt)
        );
    }

    private long durationMs(long startedAt) {
        return (System.nanoTime() - startedAt) / 1_000_000;
    }
}
