package com.recargapay.code.assessment.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import com.recargapay.code.assessment.ports.dtos.BalanceDTO;
import com.recargapay.code.assessment.ports.dtos.WalletDTO;

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
@Table(name = "wallets")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Wallet {

	@Id
	@GeneratedValue
	@Column(columnDefinition = "uuid")
	private UUID id;
	@Column(columnDefinition = "user_id")
	private String userId;
	private BigDecimal balance;
	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;
	@Column(name = "updated_at", nullable = false, updatable = false)
	private LocalDateTime updatedAt;
	private String currency;

	@PrePersist
	protected void onCreate() {
		this.createdAt = LocalDateTime.now();
		this.updatedAt = LocalDateTime.now();
	}

	public BalanceDTO toBalanceDTO() {
		var dto = new BalanceDTO();
		dto.setBalance(this.balance);
		dto.setLastUpdated(updatedAt);
		dto.setCurrency(currency);
		dto.setIdWallet(id);
		return dto;

	}
	
	public WalletDTO toWalaDto() {
		var dto = new WalletDTO();
		dto.setCreatedAt(updatedAt);
		dto.setCurrency(currency);
		dto.setId(id);
		dto.setUserId(userId);
		return dto;

	}
	
	public static Wallet createFromWalletDTO(WalletDTO walletDTO) {
		return Wallet.builder()
		.userId(walletDTO.getUserId()).build();
	
	}
}
