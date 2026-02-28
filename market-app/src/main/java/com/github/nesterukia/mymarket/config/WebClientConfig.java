package com.github.nesterukia.mymarket.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ReactorResourceFactory;
import org.springframework.http.client.reactive.ClientHttpConnector;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;

@Configuration
public class WebClientConfig {
    public static final long TIMEOUT_MS = 1000;

    @Bean
    public ReactorResourceFactory resourceFactory() {
        ReactorResourceFactory factory = new ReactorResourceFactory();
        factory.setUseGlobalResources(false);
        return factory;
    }

    @Bean
    public WebClient webClient(ReactorResourceFactory resourceFactory) {
        ClientHttpConnector connector = new ReactorClientHttpConnector(
                resourceFactory,
                client -> client.responseTimeout(Duration.ofMillis(TIMEOUT_MS))
        );

        return WebClient.builder().clientConnector(connector).build();
    }
}
