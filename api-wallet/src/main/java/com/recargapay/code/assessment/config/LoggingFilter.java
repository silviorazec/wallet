package com.recargapay.code.assessment.config;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;

import com.recargapay.code.assessment.ports.helper.JwtHelper;
import com.recargapay.code.assessment.ports.helper.JwtInfo;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class LoggingFilter extends GenericFilterBean {
	
	@Value("${keycloak.resource}")
	private String clientId;
	private JwtHelper jwtHelper;
	
	public LoggingFilter(JwtHelper jwtHelper) {
		this.jwtHelper = jwtHelper;
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
	        throws IOException, ServletException {

	    HttpServletRequest httpRequest = (HttpServletRequest) request;
	    CachedBodyHttpServletRequest cachedRequest = new CachedBodyHttpServletRequest(httpRequest);

	    CapturingResponseWrapper responseWrapper = new CapturingResponseWrapper((HttpServletResponse) response);

	    // Leitura do corpo da requisição
	    String requestBody = new BufferedReader(new InputStreamReader(cachedRequest.getInputStream()))
	            .lines()
	            .reduce("", (accumulator, actual) -> accumulator + actual);

	    var jwtInfo = getInfo(httpRequest.getHeader("Authorization").replace("Bearer ", ""), clientId);
	    log.info("Requisição: [Método: {},  Corpo: {}, URI: {}, Info Token -> Emissor:{}, Roles {}, Usuario {}, Expiracao: {}]",
	            httpRequest.getMethod(),
	            requestBody,
	            httpRequest.getRequestURI(),
	            jwtInfo.getIssuer(),
	            jwtInfo.getRoles().stream().collect(Collectors.joining(", ", "[", "]")),
	            jwtInfo.getUserId(),
	            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")
	                    .withZone(ZoneId.systemDefault()).format(jwtInfo.getExp())
	    );

	    // Continuar com a requisição usando a versão cacheada
	    chain.doFilter(cachedRequest, responseWrapper);


	    log.info("Resposta: [Status: {}, Headers: {}, Body {}]",
	            responseWrapper.getStatus(), responseWrapper.getHeaderNames(),responseWrapper.getCapturedBody());


	}

    
    private JwtInfo getInfo(String token, String clientId) {
    	try {

    		return jwtHelper.extractJwtInfo(token, clientId);
    	}catch (Exception e) {
			log.error("Problems to extract info from Token", e);
			return null;
		}
    }
    
   
}
