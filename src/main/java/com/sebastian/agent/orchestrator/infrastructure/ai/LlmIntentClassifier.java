package com.sebastian.agent.orchestrator.infrastructure.ai;

import com.sebastian.agent.orchestrator.domain.model.ChatIntent;
import com.sebastian.agent.orchestrator.domain.ports.IntentClassifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("llm")
public class LlmIntentClassifier implements IntentClassifier {
    private static final Logger log = LoggerFactory.getLogger(LlmIntentClassifier.class);

    private final GeminiIntentClient geminiIntentClient;

    public LlmIntentClassifier(GeminiIntentClient geminiIntentClient) {
        this.geminiIntentClient = geminiIntentClient;
    }

    @Override
    public ChatIntent classify(String message) {
        long startedAt = System.nanoTime();
        ChatIntent intent = geminiIntentClient.classify(buildPrompt(message))
                .map(this::parseIntent)
                .orElse(ChatIntent.UNCLEAR);

        log.debug(
                "llm_intent_classified intent={} messageLength={} durationMs={}",
                intent,
                message == null ? 0 : message.length(),
                durationMs(startedAt)
        );

        return intent;
    }

    private String buildPrompt(String message) {
        return """
                  Clasifica este mensaje en una sola palabra:
                  - PROFILE: preguntas sobre Sebastian, su experiencia, stack, proyectos o decisiones tecnicas.
                  - CONTACT: quiere contactar, dejar mensaje, enviar correo, hablar por LinkedIn o iniciar una conversacion laboral.
                  - UNCLEAR: no esta claro.

                  Mensaje: "%s"

                  Responde SOLO una palabra:
                  PROFILE | CONTACT | UNCLEAR
                  """.formatted(message);
    }

    private ChatIntent parseIntent(String rawIntent) {
        String normalized = rawIntent
                .trim()
                .toUpperCase();

        if (normalized.contains("PROFILE")){
            return ChatIntent.PROFILE;
        }

        if (normalized.contains("CONTACT")) {
            return ChatIntent.CONTACT;
        }

        return ChatIntent.UNCLEAR;
    }

    private long durationMs(long startedAt) {
        return (System.nanoTime() - startedAt) / 1_000_000;
    }
}
