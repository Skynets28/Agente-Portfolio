package com.sebastian.agent.orchestrator.domain.ports;

import com.sebastian.agent.orchestrator.domain.model.RateLimitContext;

public interface RateLimitPolicy {

    boolean isAllowed(RateLimitContext context);
}
