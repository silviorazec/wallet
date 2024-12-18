package com.recargapay.code.assessment.ports.dtos;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class BalanceDTO {
	
	private UUID idWallet;
    private BigDecimal balance; 
    private String currency;
    private LocalDateTime lastUpdated; 
    

}
