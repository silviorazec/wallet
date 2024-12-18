package com.recargapay.code.assessment.ports.interfaces;

import java.time.LocalDate;
import java.util.UUID;

import com.recargapay.code.assessment.ports.dtos.BalanceDTO;
import com.recargapay.code.assessment.ports.dtos.WalletDTO;
import com.recargapay.code.assessment.ports.exceptions.ConflictException;
import com.recargapay.code.assessment.ports.exceptions.ForBiddenException;
import com.recargapay.code.assessment.ports.exceptions.NotFoundException;
import com.recargapay.code.assessment.ports.exceptions.WalletException;

public interface WalletService {

	public UUID create(WalletDTO walletDTO) throws WalletException, ConflictException;

	public BalanceDTO findBalance(String userId, LocalDate dateHistory) throws WalletException, NotFoundException;

	public void validateAccessToWallet(UUID idWallet, String userId) throws ForBiddenException, NotFoundException;
	
	public WalletDTO findBalanceByWallateId(UUID idWalUuid) throws WalletException, NotFoundException;

}
