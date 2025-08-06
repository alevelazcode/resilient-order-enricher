package com.resilient.orderworker.infrastructure.webclient;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * WebClient configuration for external API calls with proper timeouts and connection management.
 */
@Configuration
public class WebClientConfig {
    
    @Value("${enricher-api.base-url:http://localhost:8080}")
    private String enricherApiBaseUrl;
    
    @Value("${enricher-api.timeout.connect:5000}")
    private int connectTimeout;
    
    @Value("${enricher-api.timeout.read:10000}")
    private int readTimeout;
    
    @Value("${enricher-api.timeout.write:10000}")
    private int writeTimeout;
    
    @Value("${enricher-api.timeout.response:30000}")
    private long responseTimeout;
    
    @Bean("enricherApiWebClient")
    public WebClient enricherApiWebClient() {
        HttpClient httpClient = HttpClient.create()
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeout)
            .responseTimeout(Duration.ofMillis(responseTimeout))
            .doOnConnected(conn -> 
                conn.addHandlerLast(new ReadTimeoutHandler(readTimeout, TimeUnit.MILLISECONDS))
                    .addHandlerLast(new WriteTimeoutHandler(writeTimeout, TimeUnit.MILLISECONDS)));
        
        return WebClient.builder()
            .baseUrl(enricherApiBaseUrl)
            .clientConnector(new ReactorClientHttpConnector(httpClient))
            .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(1024 * 1024)) // 1MB
            .build();
    }
}