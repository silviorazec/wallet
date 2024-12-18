package com.recargapay.code.assessment.test.service;



import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ContextConfiguration;

import com.recargapay.code.assessment.ServiceWalletApplication;
import com.recargapay.code.assessment.model.Transaction;
import com.recargapay.code.assessment.model.Wallet;
import com.recargapay.code.assessment.ports.dtos.TransactionDTO;
import com.recargapay.code.assessment.ports.exceptions.ConflictException;
import com.recargapay.code.assessment.ports.exceptions.NotFoundException;
import com.recargapay.code.assessment.ports.exceptions.WalletException;
import com.recargapay.code.assessment.repositories.TransactionRepository;
import com.recargapay.code.assessment.repositories.WalletRepository;
import com.recargapay.code.assessment.services.DefaultTransactionService;

@ExtendWith(MockitoExtension.class)
@ContextConfiguration(classes = { DefaultTransactionService.class, ServiceWalletApplication.class})
public class DefaultTransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private WalletRepository walletRepository;

    @InjectMocks
    private DefaultTransactionService service;
    

	@Value("${currency}")
	private String currency;


    private Wallet originWallet;
    private Wallet destinationWallet;
    private TransactionDTO transactionDTO;
    private Transaction transacaoOrigem;
    private Transaction transacaoDestino;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new DefaultTransactionService(transactionRepository, walletRepository);
        
        
        var walletOrigemId = UUID.randomUUID();
        originWallet = Wallet.builder().balance(BigDecimal.valueOf(3000))
    			        .currency("BRL")
    			        .id(walletOrigemId)
    			        .userId("user1").build();
    	var walletDestinoId = UUID.randomUUID();
    	
    	destinationWallet = Wallet.builder().balance(BigDecimal.valueOf(1000))
    			        .currency("BRL")
    			        .id(walletDestinoId)
    			        .userId("user2").build();
    	
        transactionDTO =  TransactionDTO.builder()
				  .amount(BigDecimal.valueOf(100))
				  .walletId(originWallet.getId())
				  .relatedWalletId(originWallet.getId())
				  .build();
        
    	 transacaoOrigem = Transaction.builder()
				  .amount(BigDecimal.valueOf(50).negate())
				  .balanceAtfer((originWallet.getBalance().subtract(BigDecimal.valueOf(50))).negate())
				  .currency(currency)
				  .walletId(originWallet.getId())
				  .relatedWalletId(destinationWallet.getId()).build();
         transacaoDestino = Transaction.builder()
					  .amount(BigDecimal.valueOf(50))
					  .balanceAtfer(destinationWallet.getBalance().add(BigDecimal.valueOf(50)))
					  .currency(currency)
					  .walletId(destinationWallet.getId())
					  .relatedWalletId(originWallet.getId()).build();
         
    }

    @Test
    void processTransfer_Success() throws WalletException, NotFoundException, ConflictException {
    	
    	
    	        when(walletRepository.findWalletsByIds(any(), any()))
                .thenReturn(List.of(originWallet, destinationWallet));
               when(transactionRepository.saveAll(any()))
                .thenReturn(List.of(transacaoOrigem,transacaoOrigem));

        
        UUID result = service.processTransfer(transactionDTO);


        assertEquals(BigDecimal.valueOf(1000 + 50), transacaoDestino.getBalanceAtfer());
        assertEquals(BigDecimal.valueOf(3000 - 50).negate(), transacaoOrigem.getBalanceAtfer());
        assertEquals(result, transacaoOrigem.getId());
    }

    @Test
    void processTransfer_walletNoExists_ThrowsConflictException() {
        transactionDTO.setAmount(originWallet.getBalance().subtract(originWallet.getBalance()));
        
        when(walletRepository.findWalletsByIds(any(), any()))
                .thenReturn(List.of(destinationWallet));

        assertThrows(ConflictException.class, 
                     () -> service.processTransfer(transactionDTO));
    }
    
    @Test
    void processTransfer_walletTargetNoExists_ThrowsConflictException() {
        transactionDTO.setAmount(originWallet.getBalance().subtract(originWallet.getBalance()));
        
        when(walletRepository.findWalletsByIds(any(), any()))
                .thenReturn(List.of(originWallet));

        assertThrows(ConflictException.class, 
                     () -> service.processTransfer(transactionDTO));
    }
    
    @Test
    void processTransfer_walletEquals_ThrowsConflictException() {
        transactionDTO.setAmount(originWallet.getBalance().subtract(originWallet.getBalance()));
        destinationWallet.setId(originWallet.getId());
        when(walletRepository.findWalletsByIds(any(), any()))
                .thenReturn(List.of(originWallet, destinationWallet));

        assertThrows(ConflictException.class, 
                     () -> service.processTransfer(transactionDTO));
    }
    
    @Test
    void processTransfer_NotEnoughBalance_ThrowsConflictException() {
        transactionDTO.setAmount(originWallet.getBalance().subtract(originWallet.getBalance()));

        when(walletRepository.findWalletsByIds(any(), any()))
                .thenReturn(List.of(originWallet));

        assertThrows(ConflictException.class, 
                     () -> service.processTransfer(transactionDTO));
    }


    @Test
    void findTransactions_Success() throws NotFoundException, WalletException {
   
    	
        UUID walletId = originWallet.getId();
        
        Transaction transaction = Transaction.builder().amount(BigDecimal.valueOf(100))
				  .walletId(originWallet.getId())
				  .type("DEPOSIT").build();
        
        when(walletRepository.findByUserId(any())).thenReturn(Optional.of(originWallet));
        when(transactionRepository.findByWalletId(walletId))
                .thenReturn(List.of(transaction));

        var transactions = service.findTransactions(originWallet.getUserId());

        assertFalse(transactions.isEmpty());
        verify(transactionRepository).findByWalletId(walletId);
    }

    @Test
    void findTransactions_EmptyList_ThrowsNotFoundException() {
        when(walletRepository.findByUserId(any())).thenReturn(Optional.of(originWallet));
        when(transactionRepository.findByWalletId(any())).thenReturn(List.of());

        assertThrows(NotFoundException.class, () -> service.findTransactions(originWallet.getUserId()));
    }

    @Test
    void deposit_Success() throws WalletException, ConflictException {
    	
    	
    	Wallet wallet = Wallet.builder().id(UUID.randomUUID())
    					.balance(BigDecimal.valueOf(800))
    					.userId(originWallet.getUserId()).build();
    	TransactionDTO dto =  TransactionDTO.builder()
    						  .amount(BigDecimal.valueOf(100))
    						  .walletId(wallet.getId()).build();
    						  
    	
        when(walletRepository.findByUserId(any())).thenReturn(Optional.of(originWallet));
        UUID idTransaction =  UUID.randomUUID();
        when(transactionRepository.save(any(Transaction.class))).thenReturn(Transaction.builder().id(idTransaction).build());

        when(walletRepository.save(any(Wallet.class))).thenReturn(wallet);
        UUID result = service.deposit(dto, originWallet.getUserId());


        assertEquals(idTransaction, result);


    }

    @Test
    void withdraw_Success() throws WalletException, ConflictException {
        when(walletRepository.findByUserId(any())).thenReturn(Optional.of(originWallet));
        when(transactionRepository.save(any())).thenReturn(new Transaction());
        transactionDTO.setAmount(BigDecimal.valueOf(500));
        service.withdraw(transactionDTO, originWallet.getUserId());

        
        assertEquals(BigDecimal.valueOf(3000-500), originWallet.getBalance());
    }

    @Test
    void withdraw_NotEnoughBalance_ThrowsWalletException() {
        transactionDTO.setAmount(BigDecimal.valueOf(5000));
        when(walletRepository.findByUserId(any())).thenReturn(Optional.of(originWallet));

        assertThrows(ConflictException.class, () -> service.withdraw(transactionDTO, originWallet.getUserId()));
    }
    
}
