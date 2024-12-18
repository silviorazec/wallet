package com.recargapay.code.assessment.controllers;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.recargapay.code.assessment.ports.dtos.TransactionDTO;
import com.recargapay.code.assessment.ports.enums.TransactionType;
import com.recargapay.code.assessment.ports.exceptions.ConflictException;
import com.recargapay.code.assessment.ports.exceptions.NotFoundException;
import com.recargapay.code.assessment.ports.exceptions.WalletException;
import com.recargapay.code.assessment.ports.helper.JwtHelper;
import com.recargapay.code.assessment.ports.interfaces.MessageBrokerTransactionService;
import com.recargapay.code.assessment.ports.interfaces.TransactionService;

@RestController
@RequestMapping("v1/transactions")
public class TransactionController {

	private TransactionService transactionService;
	private JwtHelper jwtHelper;
	private MessageBrokerTransactionService messageBrokerTransactionService;

	public TransactionController(TransactionService transactionService, JwtHelper jwtHelper,
			MessageBrokerTransactionService messageBrokerTransactionService) {
		this.transactionService = transactionService;
		this.jwtHelper = jwtHelper;
		this.messageBrokerTransactionService = messageBrokerTransactionService;
	}

	@PreAuthorize("hasRole('API_WALLET:OWNER')")
	@GetMapping("/")
	public ResponseEntity<List<TransactionDTO>> getTransfer(Authentication authentication)
			throws NotFoundException, WalletException {

		return ResponseEntity.ok(transactionService.findTransactions(jwtHelper.getUserId(authentication)));
	}

	@PreAuthorize("hasRole('API_WALLET:OWNER')")
	@PostMapping("/transfers")
	public ResponseEntity<Void> makeTransfer(@RequestBody TransactionDTO transactionDTO, Authentication authentication)
			throws  WalletException {
		executeTransaction(transactionDTO, TransactionType.TRANSFER, authentication);
		return ResponseEntity.accepted().build();
	}

	@PreAuthorize("hasRole('API_WALLET:OWNER')")
	@PostMapping("/deposits")
	public ResponseEntity<Void> makeDeposit(@RequestBody TransactionDTO transactionDTO, Authentication authentication)
			throws WalletException {
		System.out.println("VALOR " + transactionDTO.getAmount());
		executeTransaction(transactionDTO, TransactionType.DEPOSIT, authentication);
		return ResponseEntity.accepted().build();
	}

	@PreAuthorize("hasRole('API_WALLET:OWNER')")
	@PostMapping("/withdraws")
	public ResponseEntity<Void> makeWithdraw(@RequestBody TransactionDTO transactionDTO, Authentication authentication)
			throws WalletException {

		executeTransaction(transactionDTO, TransactionType.WITHDRAW, authentication);
		return ResponseEntity.accepted().build();

	}

	
	private void executeTransaction(TransactionDTO transactionDTO, TransactionType transactionType,
			Authentication authentication) throws WalletException {
		transactionDTO.setCreatedAt(LocalDateTime.now());
		messageBrokerTransactionService.publishTransaction(transactionDTO.toTopic(transactionType),
				jwtHelper.getJwt(authentication));
	}

}
