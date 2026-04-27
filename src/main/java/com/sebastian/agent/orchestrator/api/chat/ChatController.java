package com.sebastian.agent.orchestrator.api.chat;

import com.sebastian.agent.orchestrator.api.chat.dto.ChatRequest;
import com.sebastian.agent.orchestrator.api.chat.dto.ChatResponse;
import com.sebastian.agent.orchestrator.application.chat.OrchestratorUseCase;
import com.sebastian.agent.orchestrator.domain.model.OrchestratorResult;
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
    public ResponseEntity<ChatResponse> chat(@Valid @RequestBody ChatRequest request) {
        OrchestratorResult result = orchestratorUseCase.handle(
                request.sessionId(),
                request.message(),
                request.section()
        );

        return  ResponseEntity.ok(toResponse(result));
    }

    private ChatResponse toResponse(OrchestratorResult result){
        return new ChatResponse(
                result.sessionId(),
                result.reply(),
                result.intent().name(),
                result.agentUsed().name(),
                result.rateLimitStatus()
        );
    }
}
