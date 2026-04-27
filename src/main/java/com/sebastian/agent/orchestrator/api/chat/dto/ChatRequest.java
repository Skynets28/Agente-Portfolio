package com.sebastian.agent.orchestrator.api.chat.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChatRequest(
        String sessionId,
        @NotBlank(message = "Message is required")
        @Size(max = 2_000, message = "Message must be 2000 characters or less")
        String message,
        @Size(max = 80, message = "Section must be 80 characters or less")
        String section
) {
}
