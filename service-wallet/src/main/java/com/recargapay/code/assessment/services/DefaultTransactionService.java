package com.recargapay.code.assessment.services;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.recargapay.code.assessment.model.Transaction;
import com.recargapay.code.assessment.model.Wallet;
import com.recargapay.code.assessment.ports.dtos.TransactionDTO;
import com.recargapay.code.assessment.ports.enums.TransactionType;
import com.recargapay.code.assessment.ports.exceptions.ConflictException;
import com.recargapay.code.assessment.ports.exceptions.NotFoundException;
import com.recargapay.code.assessment.ports.exceptions.WalletException;
import com.recargapay.code.assessment.ports.interfaces.TransactionService;
import com.recargapay.code.assessment.repositories.TransactionRepository;
import com.recargapay.code.assessment.repositories.WalletRepository;

@Service
public class DefaultTransactionService implements TransactionService {

	@Value("${currency}")
	private String currency;

	private TransactionRepository transactionRepository;
	private WalletRepository walletRepository;

	public DefaultTransactionService(TransactionRepository transactionRepository, WalletRepository walletRepository) {
		this.transactionRepository = transactionRepository;
		this.walletRepository = walletRepository;
	}

	@Transactional
	public UUID processTransfer(TransactionDTO transactionDTO)
			throws NotFoundException, ConflictException, WalletException {

		try {
			List<Wallet> wallets = walletRepository.findWalletsByIds(transactionDTO.getWalletId(),
					transactionDTO.getRelatedWalletId());

			validateTransaction(transactionDTO, wallets);

			var originWallet = wallets.stream().filter(w -> w.getId().equals(transactionDTO.getWalletId())).findFirst()
					.get();
			var destinationWallet = wallets.stream().filter(w -> w.getId().equals(transactionDTO.getRelatedWalletId()))
					.findFirst().get();


			BigDecimal balacentOrigin = originWallet.getBalance().subtract(transactionDTO.getAmount());
			BigDecimal balanceDestination = destinationWallet.getBalance().add(transactionDTO.getAmount());

			
			originWallet.setBalance(balacentOrigin);
			destinationWallet.setBalance(balanceDestination);
			var negativeTransaction = createNegativeTransaction(transactionDTO, originWallet.getId());
			var positiveTransaction = createPositiveTransaction(transactionDTO, destinationWallet.getId());

			negativeTransaction.setBalanceAtfer(balacentOrigin);
			positiveTransaction.setBalanceAtfer(balanceDestination);

		
			walletRepository.save(originWallet);
			walletRepository.save(destinationWallet);
			List<Transaction> transactions = transactionRepository
					.saveAll(List.of(negativeTransaction, positiveTransaction));

			return transactions.stream().filter(t -> t.getWalletId().equals(originWallet.getId())).findFirst().get()
					.getId();
		} catch (NotFoundException e) {
			throw e;
		} catch (ConflictException e) {
			throw e;
		} catch (Exception e) {
			throw new WalletException("Problems to make a transfer ", e);
		}
	}

	public List<TransactionDTO> findTransactions(String userId) throws NotFoundException, WalletException {

		try {

			var wallet = walletRepository.findByUserId(userId);
			var transacations = transactionRepository.findByWalletId(wallet.get().getId());

			if (transacations.isEmpty()) {
				throw new NotFoundException("Transactions  not found ");
			}
			return transacations.stream().map(Transaction::toTransactionDTO).collect(Collectors.toList());

		} catch (NotFoundException e) {
			throw e;
		} catch (Exception e) {
			throw new WalletException("Problems to find a transaction", e);
		}
	}

	public UUID deposit(TransactionDTO transactionDTO, String userId) throws WalletException, ConflictException {

		try {
			var walletOp = walletRepository.findByUserId(userId);
			if(walletOp.isPresent()) {
				var wallet = walletOp.get();
				wallet.setBalance(wallet.getBalance().add(transactionDTO.getAmount()));
		
					var transaction = Transaction.builder()
							.amount( transactionDTO.getAmount())
							.currency(currency).type(TransactionType.DEPOSIT.getValue()).balanceAtfer(wallet.getBalance())
							.walletId(wallet.getId()).build();
		
					walletRepository.save(wallet);
					transaction = transactionRepository.save(transaction);
					return transaction.getId();
			}else {
				throw new ConflictException("This user has not a wallet userID: " + userId);
			}
		}catch (ConflictException e) {
			throw e;
		}catch (Exception e) {
			throw new WalletException("Problems to make a deposit",e);
		}
	}

	public UUID withdraw(TransactionDTO transactionDTO, String userId) throws WalletException, ConflictException {

		try {
			var walletOp = walletRepository.findByUserId(userId);
			if(walletOp.isEmpty()) {
				throw new ConflictException("This user has not a wallet userID: " + userId);
			}
			var wallet = walletOp.get();
			wallet.setBalance(wallet.getBalance().subtract(transactionDTO.getAmount()));
			
			if(hasSufficientFunds(wallet,transactionDTO) ) {
				throw new ConflictException("There is not enough balance for this operation");
			}else {
				var transaction = Transaction.builder()
						.amount(transactionDTO.getAmount().negate())
						.currency(currency).type(TransactionType.WITHDRAW.getValue())
						.balanceAtfer(wallet.getBalance())
						.walletId(wallet.getId()).build();
	
				walletRepository.save(wallet);
				transaction = transactionRepository.save(transaction);
				return transaction.getId();
				
			}
		}catch (ConflictException e) {
				throw e;
			
		} catch (Exception e) {
			throw new WalletException("Problems to make the transaction", e);
		}
	}


	
	private boolean hasSufficientFunds(Wallet wallet, TransactionDTO transactionDTO){
		return  wallet.getBalance().compareTo(transactionDTO.getAmount()) == -1;
	}

	private void validateTransaction(TransactionDTO transaction, List<Wallet> wallets)
			throws NotFoundException, ConflictException {

		if (transaction.getAmount().signum() == 0 || transaction.getAmount().signum() == -1) {
			throw new ConflictException("Negative or 0(zero) values. This operation is not allowed.");
		}

		var originWallet = wallets.stream().filter(w -> w.getId().equals(transaction.getWalletId())).findFirst()
				.orElseThrow(() -> new NotFoundException("Origin wallet not found"));
		wallets.stream().filter(w -> w.getId().equals(transaction.getRelatedWalletId())).findFirst()
				.orElseThrow(() -> new NotFoundException("Destination wallet not found"));

		if (originWallet.getBalance().compareTo(transaction.getAmount()) == -1) {
			throw new ConflictException("There is not enough balance for this operation");
		}

	}

	private Transaction createNegativeTransaction(TransactionDTO transactionDTO, UUID walletId) {
		return Transaction.builder().amount(transactionDTO.getAmount().negate()).currency(currency)
				.relatedWalletId(transactionDTO.getRelatedWalletId()).type(TransactionType.TRANSFER.getValue())
				.walletId(walletId).build();
	}

	private Transaction createPositiveTransaction(TransactionDTO transactionDTO, UUID walletId) {
		return Transaction.builder().amount(transactionDTO.getAmount()).currency(currency).relatedWalletId(transactionDTO.getWalletId())
				.type(TransactionType.TRANSFER.getValue()).walletId(walletId).build();
	}

}
