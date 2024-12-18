package com.recargapay.code.assessment.ports.helper;

import java.time.Instant;
import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class JwtPropertiesValidator {
    private String issuer;
    private Instant expiration;
    private List<String> rollesAllowed;
    private String jkws;
    private String clientId;
}