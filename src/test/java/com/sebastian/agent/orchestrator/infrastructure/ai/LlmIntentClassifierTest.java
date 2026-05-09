package com.sebastian.agent.orchestrator.infrastructure.ai;

import com.sebastian.agent.orchestrator.domain.model.ChatIntent;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LlmIntentClassifierTest {

    private final GeminiIntentClient geminiIntentClient = mock(GeminiIntentClient.class);
    private final LlmIntentClassifier classifier = new LlmIntentClassifier(geminiIntentClient);

    @Test
    void returnsProfileWhenGeminiClassifiesProfileIntent() {
        when(geminiIntentClient.classify(contains("Tell me about Sebastian")))
                .thenReturn(Optional.of("PROFILE"));

        ChatIntent intent = classifier.classify("Tell me about Sebastian");

        assertThat(intent).isEqualTo(ChatIntent.PROFILE);
    }

    @Test
    void returnsContactWhenGeminiClassifiesContactIntent() {
        when(geminiIntentClient.classify(contains("Quiero dejarle un mensaje")))
                .thenReturn(Optional.of("CONTACT"));

        ChatIntent intent = classifier.classify("Quiero dejarle un mensaje");

        assertThat(intent).isEqualTo(ChatIntent.CONTACT);
    }

    @Test
    void returnsUnclearWhenGeminiClassifiesUnclearIntent() {
        when(geminiIntentClient.classify(contains("hola")))
                .thenReturn(Optional.of("UNCLEAR"));

        ChatIntent intent = classifier.classify("hola");

        assertThat(intent).isEqualTo(ChatIntent.UNCLEAR);
    }

    @Test
    void returnsUnclearWhenGeminiDoesNotReturnText() {
        when(geminiIntentClient.classify(contains("hola")))
                .thenReturn(Optional.empty());

        ChatIntent intent = classifier.classify("hola");

        assertThat(intent).isEqualTo(ChatIntent.UNCLEAR);
    }

    @Test
    void normalizesExtraTextAroundKnownIntent() {
        when(geminiIntentClient.classify(contains("email")))
                .thenReturn(Optional.of("Intent: contact"));

        ChatIntent intent = classifier.classify("Can I send him an email?");

        assertThat(intent).isEqualTo(ChatIntent.CONTACT);
    }
}
