package com.sebastian.agent.orchestrator.api.chat;

import com.sebastian.agent.orchestrator.api.chat.dto.ChatRequest;
import com.sebastian.agent.orchestrator.api.chat.dto.ChatResponse;
import com.sebastian.agent.orchestrator.application.chat.OrchestratorUseCase;
import com.sebastian.agent.orchestrator.domain.model.OrchestratorResult;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/agent")
public class ChatController {
    private final OrchestratorUseCase orchestratorUseCase;

    public ChatController(OrchestratorUseCase orchestratorUseCase){
        this.orchestratorUseCase = orchestratorUseCase;
    }

    @PostMapping("/chat")
    public ResponseEntity<ChatResponse> chat(
            @Valid @RequestBody ChatRequest request,
            HttpServletRequest httpRequest) {
        OrchestratorResult result = orchestratorUseCase.handle(
                request.sessionId(),
                request.message(),
                request.section(),
                resolveClientIp(httpRequest)
        );

        return  ResponseEntity.ok(toResponse(result));
    }

    private ChatResponse toResponse(OrchestratorResult result) {
        return new ChatResponse(
                result.sessionId(),
                result.reply(),
                result.intent().name(),
                result.agentUsed().name(),
                result.rateLimitStatus()
        );
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");

        if(forwardedFor != null && !forwardedFor.isBlank()){
            return forwardedFor.split(",")[0].trim();
        }

        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isBlank()) {
            return realIp;
        }

        return request.getRemoteAddr();
    }
}
