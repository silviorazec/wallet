package com.recargapay.code.assessment.ports.enums;

public enum TransactionType {
	TRANSFER("TRANSFER"), DEPOSIT("DEPOSIT"), WITHDRAW("WITHDRAW");

	private final String value;

	TransactionType(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}
}
