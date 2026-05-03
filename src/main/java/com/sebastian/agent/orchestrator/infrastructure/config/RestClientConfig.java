package com.sebastian.agent.orchestrator.infrastructure.config;

import org.springframework.boot.http.client.ClientHttpRequestFactoryBuilder;
import org.springframework.boot.http.client.HttpClientSettings;
import org.springframework.boot.restclient.RestClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RestClientConfig {

    @Bean
    RestClientCustomizer agentRestClientTimeoutCustomizer(OrchestratorProperties orchestratorProperties) {
        HttpClientSettings settings = HttpClientSettings.defaults()
                .withConnectTimeout(orchestratorProperties.agents().connectTimeout())
                .withReadTimeout(orchestratorProperties.agents().readTimeout());

        return builder -> builder.requestFactory(
                ClientHttpRequestFactoryBuilder.detect().build(settings)
        );
    }
}
