package com.sebastian.agent.orchestrator.infrastructure.ai;

import com.sebastian.agent.orchestrator.infrastructure.ai.dto.GeminiGenerateContentRequest;
import com.sebastian.agent.orchestrator.infrastructure.ai.dto.GeminiGenerateContentResponse;
import com.sebastian.agent.orchestrator.infrastructure.config.OrchestratorProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Optional;

@Component
@Profile("llm")
public class GeminiIntentClient {
    private static final Logger log = LoggerFactory.getLogger(GeminiIntentClient.class);
    private static final String GEMINI_BASE_URL = "https://generativelanguage.googleapis.com";

    private final RestClient restClient;
    private  final OrchestratorProperties orchestratorProperties;

    public GeminiIntentClient(RestClient.Builder builder, OrchestratorProperties orchestratorProperties) {
        this.restClient = builder.clone().baseUrl(GEMINI_BASE_URL).build();
        this.orchestratorProperties= orchestratorProperties;
    }

    public Optional<String> classify(String prompt) {
        long startedAt = System.nanoTime();
        String apiKey = orchestratorProperties.intentClassifier().apiKey();

        if(apiKey == null || apiKey.isBlank()) {
            log.warn(
                    "gemini_intent_skipped reason=missing_api_key model={} durationMs={}",
                    orchestratorProperties.intentClassifier().model(),
                    durationMs(startedAt)
            );
            return Optional.empty();
        }

        try{
            GeminiGenerateContentResponse response = restClient.post()
                    .uri("/v1beta/models/{model}:generateContent",
                            orchestratorProperties.intentClassifier().model())
                    .header("x-goog-api-key", apiKey)
                    .body(GeminiGenerateContentRequest.fromText(prompt))
                    .retrieve()
                    .body(GeminiGenerateContentResponse.class);

            Optional<String> extractedText = extractText(response);

            if (extractedText.isEmpty()) {
                log.warn(
                        "gemini_intent_empty_response model={} durationMs={}",
                        orchestratorProperties.intentClassifier().model(),
                        durationMs(startedAt)
                );
            } else {
                log.debug(
                        "gemini_intent_completed model={} durationMs={}",
                        orchestratorProperties.intentClassifier().model(),
                        durationMs(startedAt)
                );
            }

            return extractedText;
        }catch (RestClientException e) {
            log.warn(
                    "gemini_intent_failed model={} durationMs={}",
                    orchestratorProperties.intentClassifier().model(),
                    durationMs(startedAt),
                    e
            );
            return Optional.empty();
        }
    }

    private Optional<String> extractText(GeminiGenerateContentResponse response) {
        if (response == null ||
        response.candidates() == null ||
        response.candidates().isEmpty() ||
        response.candidates().getFirst().content() == null ||
        response.candidates().getFirst().content().parts() == null ||
        response.candidates().getFirst().content().parts().isEmpty()) {
            return Optional.empty();
        }

        String text = response.candidates()
                .getFirst()
                .content()
                .parts()
                .getFirst()
                .text();

        if (text == null || text.isBlank()){
            return Optional.empty();
        }

        return Optional.of(text.trim());
    }

    private long durationMs(long startedAt) {
        return (System.nanoTime() - startedAt) / 1_000_000;
    }
}
