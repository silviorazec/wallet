package com.recargapay.code.assessment.ports.dtos;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.recargapay.code.assessment.ports.enums.TransactionType;
import com.recargapay.code.assessment.topics.Transaction;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TransactionDTO {

	private UUID walletId;
	private BigDecimal amount;
	private UUID relatedWalletId;
	private LocalDateTime createdAt;
	private BigDecimal balanceAtfer;
	private String type;

	public static TransactionDTO createByTopic(Transaction transaction) {
		return TransactionDTO.builder().amount(byteBufferToBigDecimal(transaction.getAmount()))
				.relatedWalletId(transaction.getRelatedWalletId()).type(transaction.getTypeTransaction().toString())
				.walletId(transaction.getWalletId())
				.build();

	}

	public Transaction toTopic(TransactionType transactionType) {
		return Transaction.newBuilder().setAmount(amount != null ? bigDecimalToByteBuffer(amount) : null)
				.setRelatedWalletId(relatedWalletId != null ? relatedWalletId : null)
				.setWalletId(walletId)
				.setCreatedAt(createdAt == null ? LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant()
						: createdAt.atZone(ZoneId.systemDefault()).toInstant())
				.setTypeTransaction(transactionType.getValue()).build();
	}

	public static BigDecimal byteBufferToBigDecimal(ByteBuffer buffer) {

	    if (buffer == null || buffer.remaining() == 0) {
	        throw new IllegalArgumentException("ByteBuffer is null or empty");
	    }


	    buffer.rewind();

	
	    byte[] bytes = new byte[buffer.remaining()];
	    buffer.get(bytes);

	    BigInteger unscaledValue = new BigInteger(bytes);
	    int scale = 2; 
	    return new BigDecimal(unscaledValue, scale);
	}

	private ByteBuffer bigDecimalToByteBuffer(BigDecimal value) {
		if (value == null) {
			return null;
		}

		byte[] decimalBytes = value.unscaledValue().toByteArray();
		return ByteBuffer.wrap(decimalBytes);
	}
}
