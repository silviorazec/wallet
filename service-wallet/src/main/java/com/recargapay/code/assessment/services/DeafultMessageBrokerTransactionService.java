package com.recargapay.code.assessment.services;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.recargapay.code.assessment.ports.exceptions.WalletException;
import com.recargapay.code.assessment.ports.interfaces.MessageBrokerTransactionService;
import com.recargapay.code.assessment.topics.Notification;
import com.recargapay.code.assessment.topics.Transaction;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.apache.kafka.common.header.Header;

import java.time.Instant;

import org.apache.kafka.clients.producer.ProducerRecord;

@Service
public class DeafultMessageBrokerTransactionService implements MessageBrokerTransactionService{

	private KafkaTemplate<String, Object> kafkaTemplate;

	public DeafultMessageBrokerTransactionService(KafkaTemplate<String, Object> kafkaTemplate) {
		this.kafkaTemplate = kafkaTemplate;
	}

	@Override
	public void publishTransaction(Transaction transaction, String jwtToken) throws WalletException {
		try {
			long timestamp = Instant.now().toEpochMilli();
			Header jwtHeader = new RecordHeader("Authorization", ("Bearer " +jwtToken).getBytes());
	
			ProducerRecord<String, Object> producerRecord = new ProducerRecord<>("transaction", null, timestamp, null,
					transaction);
			producerRecord.headers().add(jwtHeader);
	
	
			kafkaTemplate.send(producerRecord).get();
		}catch (Exception e) {
			throw new WalletException("Problems to producer this message", e);
		}
	}
	
	@Override
	public void notify(Notification notification, String jwtToken) throws WalletException {
		try {
			Header jwtHeader = new RecordHeader("Authorization", ("Bearer " +jwtToken).getBytes());
			
			ProducerRecord<String, Object> producerRecord = new ProducerRecord<>("notification", null, null, null,
					notification);
			producerRecord.headers().add(jwtHeader);
			kafkaTemplate.send(producerRecord).get();
		}catch (Exception e) {
			throw new WalletException("Problems to producer this message", e);
		}
	}

}
