package com.sebastian.agent.orchestrator.infrastructure.ratelimit;

import com.sebastian.agent.orchestrator.domain.model.ChatIntent;
import com.sebastian.agent.orchestrator.domain.model.RateLimitContext;
import com.sebastian.agent.orchestrator.domain.ports.RateLimitPolicy;
import com.sebastian.agent.orchestrator.infrastructure.config.OrchestratorProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.RedisSystemException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;


@Component
@Profile({"redis"})
public class RedisRateLimitPolicy implements RateLimitPolicy {
    private static final Logger log = LoggerFactory.getLogger(RedisRateLimitPolicy.class);

    private final OrchestratorProperties orchestratorProperties;
    private final StringRedisTemplate stringRedisTemplate;

    public RedisRateLimitPolicy(
            OrchestratorProperties orchestratorProperties,
            StringRedisTemplate stringRedisTemplate) {
        this.orchestratorProperties = orchestratorProperties;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public boolean isAllowed(RateLimitContext context) {
        if (context.intent() == ChatIntent.CONTACT) {
            return true;
        }
        try{
            String key = buildKey(context);
            Long count = stringRedisTemplate.opsForValue().increment(key);

            if (count != null && count == 1L) {
                stringRedisTemplate.expire(key, orchestratorProperties.rateLimit().window());
            }

            if (count ==  null){
                return true;
            }
            return count <= orchestratorProperties.rateLimit().maxMessagesPerSession();
        }catch (RedisConnectionFailureException | RedisSystemException exception) {
            log.warn("rate_limit_redis_unavailable sessionId={}", context.sessionId(), exception);
            return true;
        }
    }

    private String buildKey(RateLimitContext context) {
        return orchestratorProperties.rateLimit().keyPrefix()
                + context.clientIp()
                + ":"
                + context.sessionId();
    }
}
