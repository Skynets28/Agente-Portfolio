package com.sebastian.agent.orchestrator.infrastructure.routing;

import com.sebastian.agent.orchestrator.domain.model.ChatIntent;
import com.sebastian.agent.orchestrator.domain.model.VisitorSession;
import com.sebastian.agent.orchestrator.domain.ports.IntentClassifier;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile({"simple"})
public class SimpleIntentClassifier implements IntentClassifier {
    @Override
    public  ChatIntent classify(String message) {
        String normalized = message.toLowerCase();

        if (normalized.contains("contact") ||
                normalized.contains("message") ||
                normalized.contains("email") ||
                normalized.contains("linkedin")) {
            return ChatIntent.CONTACT;
        }

        if (normalized.contains("sebastian") ||
                normalized.contains("proyecto")||
                normalized.contains("stack") ||
                normalized.contains("experiencia") ||
                normalized.contains("java")) {
            return ChatIntent.PROFILE;
        }

        return ChatIntent.UNCLEAR;
    }
}
