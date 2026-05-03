package com.sebastian.agent.orchestrator.infrastructure.ratelimit;

import com.sebastian.agent.orchestrator.domain.model.ChatIntent;
import com.sebastian.agent.orchestrator.domain.model.RateLimitContext;
import com.sebastian.agent.orchestrator.infrastructure.config.OrchestratorProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class RedisRateLimitPolicyTest {

    private static final String RATE_LIMIT_KEY = "rl:127.0.0.1:session-1";
    private static final Duration RATE_LIMIT_WINDOW = Duration.ofHours(1);

    private StringRedisTemplate redisTemplate;
    private ValueOperations<String, String> valueOperations;
    private RedisRateLimitPolicy policy;

    @BeforeEach
    void setUp() {
        redisTemplate = mock(StringRedisTemplate.class);
        valueOperations = mock(ValueOperations.class);
        policy = new RedisRateLimitPolicy(properties(), redisTemplate);
    }

    @Test
    void allowsContactIntentWithoutUsingRedis() {
        RateLimitContext context = context(ChatIntent.CONTACT, 41);

        boolean allowed = policy.isAllowed(context);

        assertThat(allowed).isTrue();
        verifyNoInteractions(redisTemplate);
    }

    @Test
    void allowsRequestAtRedisLimit() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.increment(RATE_LIMIT_KEY)).thenReturn(40L);

        boolean allowed = policy.isAllowed(context(ChatIntent.PROFILE, 1));

        assertThat(allowed).isTrue();
        verify(redisTemplate, never()).expire(RATE_LIMIT_KEY, RATE_LIMIT_WINDOW);
    }

    @Test
    void blocksRequestAfterRedisLimit() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.increment(RATE_LIMIT_KEY)).thenReturn(41L);

        boolean allowed = policy.isAllowed(context(ChatIntent.PROFILE, 1));

        assertThat(allowed).isFalse();
        verify(redisTemplate, never()).expire(RATE_LIMIT_KEY, RATE_LIMIT_WINDOW);
    }

    @Test
    void setsExpirationWhenRedisCounterIsCreated() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.increment(RATE_LIMIT_KEY)).thenReturn(1L);

        boolean allowed = policy.isAllowed(context(ChatIntent.UNCLEAR, 41));

        assertThat(allowed).isTrue();
        verify(redisTemplate).expire(RATE_LIMIT_KEY, RATE_LIMIT_WINDOW);
    }

    @Test
    void allowsRequestWhenRedisReturnsNullCounter() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.increment(RATE_LIMIT_KEY)).thenReturn(null);

        boolean allowed = policy.isAllowed(context(ChatIntent.PROFILE, 41));

        assertThat(allowed).isTrue();
        verify(redisTemplate, never()).expire(RATE_LIMIT_KEY, RATE_LIMIT_WINDOW);
    }

    @Test
    void allowsRequestWhenRedisIsUnavailable() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.increment(RATE_LIMIT_KEY))
                .thenThrow(new RedisConnectionFailureException("Redis unavailable"));

        boolean allowed = policy.isAllowed(context(ChatIntent.PROFILE, 41));

        assertThat(allowed).isTrue();
    }

    private RateLimitContext context(ChatIntent intent, int messageCount) {
        return new RateLimitContext(
                "session-1",
                "127.0.0.1",
                intent,
                messageCount
        );
    }

    private OrchestratorProperties properties() {
        return new OrchestratorProperties(
                new OrchestratorProperties.Session(Duration.ofHours(2), "session:"),
                new OrchestratorProperties.RateLimit(40, "rl:", RATE_LIMIT_WINDOW),
                new OrchestratorProperties.Agents(
                        "http://localhost:9091",
                        "http://localhost:9092",
                        Duration.ofMillis(500),
                        Duration.ofSeconds(2)
                )
        );
    }
}
