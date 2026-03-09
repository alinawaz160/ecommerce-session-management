package com.ecommerce.checkout.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Configuration
public class OrderServiceRestTemplateConfig {

    @Value("${order-service.base-url}")
    private String orderServiceBaseUrl;

    @Bean("orderRestTemplate")
    public RestTemplate orderRestTemplate(RestTemplateBuilder builder) {
        return builder
            .rootUri(orderServiceBaseUrl)
            .setConnectTimeout(Duration.ofSeconds(5))
            .setReadTimeout(Duration.ofSeconds(10))
            .build();
    }
}
