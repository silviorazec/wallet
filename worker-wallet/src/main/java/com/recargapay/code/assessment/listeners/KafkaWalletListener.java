package com.recargapay.code.assessment.listeners;

import java.time.Instant;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Headers;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import com.recargapay.code.assessment.config.TriConsumer;
import com.recargapay.code.assessment.ports.dtos.TransactionDTO;
import com.recargapay.code.assessment.ports.dtos.WalletDTO;
import com.recargapay.code.assessment.ports.enums.TransactionType;
import com.recargapay.code.assessment.ports.exceptions.ConflictException;
import com.recargapay.code.assessment.ports.exceptions.NotFoundException;
import com.recargapay.code.assessment.ports.exceptions.WalletException;
import com.recargapay.code.assessment.ports.helper.JwtHelper;
import com.recargapay.code.assessment.ports.helper.JwtPropertiesValidator;
import com.recargapay.code.assessment.ports.helper.TokenValidator;
import com.recargapay.code.assessment.ports.interfaces.MessageBrokerTransactionService;
import com.recargapay.code.assessment.ports.interfaces.TransactionService;
import com.recargapay.code.assessment.ports.interfaces.WalletService;
import com.recargapay.code.assessment.topics.Notification;
import com.recargapay.code.assessment.topics.Transaction;

import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
public class KafkaWalletListener {

	private final TokenValidator tokenValidator;

	@Value("${keycloak.url.realm}")
	private String urlIssuer;

	@Value("${client.id}")
	private String clientId;

	private String jwkJson;
	private JwtHelper jwtHelper;

	private TransactionService transactionService;
	private WalletService walletService;
	private MessageBrokerTransactionService messageBrokerTransactionService;
	
	

	private final Map<TransactionType, TriConsumer<TransactionDTO, String, String>> transactionHandlers = new EnumMap<>(
			TransactionType.class);

	private final static String ROLE_ALLOWED = "API_WALLET:OWNER";

	public KafkaWalletListener(TokenValidator tokenValidator, @Qualifier("chaveIDP") String jwkJson, WalletService walletService,
			TransactionService transactionService, JwtHelper jwtHelper,
			MessageBrokerTransactionService  messageBrokerTransactionService) {
		this.tokenValidator = tokenValidator;
		this.jwkJson = jwkJson;
		this.transactionService = transactionService;
		this.walletService = walletService;
		this.jwtHelper = jwtHelper;
		this.messageBrokerTransactionService = messageBrokerTransactionService;

		transactionHandlers.put(TransactionType.TRANSFER, this::processTransfer);
		transactionHandlers.put(TransactionType.DEPOSIT, this::processDeposit);
		transactionHandlers.put(TransactionType.WITHDRAW, this::processWithdraw);
	}

	@KafkaListener(topics = "transaction", groupId = "${spring.kafka.consumer.group-id}")
	public void listen(ConsumerRecord<String, Transaction> record, @Payload Object payload) {
		try {
			Headers headers = record.headers();

			String jwtToken = new String(headers.lastHeader("Authorization").value());

			jwtToken = jwtToken.replace("Bearer ", "");

			var jwtPropertiesValidator = createJWtJwtPropertiesValidator();
			jwtPropertiesValidator.setExpiration(Instant.ofEpochMilli(record.timestamp()));

			tokenValidator.validate(jwtPropertiesValidator, jwtToken);

			var userIdOrigin = jwtHelper.getClaim("sub", jwtToken).toString();
			var transactionDTO = TransactionDTO.createByTopic(record.value());
			
			var walletOrigem = walletService.findBalance(userIdOrigin, null);
			transactionDTO.setWalletId(walletOrigem.getIdWallet());
			String userDestinationId = null;
			if(transactionDTO.getRelatedWalletId() != null) {
				userDestinationId  = walletService.findBalanceByWallateId(transactionDTO.getWalletId()).getUserId();
			}
			
			processTransaction(transactionDTO, userIdOrigin, userDestinationId);
			var notification = Notification.newBuilder()
							   .setTo(userIdOrigin)
							   .setMessage("Sucess transaction, see your balance.").build();
			messageBrokerTransactionService.notify(notification, jwtToken);
		} catch (Exception e) {
			log.error("It isn't possible to process this message");
			log.error(e, e);
		}
	}

	private JwtPropertiesValidator createJWtJwtPropertiesValidator() {
		return JwtPropertiesValidator.builder().clientId(clientId).issuer(urlIssuer).jkws(jwkJson)
				.rollesAllowed(List.of(KafkaWalletListener.ROLE_ALLOWED)).build();
	}

	private void processTransaction(TransactionDTO transactionDTO, String userIdOrigin, String userIdDestination) throws WalletException {
		TriConsumer<TransactionDTO, String, String> handler = transactionHandlers
				.get(TransactionType.valueOf(transactionDTO.getType()));
			
	
	        try {
	            handler.accept(transactionDTO, userIdOrigin, userIdDestination);
	        } catch (Exception e) {
	            throw new WalletException("Erro ao processar transação do tipo " + transactionDTO.getType(), e);
	        }
	}

	private void processTransfer(TransactionDTO transactionDTO, String userIdOrigin, String userIdDestination)
			throws NotFoundException, ConflictException, WalletException {
		transactionService.processTransfer(transactionDTO);
	}

	private void processDeposit(TransactionDTO transactionDTO, String userId, String unused) throws WalletException, ConflictException {
		transactionService.deposit(transactionDTO, userId);
	}

	private void processWithdraw(TransactionDTO transactionDTO, String userId, String unused) throws WalletException, ConflictException {
		transactionService.withdraw(transactionDTO, userId);
	}
}
