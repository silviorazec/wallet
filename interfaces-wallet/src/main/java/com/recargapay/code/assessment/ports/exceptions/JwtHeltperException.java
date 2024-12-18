package com.recargapay.code.assessment.ports.exceptions;

public class JwtHeltperException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -423137628110242760L;

	public JwtHeltperException(String msg, Exception e) {
		super(msg, e);
	}

}
