package com.recargapay.code.assessment.ports.interfaces;

import com.recargapay.code.assessment.ports.exceptions.WalletException;
import com.recargapay.code.assessment.topics.Notification;
import com.recargapay.code.assessment.topics.Transaction;

public interface MessageBrokerTransactionService {



	public void publishTransaction(Transaction transaction, String jwtToken) throws WalletException;
	
	public void notify(Notification notification, String jwtToken) throws WalletException;

}

