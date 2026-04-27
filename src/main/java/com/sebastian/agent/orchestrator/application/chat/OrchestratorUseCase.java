package com.sebastian.agent.orchestrator.application.chat;

import com.sebastian.agent.orchestrator.domain.model.AgentType;
import com.sebastian.agent.orchestrator.domain.model.ChatIntent;
import com.sebastian.agent.orchestrator.domain.model.OrchestratorResult;
import com.sebastian.agent.orchestrator.domain.model.VisitorSession;
import com.sebastian.agent.orchestrator.domain.ports.AgentClient;
import com.sebastian.agent.orchestrator.domain.ports.IntentClassifier;
import com.sebastian.agent.orchestrator.domain.ports.RateLimitPolicy;
import com.sebastian.agent.orchestrator.domain.ports.SessionRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class OrchestratorUseCase {
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

    public OrchestratorResult handle(String sessionId, String message, String section) {
        String resolvedSessionId = resolveSessionId(sessionId);

        VisitorSession session = findOrCreateSession(resolvedSessionId);
        VisitorSession updatedSession = incrementMessageCount(session);

        ChatIntent intent = intentClassifier.classify(message);

        if (!rateLimitPolicy.isAllowed(updatedSession, intent)) {
            VisitorSession savedSession = sessionRepository.save(
                    updateSessionAgent(updatedSession, AgentType.ORCHESTRATOR)
            );

            return new OrchestratorResult(savedSession.sessionId(),
                    guardianReply(),
                    intent,
                    AgentType.ORCHESTRATOR,
                    "LIMITED"
            );
        }

        AgentType agentType = resolveAgent(intent);

        if (agentType == AgentType.ORCHESTRATOR) {
            VisitorSession savedSession = sessionRepository.save(
                    updateSessionAgent(updatedSession, AgentType.ORCHESTRATOR)
            );

            return new OrchestratorResult(
                    savedSession.sessionId(),
                    clarificationReply(),
                    intent,
                    AgentType.ORCHESTRATOR,
                    "ALLOWED"
            );
        }

        String reply = agentClient.sendMessage(agentType, resolvedSessionId, message);

        VisitorSession savedSession = sessionRepository.save(
                updateSessionAgent(updatedSession, agentType)
        );

        return new OrchestratorResult(
                savedSession.sessionId(),
                reply,
                intent,
                agentType,
                "ALLOWED"
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
}
