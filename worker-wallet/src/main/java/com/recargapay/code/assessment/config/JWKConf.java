package com.recargapay.code.assessment.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Configuration
public class JWKConf{

	@Bean("chaveIDP")
    String jwkJson(WebClient.Builder builder) {
		String jwks =  builder
        .baseUrl("http://keycloak:8080")
        .build()
        .get()
        .uri("/realms/recargapay_wallet_test/protocol/openid-connect/certs")
		.retrieve()
		.bodyToMono(String.class)
		.block();
		
		log.info("CHAVE PUBLICA: \n " + jwks);
        return jwks;
    }
}
