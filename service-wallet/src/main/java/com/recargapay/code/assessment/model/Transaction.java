package com.recargapay.code.assessment.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import com.recargapay.code.assessment.ports.dtos.BalanceDTO;
import com.recargapay.code.assessment.ports.dtos.TransactionDTO;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(name = "transactions")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Transaction {
	
	@Id
	@GeneratedValue 
    @Column(columnDefinition = "uuid")
	private UUID id;
	@Column(name = "wallet_id")
	private UUID walletId;
	private String type;
	private BigDecimal amount;
	@Column(name="related_wallet_id")
	private UUID relatedWalletId;
	@Column(name = "sequence_no", insertable = false, updatable = false)
	private Long sequence;
	@Column(name="created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;
	@Column(name="balance_after")
	private BigDecimal balanceAtfer;
	@Column(name="currency")
	private String currency;
	
	@PrePersist
	protected void onCreate() {
		this.createdAt = LocalDateTime.now();
	}

	public BalanceDTO toBalanceDTO() {
		var balanceDTO = new BalanceDTO();
		balanceDTO.setBalance(amount);
		balanceDTO.setLastUpdated(createdAt);
		balanceDTO.setCurrency(currency);
		balanceDTO.setIdWallet(walletId);
		return balanceDTO;
		
	}
	
	public TransactionDTO toTransactionDTO() {
		return TransactionDTO.builder()
			   .amount(amount)
			   .balanceAtfer(balanceAtfer)
			   .createdAt(createdAt)
			   .walletId(walletId)
			   .relatedWalletId(relatedWalletId)
			   .build();
	}
	
	
}
