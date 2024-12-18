package com.recargapay.code.assessment.controllers;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.recargapay.code.assessment.ports.dtos.BalanceDTO;
import com.recargapay.code.assessment.ports.dtos.WalletDTO;
import com.recargapay.code.assessment.ports.exceptions.ConflictException;
import com.recargapay.code.assessment.ports.exceptions.JwtHeltperException;
import com.recargapay.code.assessment.ports.exceptions.NotFoundException;
import com.recargapay.code.assessment.ports.exceptions.WalletException;
import com.recargapay.code.assessment.ports.helper.ControllerHelper;
import com.recargapay.code.assessment.ports.helper.JwtHelper;
import com.recargapay.code.assessment.ports.helper.JwtInfo;
import com.recargapay.code.assessment.ports.interfaces.WalletService;

@RestController
@RequestMapping("v1/wallets")
public class WalletController {

	private WalletService walletService;
	private JwtHelper jwtHelper;
	@Value("${keycloak.resource}")
	private String clientId;
	private ControllerHelper controllerHelper;
	

	public WalletController(WalletService walletService, JwtHelper jwtHelper, ControllerHelper controllerHelper) {
		this.walletService = walletService;
		this.jwtHelper = jwtHelper;
		this.controllerHelper = controllerHelper;
	}

	@PreAuthorize("hasRole('API_WALLET:OWNER')")
	@PostMapping("/")
	public ResponseEntity<Void> create(@RequestBody WalletDTO walletDTO, Authentication authentication)
			throws WalletException, JwtHeltperException, ConflictException {

		walletService.create(walletDTO);
		
		return ResponseEntity.status(HttpStatus.CREATED).build();

	}

	@PreAuthorize("hasRole('API_WALLET:OWNER')")
	@GetMapping("/balance/")
	public ResponseEntity<BalanceDTO> getBalanceByDate(
			@RequestParam(required = false, name = "dateHistory") LocalDate dateHistory,
			Authentication authentication) throws WalletException, NotFoundException, JwtHeltperException {

		JwtInfo jwt = jwtHelper.extractJwtInfo(authentication,clientId);

		return ResponseEntity.ok(walletService.findBalance(jwt.getUserId(), dateHistory));
	}

}
