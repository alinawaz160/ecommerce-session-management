package com.ecommerce.cart.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Configuration
public class CheckoutRestTemplateConfig {

    @Value("${checkout-service.base-url}")
    private String checkoutServiceBaseUrl;

    @Bean("checkoutRestTemplate")
    public RestTemplate checkoutRestTemplate(RestTemplateBuilder builder) {
        return builder
            .rootUri(checkoutServiceBaseUrl)
            .setConnectTimeout(Duration.ofSeconds(5))
            .setReadTimeout(Duration.ofSeconds(10))
            .build();
    }
}
