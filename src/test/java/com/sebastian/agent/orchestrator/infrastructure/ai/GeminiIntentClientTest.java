package com.sebastian.agent.orchestrator.infrastructure.ai;

import com.sebastian.agent.orchestrator.infrastructure.config.OrchestratorProperties;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class GeminiIntentClientTest {

    @Test
    void returnsGeneratedTextWhenGeminiRespondsWithCandidate() {
        RestClient.Builder restClientBuilder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(restClientBuilder).build();
        GeminiIntentClient client = new GeminiIntentClient(restClientBuilder, properties("test-api-key"));

        server.expect(requestTo("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("x-goog-api-key", "test-api-key"))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json("""
                        {
                          "contents": [
                            {
                              "parts": [
                                {
                                  "text": "classify this"
                                }
                              ]
                            }
                          ]
                        }
                        """))
                .andRespond(withSuccess("""
                        {
                          "candidates": [
                            {
                              "content": {
                                "parts": [
                                  {
                                    "text": " CONTACT "
                                  }
                                ]
                              }
                            }
                          ]
                        }
                        """, MediaType.APPLICATION_JSON));

        Optional<String> result = client.classify("classify this");

        assertThat(result).contains("CONTACT");
        server.verify();
    }

    @Test
    void returnsEmptyWhenApiKeyIsMissing() {
        RestClient.Builder restClientBuilder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(restClientBuilder).build();
        GeminiIntentClient client = new GeminiIntentClient(restClientBuilder, properties(" "));

        Optional<String> result = client.classify("classify this");

        assertThat(result).isEmpty();
        server.verify();
    }

    @Test
    void returnsEmptyWhenGeminiReturnsServerError() {
        RestClient.Builder restClientBuilder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(restClientBuilder).build();
        GeminiIntentClient client = new GeminiIntentClient(restClientBuilder, properties("test-api-key"));

        server.expect(requestTo("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withServerError());

        Optional<String> result = client.classify("classify this");

        assertThat(result).isEmpty();
        server.verify();
    }

    @Test
    void returnsEmptyWhenGeminiTextIsBlank() {
        RestClient.Builder restClientBuilder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(restClientBuilder).build();
        GeminiIntentClient client = new GeminiIntentClient(restClientBuilder, properties("test-api-key"));

        server.expect(requestTo("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("""
                        {
                          "candidates": [
                            {
                              "content": {
                                "parts": [
                                  {
                                    "text": " "
                                  }
                                ]
                              }
                            }
                          ]
                        }
                        """, MediaType.APPLICATION_JSON));

        Optional<String> result = client.classify("classify this");

        assertThat(result).isEmpty();
        server.verify();
    }

    private OrchestratorProperties properties(String apiKey) {
        return new OrchestratorProperties(
                new OrchestratorProperties.Session(Duration.ofHours(2), "session:"),
                new OrchestratorProperties.RateLimit(40, "rl:", Duration.ofHours(1)),
                new OrchestratorProperties.Agents(
                        "http://localhost:9091",
                        "http://localhost:9092",
                        Duration.ofMillis(500),
                        Duration.ofSeconds(2)
                ),
                new OrchestratorProperties.IntentClassifierProperties(
                        "gemini",
                        "gemini-2.5-flash",
                        Duration.ofSeconds(2),
                        apiKey
                )
        );
    }
}
