package com.recargapay.code.assessment.ports.exceptions;

public class WalletException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8170626096736095962L;
	
	public WalletException(String msg, Exception exception) {
		super(msg, exception);
	}



}
