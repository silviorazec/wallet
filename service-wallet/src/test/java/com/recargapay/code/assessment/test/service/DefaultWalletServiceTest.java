package com.recargapay.code.assessment.test.service;


import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ContextConfiguration;

import com.recargapay.code.assessment.ServiceWalletApplication;
import com.recargapay.code.assessment.model.Transaction;
import com.recargapay.code.assessment.model.Wallet;
import com.recargapay.code.assessment.ports.dtos.BalanceDTO;
import com.recargapay.code.assessment.ports.dtos.WalletDTO;
import com.recargapay.code.assessment.ports.exceptions.ConflictException;
import com.recargapay.code.assessment.ports.exceptions.ForBiddenException;
import com.recargapay.code.assessment.ports.exceptions.NotFoundException;
import com.recargapay.code.assessment.ports.exceptions.WalletException;
import com.recargapay.code.assessment.repositories.TransactionRepository;
import com.recargapay.code.assessment.repositories.WalletRepository;
import com.recargapay.code.assessment.services.DefaultWalletService;

@ExtendWith(MockitoExtension.class)
@ContextConfiguration(classes = { DefaultWalletService.class, ServiceWalletApplication.class})
public class DefaultWalletServiceTest {

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private DefaultWalletService walletService;

    private WalletDTO walletDTO;
    private Wallet wallet;
    private UUID walletId;
    
    private  Transaction transaction;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        walletId = UUID.randomUUID();
        walletDTO = new WalletDTO();
        walletDTO.setUserId("user123");

        wallet = Wallet.createFromWalletDTO(walletDTO);
        wallet.setId(walletId);
        wallet.setCurrency("BRL");
        
        transaction = Transaction.builder()
        						  .amount(BigDecimal.TEN)
        						  .balanceAtfer(BigDecimal.valueOf(20))
        						  .currency("BRL")
        						  .relatedWalletId(UUID.randomUUID())
        						  .walletId(walletId).build();
        				
    }

    @Test
    void testCreateWallet_Success() throws WalletException, ConflictException {
        when(walletRepository.findByUserId(walletDTO.getUserId())).thenReturn(Optional.empty());
        when(walletRepository.save(any(Wallet.class))).thenReturn(wallet);

        UUID result = walletService.create(walletDTO);

        assertNotNull(result);
        assertEquals(walletId, result);
        verify(walletRepository, times(1)).save(any(Wallet.class));
    }

    @Test
    void testCreateWallet_ConflictException() {
        when(walletRepository.findByUserId(walletDTO.getUserId())).thenReturn(Optional.of(wallet));

        ConflictException exception = assertThrows(ConflictException.class, () -> walletService.create(walletDTO));
        assertEquals("This user already has an wallet.", exception.getMessage());
    }

    @Test
    void testCreateWallet_WalletException() {
        when(walletRepository.save(any(Wallet.class))).thenThrow(RuntimeException.class);

        WalletException exception = assertThrows(WalletException.class, () -> walletService.create(walletDTO));
        assertTrue(exception.getMessage().contains("Failed to create wallet"));
    }

    @Test
    void testFindBalance_NoDate_Success() throws WalletException, NotFoundException {
        when(walletRepository.findByUserId(walletDTO.getUserId())).thenReturn(Optional.of(wallet));

        BalanceDTO balanceDTO = walletService.findBalance(walletDTO.getUserId(), null);

        assertNotNull(balanceDTO);
        verify(walletRepository, times(1)).findByUserId(walletDTO.getUserId());
    }

    @Test
    void testFindBalance_WithDate_Success() throws WalletException, NotFoundException {
    	
    	var dateNow = LocalDate.now();
        when(walletRepository.findByUserId(walletDTO.getUserId())).thenReturn(Optional.of(wallet));
        when(transactionRepository.findTransactionsByDateAndWalletId(any(LocalDateTime.class),any(LocalDateTime.class),any(UUID.class))).thenReturn(Optional.of(transaction));

        BalanceDTO balanceDTO = walletService.findBalance(walletDTO.getUserId(), dateNow);

        assertNotNull(balanceDTO);
      
    }



    @Test
    void testValidateAccessToWallet_Success() throws NotFoundException, ForBiddenException {
        when(walletRepository.findById(walletId)).thenReturn(Optional.of(wallet));

        assertDoesNotThrow(() -> walletService.validateAccessToWallet(walletId, walletDTO.getUserId()));
    }

    @Test
    void testValidateAccessToWallet_ForBiddenException() {
        wallet.setUserId("anotherUser");
        when(walletRepository.findById(walletId)).thenReturn(Optional.of(wallet));

        ForBiddenException exception = assertThrows(ForBiddenException.class,
                () -> walletService.validateAccessToWallet(walletId, walletDTO.getUserId()));
        assertEquals("You can't haves access to this wallet", exception.getMessage());
    }

    @Test
    void testValidateAccessToWallet_NotFoundException() {
        when(walletRepository.findById(walletId)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> walletService.validateAccessToWallet(walletId, walletDTO.getUserId()));
        assertEquals("Wallet not found with id: " + walletId, exception.getMessage());
    }

    @Test
    void testFindBalanceByWalletId_Success() throws WalletException, NotFoundException {
        when(walletRepository.findById(walletId)).thenReturn(Optional.of(wallet));

        WalletDTO result = walletService.findBalanceByWallateId(walletId);

        assertNotNull(result);
        verify(walletRepository, times(1)).findById(walletId);
    }

    @Test
    void testFindBalanceByWalletId_NotFoundException() {
        when(walletRepository.findById(walletId)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> walletService.findBalanceByWallateId(walletId));
        assertEquals("This wallate don't exists", exception.getMessage());
    }

    @Test
    void testFindBalanceByWalletId_WalletException() {
        when(walletRepository.findById(walletId)).thenThrow(RuntimeException.class);

        WalletException exception = assertThrows(WalletException.class,
                () -> walletService.findBalanceByWallateId(walletId));
        assertTrue(exception.getMessage().contains("Problems to find this wallate"));
    }
}
