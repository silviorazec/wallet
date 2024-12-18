package com.recargapay.code.assessment.services;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.recargapay.code.assessment.model.Wallet;
import com.recargapay.code.assessment.ports.dtos.BalanceDTO;
import com.recargapay.code.assessment.ports.dtos.WalletDTO;
import com.recargapay.code.assessment.ports.exceptions.ConflictException;
import com.recargapay.code.assessment.ports.exceptions.ForBiddenException;
import com.recargapay.code.assessment.ports.exceptions.NotFoundException;
import com.recargapay.code.assessment.ports.exceptions.WalletException;
import com.recargapay.code.assessment.ports.interfaces.WalletService;
import com.recargapay.code.assessment.repositories.TransactionRepository;
import com.recargapay.code.assessment.repositories.WalletRepository;

@Service
public class DefaultWalletService implements WalletService {

	@Value("${currency}")
	private String currency;

	private WalletRepository walletRepository;
	private TransactionRepository transactionRepository;

	public DefaultWalletService(WalletRepository walletRepository, TransactionRepository transactionRepository) {
		this.walletRepository = walletRepository;
		this.transactionRepository = transactionRepository;
	}

	public UUID create(WalletDTO walletDTO) throws WalletException, ConflictException {

		try {
			var wallet = Wallet.createFromWalletDTO(walletDTO);
			wallet.setCurrency(currency);
			if (walletRepository.findByUserId(walletDTO.getUserId()).isPresent()) {
				throw new ConflictException("This user already has an wallet.");
			} else {
				return walletRepository.save(wallet).getId();
			}
		} catch (ConflictException e) {
			throw e;
		} catch (Exception e) {
			throw new WalletException(
					String.format("Failed to create wallet for user %s: %s", walletDTO.getUserId(), e.getMessage()), e);
		}

	}

	public BalanceDTO findBalance(String userId, LocalDate dateHistory) throws WalletException, NotFoundException {

		try {
			var wallet = walletRepository.findByUserId(userId)
					.orElseThrow(() -> new NotFoundException("This user don't has wallet : " + userId));
			if (dateHistory == null) {
				return wallet.toBalanceDTO();
			} else {
				

				LocalDateTime startOfDay = dateHistory.atStartOfDay(); 
				LocalDateTime endOfDay = dateHistory.atTime(23, 59, 59); 
				var transaction = transactionRepository.findTransactionsByDateAndWalletId
							(startOfDay, endOfDay,wallet.getId())
						.orElseThrow(() -> new NotFoundException(
								"Wallet not found with id: " + wallet.getId() + " and date " + dateHistory));
				return transaction.toBalanceDTO();
			}	

		} catch (NotFoundException e) {
			throw e;
		} catch (Exception e) {
			throw new WalletException(String.format("Failed to find the balance of wallet"), e);
		}

	}

	public void validateAccessToWallet(UUID idWallet, String userId) throws ForBiddenException, NotFoundException {

		var wallet = walletRepository.findById(idWallet)
				.orElseThrow(() -> new NotFoundException("Wallet not found with id: " + idWallet));
		if (!wallet.getUserId().equals(userId)) {
			throw new ForBiddenException("You can't haves access to this wallet");
		}

	}

	public WalletDTO findBalanceByWallateId(UUID idWallate) throws WalletException, NotFoundException {
		try {
			var wallate = walletRepository.findById(idWallate);
			if (wallate.isPresent()) {
				return wallate.get().toWalaDto();
			} else {
				throw new NotFoundException("This wallate don't exists");
			}

		} catch (NotFoundException e) {
			throw e;
		} catch (Exception e) {
			throw new WalletException("Problems to find this wallate:" + idWallate, e);
		}
	}

}
