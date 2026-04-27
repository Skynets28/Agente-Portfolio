package com.sebastian.agent.orchestrator.domain.ports;

import com.sebastian.agent.orchestrator.domain.model.ChatIntent;
import com.sebastian.agent.orchestrator.domain.model.VisitorSession;

public interface RateLimitPolicy {

    boolean isAllowed(VisitorSession session, ChatIntent intent);
}
