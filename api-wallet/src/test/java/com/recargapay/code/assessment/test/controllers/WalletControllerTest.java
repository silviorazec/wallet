package com.recargapay.code.assessment.test.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.recargapay.code.assessment.controllers.ExceptionController;
import com.recargapay.code.assessment.controllers.WalletController;
import com.recargapay.code.assessment.ports.dtos.BalanceDTO;
import com.recargapay.code.assessment.ports.dtos.WalletDTO;
import com.recargapay.code.assessment.ports.exceptions.ConflictException;
import com.recargapay.code.assessment.ports.exceptions.NotFoundException;
import com.recargapay.code.assessment.ports.exceptions.WalletException;
import com.recargapay.code.assessment.ports.helper.ControllerHelper;
import com.recargapay.code.assessment.ports.helper.JwtHelper;
import com.recargapay.code.assessment.ports.helper.JwtInfo;
import com.recargapay.code.assessment.ports.interfaces.WalletService;
import com.recargapay.code.assessment.test.conf.SecurityConfig;

@ExtendWith(MockitoExtension.class)
@WebMvcTest(controllers = WalletController.class)
@ContextConfiguration(classes = { SecurityConfig.class, WalletController.class, ExceptionController.class,
		ControllerHelper.class })
class WalletControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockBean
	private WalletService walletService;

	@MockBean
	private JwtHelper jwtHelper;

	// Teste de sucesso - create wallet
	@Test
	@WithMockUser(roles = { "API_WALLET:OWNER" })
	void testCreateWalletSuccess() throws Exception {
		WalletDTO walletDTO = WalletDTO.builder().userId(UUID.randomUUID().toString()).build();

		when(walletService.create(walletDTO)).thenReturn(UUID.randomUUID());

		mockMvc.perform(post("/v1/wallets/").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(walletDTO))).andExpect(status().isCreated());
	}

	// Teste de ConflictException ao criar wallet
	@Test
	@WithMockUser(roles = { "API_WALLET:OWNER" })
	void testCreateWalletThrowsConflictException() throws Exception {
		WalletDTO walletDTO = WalletDTO.builder().userId(UUID.randomUUID().toString()).build();

		when(walletService.create(walletDTO)).thenThrow(new ConflictException(""));

		mockMvc.perform(post("/v1/wallets/").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(walletDTO))).andExpect(status().isConflict());
	}

	// Teste de sucesso - getBalanceByDate
	@Test
	@WithMockUser(roles = { "API_WALLET:OWNER" })
	void testGetBalanceByDateSuccess() throws Exception {
		BalanceDTO balanceDTO = BalanceDTO.builder().balance(BigDecimal.TEN).build();
		LocalDate date = LocalDate.now();

		Mockito.when(jwtHelper.extractJwtInfo(any(Authentication.class), any())).thenReturn(JwtInfo.builder().build());
		Mockito.when(walletService.findBalance("user-id", date)).thenReturn(balanceDTO);

		mockMvc.perform(get("/v1/wallets/balance/").param("dateHistory", date.toString())
				.contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk());
	}

	// Teste de WalletException ao buscar saldo
	@Test
	@WithMockUser(roles = { "API_WALLET:OWNER" })
	void testGetBalanceByDateThrowsWalletException() throws Exception {
		LocalDate date = LocalDate.now();

		Mockito.when(jwtHelper.extractJwtInfo(any(Authentication.class), any()))
				.thenReturn(JwtInfo.builder().userId("user-id").build());
		Mockito.when(walletService.findBalance("user-id", date))
				.thenThrow(new WalletException("Error fetching balance", new Exception()));

		mockMvc.perform(get("/v1/wallets/balance/").param("dateHistory", date.toString())
				.contentType(MediaType.APPLICATION_JSON)).andExpect(status().isInternalServerError());
	}

	// Teste de NotFoundException ao buscar saldo
	@Test
	@WithMockUser(roles = { "API_WALLET:OWNER" })
	void testGetBalanceByDateThrowsNotFoundException() throws Exception {
		LocalDate date = LocalDate.now();

		Mockito.when(jwtHelper.extractJwtInfo(any(Authentication.class), any())).
			thenReturn(JwtInfo.builder().userId("user-id").build());
		Mockito.when(walletService.findBalance("user-id", date)).thenThrow(new NotFoundException("Balance not found"));

		mockMvc.perform(get("/v1/wallets/balance/").param("dateHistory", date.toString())
				.contentType(MediaType.APPLICATION_JSON)).andExpect(status().isNotFound());
	}

}
