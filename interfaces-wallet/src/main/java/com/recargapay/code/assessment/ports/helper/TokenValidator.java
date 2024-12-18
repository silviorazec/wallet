package com.recargapay.code.assessment.ports.helper;

import java.util.List;
import java.util.Optional;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jwt.SignedJWT;
import com.recargapay.code.assessment.ports.exceptions.WalletException;

@Component
public class TokenValidator {


	public void validate(JwtPropertiesValidator validator, String token) throws WalletException  {
		try {
			
			JWKSet jwkSet = JWKSet.parse(validator.getJkws());

			JWK jwk = jwkSet.getKeys().get(0);

			JWSVerifier verifier = new RSASSAVerifier(jwk.toRSAKey());

			SignedJWT signedJWT = SignedJWT.parse(token);

			if (!signedJWT.verify(verifier)) {
				throw new SecurityException("Token signature is invalid");
			}

			if (!signedJWT.getJWTClaimsSet().getIssuer().endsWith(validator.getIssuer())) {
				throw new SecurityException("Invalid issuer");
			}

			JwtHelper jwtHelper = new JwtHelper(validator.getJkws());
			Jwt jwt = jwtHelper.getJwtDecode(jwk, token);

			if (validator.getExpiration().compareTo(jwt.getExpiresAt()) == 1) {
				throw new SecurityException("Expired");
			}

			List<String> roles = jwtHelper.extractRoles(jwt, validator.getClientId());

			Optional<String> roleAllowed = roles.stream().filter(role -> validator.getRollesAllowed().contains(role))
					.findFirst();

			if (roleAllowed.isEmpty()) {
				throw new SecurityException("Forbidden");
			}
		} catch (Exception e) {
			throw new WalletException("Invalid Token", e);
		}
	}


}
