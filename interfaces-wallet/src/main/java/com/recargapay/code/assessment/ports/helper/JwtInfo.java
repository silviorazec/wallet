package com.recargapay.code.assessment.ports.helper;

import java.time.Instant;
import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class JwtInfo {
	private String userId;
	private List<String> roles;
	private Instant exp;
	private String issuer;
}