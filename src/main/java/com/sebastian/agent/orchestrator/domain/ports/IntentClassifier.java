package com.sebastian.agent.orchestrator.domain.ports;

import com.sebastian.agent.orchestrator.domain.model.ChatIntent;

public interface IntentClassifier {

    ChatIntent classify(String message);
}
