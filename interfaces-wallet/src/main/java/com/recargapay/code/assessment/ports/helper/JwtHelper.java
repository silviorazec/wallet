package com.recargapay.code.assessment.ports.helper;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.recargapay.code.assessment.ports.exceptions.JwtHeltperException;
import com.recargapay.code.assessment.ports.exceptions.WalletException;

@Component
public class JwtHelper {

	private String jwkJsonPort;

	public JwtHelper(@Qualifier("jwkJsonPort") String jwkJsonPort) {
		this.jwkJsonPort = jwkJsonPort;
	}

	public JwtInfo extractJwtInfo(Authentication authentication, String clientId) throws JwtHeltperException {
		try {
			String userId = getUserId(authentication);
			List<String> roles = getUserRoles(authentication, clientId);

			return JwtInfo.builder().roles(roles).userId(userId).build();
		} catch (Exception e) {
			throw new JwtHeltperException("Failed to extract JWT information", e);
		}
	}

	public JwtInfo extractJwtInfo(String token, String clientId) throws JwtHeltperException {
		try {
			String userId = getClaim("sub", token).toString();
			List<String> roles = extractRoles(jwkJsonPort, clientId, token);
			return JwtInfo.builder().roles(roles).userId(userId)
					.exp(Instant.ofEpochSecond(Long.parseLong(getClaim("exp", token).toString())))
					.issuer(getClaim("iss", token).toString()).build();
		} catch (Exception e) {
			throw new JwtHeltperException("Failed to extract JWT information", e);
		}
	}

	public Jwt getJwtDecode(JWK jwk, String token) throws JOSEException {

		JwtDecoder jwtDecoder = NimbusJwtDecoder.withPublicKey(jwk.toRSAKey().toRSAPublicKey()).build();
		return jwtDecoder.decode(token);

	}



	public String getUserId(Authentication authentication) {
		Jwt jwt = ((JwtAuthenticationToken) authentication).getToken();
		return jwt.getClaimAsString("sub");
	}

	public String getJwt(Authentication authentication) {
		return ((JwtAuthenticationToken) authentication).getToken().getTokenValue();
	}

	public Object getClaim(String claimName, String jwtToken) throws WalletException {
		try {
			String[] tokenParts = jwtToken.split("\\.");
			String payload = new String(Base64.getDecoder().decode(tokenParts[1]));
			ObjectMapper objectMapper = new ObjectMapper();
			Map<String, Object> claims = objectMapper.readValue(payload, Map.class);

			return claims.getOrDefault(claimName, null).toString();
		} catch (Exception e) {
			throw new WalletException("Problems to extrat claim", e);
		}
	}

	public  List<String> extractRoles(String jwkJson, String clientId, String token) throws WalletException {
		try {
			System.out.println(jwkJson);
			JWKSet jwkSet = JWKSet.parse(jwkJson);

			JWK jwk = jwkSet.getKeys().get(0);

			JwtHelper jwtHelper = new JwtHelper(jwkJson);
			Jwt jwt = jwtHelper.getJwtDecode(jwk, token);
			List<String> roles = extractRoles(jwt, clientId);
			return roles;
		} catch (Exception e) {
			throw new WalletException("Problemas to extract roles", e);
		}
	}

	public  List<String> extractRoles(Jwt jwt, String clientId) {

		Map<String, Object> resourceAccess = jwt.getClaim("resource_access");

		List<String> clientIdRoles = Optional.ofNullable(resourceAccess).map(access -> access.get(clientId))
				.filter(Map.class::isInstance).map(Map.class::cast).map(clientRoles -> clientRoles.get("roles"))
				.filter(List.class::isInstance).map(List.class::cast).map(this::validateRoleList)
				.orElse(Collections.emptyList());

		List<String> realmRoles = Optional.ofNullable(jwt.getClaim("realm_access")).filter(Map.class::isInstance)
				.map(Map.class::cast).map(realmAccess -> realmAccess.get("roles")).filter(List.class::isInstance)
				.map(List.class::cast).map(this::validateRoleList).orElse(Collections.emptyList());

		var roles = new ArrayList<String>();
		clientIdRoles.forEach(role -> roles.add(role));
		realmRoles.forEach(role -> roles.add(role));
		return roles;
	}

	private  List<String> validateRoleList(List<?> rawList) {
		if (rawList.stream().allMatch(String.class::isInstance)) {
			return rawList.stream().map(String.class::cast).toList();
		}

		return Collections.emptyList();
	}

	private List<String> getUserRoles(Authentication authentication, String clientId) {

		return extractRoles((Jwt) authentication.getPrincipal(), clientId);

	}

	private boolean isTheSameUser(String userId1, String userId2) {
		return userId1.equals(userId2);
	}

}
