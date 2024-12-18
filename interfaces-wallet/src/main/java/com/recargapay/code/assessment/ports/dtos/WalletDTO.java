package com.recargapay.code.assessment.ports.dtos;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WalletDTO {
	private UUID id;
	private String userId;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
	private String currency;
	
	

}
