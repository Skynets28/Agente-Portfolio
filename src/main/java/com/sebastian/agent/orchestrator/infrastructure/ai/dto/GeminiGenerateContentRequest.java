package com.sebastian.agent.orchestrator.infrastructure.ai.dto;

import java.util.List;

public record GeminiGenerateContentRequest(
        List<content> contents
) {

    public static GeminiGenerateContentRequest fromText(String text){
        return new GeminiGenerateContentRequest(
                List.of(new content(List.of(new Part(text))))
        );
    }

    public record content(
        List<Part> parts
    ){
    }

    public record Part(
            String text
    ) {
    }
}
