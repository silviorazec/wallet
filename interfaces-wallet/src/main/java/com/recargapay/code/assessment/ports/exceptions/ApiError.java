package com.recargapay.code.assessment.ports.exceptions;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class ApiError {

	private int code;
	private String msg;
	private String path;
	private LocalDateTime timestamp;
}
