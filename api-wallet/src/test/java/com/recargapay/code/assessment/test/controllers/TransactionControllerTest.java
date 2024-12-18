package com.recargapay.code.assessment.test.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.recargapay.code.assessment.controllers.ExceptionController;
import com.recargapay.code.assessment.controllers.TransactionController;
import com.recargapay.code.assessment.ports.dtos.TransactionDTO;
import com.recargapay.code.assessment.ports.exceptions.NotFoundException;
import com.recargapay.code.assessment.ports.exceptions.WalletException;
import com.recargapay.code.assessment.ports.helper.JwtHelper;
import com.recargapay.code.assessment.ports.interfaces.MessageBrokerTransactionService;
import com.recargapay.code.assessment.ports.interfaces.TransactionService;
import com.recargapay.code.assessment.test.conf.SecurityConfig;

@ExtendWith(MockitoExtension.class)
@WebMvcTest(controllers = TransactionController.class)
@ContextConfiguration(classes = { SecurityConfig.class, TransactionController.class, ExceptionController.class })
class TransactionControllerTest {

	@Autowired
	private ObjectMapper objectMapper;

	@MockBean
	private TransactionService transactionService;

	@MockBean
	private JwtHelper jwtHelper;

	@MockitoBean
	private MessageBrokerTransactionService messageBrokerTransactionService;

	@Autowired
	private MockMvc mockMvc;

	// Teste de sucesso
	@Test
	@WithMockUser(roles = { "API_WALLET:OWNER" }, username = "teste_user")
	void testGetTransfer() throws Exception {

		String userId = UUID.randomUUID().toString();
		TransactionDTO transaction = TransactionDTO.builder().walletId(UUID.randomUUID()).amount(null)
				.createdAt(LocalDateTime.now()).build();
		List<TransactionDTO> transactions = Collections.singletonList(transaction);

		Mockito.when(jwtHelper.getUserId(any())).thenReturn(userId);
		Mockito.when(transactionService.findTransactions(userId)).thenReturn(transactions);

		mockMvc.perform(get("/v1/transactions/")).andExpect(status().isOk());

	}

	// Simulação de NotFoundException
	@Test
	@WithMockUser(roles = { "API_WALLET:OWNER" }, username = "teste_user")
	void testGetTransferThrowsNotFoundException() throws Exception {
		// Mock do JwtHelper
		Mockito.when(jwtHelper.getUserId(Mockito.any())).thenReturn("mockUserId");

		Mockito.when(transactionService.findTransactions("mockUserId"))
				.thenThrow(new NotFoundException("Erro simulado"));

		mockMvc.perform(get("/v1/transactions/")).andExpect(status().isNotFound());
	}

	// Teste de sucesso - makeTransfer
	@Test
	@WithMockUser(roles = { "API_WALLET:OWNER" }, username = "teste_user")
	void testMakeTransfer() throws Exception {
		TransactionDTO transactionDTO = TransactionDTO.builder().createdAt(LocalDateTime.now()).amount(BigDecimal.TEN)
				.walletId(UUID.randomUUID())
				.build();

		Mockito.doNothing().when(messageBrokerTransactionService).publishTransaction(any(), any());
		Mockito.when(jwtHelper.getUserId(Mockito.any())).thenReturn("mockUserId");

		mockMvc.perform(post("/v1/transactions/transfers").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(transactionDTO))).andExpect(status().isAccepted());

	}

	@Test
	@WithMockUser(roles = { "API_WALLET:OWNER" })
	void testMakeTransferThrowsConflictException() throws Exception {
		TransactionDTO transactionDTO = TransactionDTO.builder().amount(BigDecimal.TEN)
				.walletId(UUID.randomUUID())
				.build();

		Mockito.doThrow(new WalletException("Erro occurred", null)).when(messageBrokerTransactionService)
				.publishTransaction(any(), any());

		mockMvc.perform(post("/v1/transactions/transfers").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(transactionDTO))).andExpect(status().isInternalServerError());
	}

	@Test
	@WithMockUser(roles = { "API_WALLET:OWNER" }, username = "teste_user")
	void testMakeDeposit() throws Exception {

		TransactionDTO transactionDTO = TransactionDTO.builder().createdAt(LocalDateTime.now()).amount(BigDecimal.TEN)
				.walletId(UUID.randomUUID())
				.build();

		Mockito.doNothing().when(messageBrokerTransactionService).publishTransaction(any(), any());
		Mockito.when(jwtHelper.getUserId(Mockito.any())).thenReturn("mockUserId");

		mockMvc.perform(post("/v1/transactions/deposits").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(transactionDTO))).andExpect(status().isAccepted());
	}
	
	@Test
	@WithMockUser(roles = { "API_WALLET:OWNER" }, username = "teste_user")
	void testMakeDepositJson() throws Exception {



		Mockito.doNothing().when(messageBrokerTransactionService).publishTransaction(any(), any());
		Mockito.when(jwtHelper.getUserId(Mockito.any())).thenReturn("mockUserId");

		mockMvc.perform(post("/v1/transactions/deposits").contentType(MediaType.APPLICATION_JSON)
				.content("{\"amount\":450.00}}")).andExpect(status().isAccepted());
	}

	@Test
	@WithMockUser(roles = { "API_WALLET:OWNER" })
	void testMakeDepositThrowsWalletException() throws Exception {
		TransactionDTO transactionDTO = TransactionDTO.builder().createdAt(LocalDateTime.now()).amount(BigDecimal.TEN)
				.walletId(UUID.randomUUID())
				.build();

		Mockito.doThrow(new WalletException("Wallet error", null)).when(messageBrokerTransactionService)
				.publishTransaction(any(), any());

		mockMvc.perform(post("/v1/transactions/deposits").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(transactionDTO))).andExpect(status().isInternalServerError());
	}

	@Test
	@WithMockUser(roles = { "API_WALLET:OWNER" })
	void testMakeWithdraw() throws Exception {
		TransactionDTO transactionDTO = TransactionDTO.builder().createdAt(LocalDateTime.now()).amount(BigDecimal.TEN)
				.walletId(UUID.randomUUID())
				.build();

		Mockito.doNothing().when(messageBrokerTransactionService).publishTransaction(any(), any());
		Mockito.when(jwtHelper.getUserId(Mockito.any())).thenReturn("mockUserId");

		mockMvc.perform(post("/v1/transactions/withdraws").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(transactionDTO))).andExpect(status().isAccepted());
	}
	
	
	// Simulação de WalletException - makeWithdraw
	@Test
	@WithMockUser(roles = { "API_WALLET:OWNER" })
	void testMakeWithdrawThrowsWalletException() throws Exception {
		TransactionDTO transactionDTO = TransactionDTO.builder().createdAt(LocalDateTime.now()).amount(BigDecimal.TEN)
				.walletId(UUID.randomUUID())
				.build();

		Mockito.doThrow(new WalletException("Wallet error", null)).when(messageBrokerTransactionService)
				.publishTransaction(any(), any());

		mockMvc.perform(post("/v1/transactions/withdraws").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(transactionDTO))).andExpect(status().isInternalServerError());
	}

}
