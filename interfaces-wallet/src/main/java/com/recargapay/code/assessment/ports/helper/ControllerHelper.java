package com.recargapay.code.assessment.ports.helper;

import java.net.URI;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@Component
public class ControllerHelper {

	
	public URI createLocation(String placeholder, Object value) {
		return ServletUriComponentsBuilder.fromCurrentRequest().path(placeholder)
				.buildAndExpand(value).toUri();
	}
}
