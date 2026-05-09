package com.sebastian.agent.orchestrator.infrastructure.ratelimit;

import com.sebastian.agent.orchestrator.domain.model.ChatIntent;
import com.sebastian.agent.orchestrator.domain.model.RateLimitContext;
import com.sebastian.agent.orchestrator.infrastructure.config.OrchestratorProperties;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class SimpleRateLimitPolicyTest {

    private final SimpleRateLimitPolicy policy = new SimpleRateLimitPolicy(
            new OrchestratorProperties(
                    new OrchestratorProperties.Session(Duration.ofHours(2), "session:"),
                    new OrchestratorProperties.RateLimit(40, "rl:", Duration.ofHours(1)),
                    new OrchestratorProperties.Agents(
                            "http://localhost:9091",
                            "http://localhost:9092",
                            Duration.ofMillis(500),
                            Duration.ofSeconds(2)
                    ),
                    new OrchestratorProperties.IntentClassifierProperties(
                            "gemini",
                            "gemini-2.5-flash",
                            Duration.ofSeconds(2),
                            "test-api-key"
                    )
            )
    );

    @Test
    void allowsProfileIntentBeforeMessageLimit() {
        RateLimitContext context = new RateLimitContext(
                "session-1",
                "127.0.0.1",
                ChatIntent.PROFILE,
                39
        );

        assertThat(policy.isAllowed(context)).isTrue();
    }

    @Test
    void allowsProfileIntentAtMessageLimit() {
        RateLimitContext context = new RateLimitContext(
                "session-1",
                "127.0.0.1",
                ChatIntent.PROFILE,
                40
        );

        assertThat(policy.isAllowed(context)).isTrue();
    }

    @Test
    void blocksProfileIntentAfterMessageLimit() {
        RateLimitContext context = new RateLimitContext(
                "session-1",
                "127.0.0.1",
                ChatIntent.PROFILE,
                41
        );

        assertThat(policy.isAllowed(context)).isFalse();
    }

    @Test
    void allowsContactIntentAfterMessageLimit() {
        RateLimitContext context = new RateLimitContext(
                "session-1",
                "127.0.0.1",
                ChatIntent.CONTACT,
                41
        );

        assertThat(policy.isAllowed(context)).isTrue();
    }

    @Test
    void blocksUnclearIntentAfterMessageLimit() {
        RateLimitContext context = new RateLimitContext(
                "session-1",
                "127.0.0.1",
                ChatIntent.UNCLEAR,
                41
        );

        assertThat(policy.isAllowed(context)).isFalse();
    }
}
