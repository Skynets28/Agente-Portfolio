package com.sebastian.agent.orchestrator.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "orchestrator")
public record OrchestratorProperties(
        Session session,
        RateLimit rateLimit,
        Agents agents,
        IntentClassifierProperties intentClassifier
) {
    public record Session(
            Duration ttl,
            String keyPrefix
    ) {
    }

    public record RateLimit(
            int maxMessagesPerSession,
            String keyPrefix,
            Duration window
    ) {
    }

    public record Agents (
            String profileBaseUrl,
            String contactBaseUrl,
            Duration connectTimeout,
            Duration readTimeout
    ) {
    }

    public record IntentClassifierProperties(
            String provider,
            String model,
            Duration timeout,
            String apiKey
    ) {
    }
}
