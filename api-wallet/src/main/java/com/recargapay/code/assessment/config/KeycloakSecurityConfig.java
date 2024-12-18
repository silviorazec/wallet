package com.recargapay.code.assessment.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

import com.recargapay.code.assessment.ports.helper.JwtHelper;

@Configuration
@EnableMethodSecurity
public class KeycloakSecurityConfig {

	@Value("${keycloak.resource}")
	private String clientId;

	JwtHelper jwtHelper;

	public KeycloakSecurityConfig(JwtHelper jwtHelper) {
		this.jwtHelper = jwtHelper;
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		
		http.authorizeHttpRequests(auth -> auth.requestMatchers("/public/**").permitAll().anyRequest().authenticated())
				.oauth2ResourceServer(
						oauth2 -> oauth2.jwt(jwt -> { 
						jwt.jwtAuthenticationConverter(jwtAuthenticationConverter());}
						) 
				);

		return http.build();
	}

	@Bean
	public JwtAuthenticationConverter jwtAuthenticationConverter() {
		
		
		JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();

		jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(jwt -> {
			var authorities = new ArrayList<GrantedAuthority>();
			List<String> roles = jwtHelper.extractRoles(jwt, clientId);
			roles.forEach(role -> {
				authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
			});
			return authorities;
		});

		return jwtAuthenticationConverter;
	}

}
