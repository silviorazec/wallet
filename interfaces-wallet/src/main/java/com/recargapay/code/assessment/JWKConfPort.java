package com.recargapay.code.assessment;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class JWKConfPort{

    @Bean("jwkJsonPort")
    String jwkJsonPort(WebClient.Builder builder) {
        return builder
            .baseUrl("http://keycloak:8080")
            .build()
            .get()
            .uri("/realms/recargapay_wallet_test/protocol/openid-connect/certs")
    		.retrieve()
    		.bodyToMono(String.class)
    		.block();
    }
}
