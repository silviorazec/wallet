package com.recargapay.code.assessment.ports.interfaces;

import java.util.List;
import java.util.UUID;

import com.recargapay.code.assessment.ports.dtos.TransactionDTO;
import com.recargapay.code.assessment.ports.exceptions.ConflictException;
import com.recargapay.code.assessment.ports.exceptions.NotFoundException;
import com.recargapay.code.assessment.ports.exceptions.WalletException;

public interface TransactionService {

	public UUID processTransfer(TransactionDTO transactionDTO)
				throws NotFoundException, ConflictException, WalletException;	

	public List<TransactionDTO> findTransactions(String userId)
			throws  NotFoundException, WalletException;

	public UUID deposit(TransactionDTO transactionDTO, String userId) throws WalletException, ConflictException;

	public UUID withdraw(TransactionDTO transactionDTO, String userId) throws WalletException, ConflictException;
	

}
