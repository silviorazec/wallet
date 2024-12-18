package com.recargapay.code.assessment.test.service;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.CompletableFuture;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Header;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ContextConfiguration;

import com.recargapay.code.assessment.ServiceWalletApplication;
import com.recargapay.code.assessment.ports.exceptions.WalletException;
import com.recargapay.code.assessment.services.DeafultMessageBrokerTransactionService;
import com.recargapay.code.assessment.topics.Notification;
import com.recargapay.code.assessment.topics.Transaction;

@ExtendWith(MockitoExtension.class)
@ContextConfiguration(classes = { DeafultMessageBrokerTransactionService.class, ServiceWalletApplication.class})
public class DeafultMessageBrokerTransactionServiceTest {

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    private DeafultMessageBrokerTransactionService service;

    @Test
    void testPublishTransactionSuccess() throws Exception {
        // Arrange
        Transaction transaction = new Transaction();
        String jwtToken = "valid-jwt-token";

        when(kafkaTemplate.send(any(ProducerRecord.class)))
                .thenReturn(CompletableFuture.completedFuture(null));

        // Act
        service.publishTransaction(transaction, jwtToken);

        // Assert
        ArgumentCaptor<ProducerRecord<String, Object>> recordCaptor = ArgumentCaptor.forClass(ProducerRecord.class);
        verify(kafkaTemplate, times(1)).send(recordCaptor.capture());

        ProducerRecord<String, Object> capturedRecord = recordCaptor.getValue();
        assertEquals("transaction", capturedRecord.topic());
        assertEquals(transaction, capturedRecord.value());

        Header authHeader = capturedRecord.headers().lastHeader("Authorization");
        assertNotNull(authHeader);
        assertEquals("Bearer valid-jwt-token", new String(authHeader.value()));
    }

    @Test
    void testPublishTransactionThrowsWalletException() {
        // Arrange
        Transaction transaction = new Transaction();
        String jwtToken = "valid-jwt-token";

        when(kafkaTemplate.send(any(ProducerRecord.class)))
                .thenReturn(CompletableFuture.failedFuture(new RuntimeException("Kafka error")));

        // Act & Assert
        WalletException exception = assertThrows(WalletException.class, () -> {
            service.publishTransaction(transaction, jwtToken);
        });

        assertEquals("Problems to producer this message", exception.getMessage());
        verify(kafkaTemplate, times(1)).send(any(ProducerRecord.class));
    }



    @Test
    void testPublishTransactionException() {
        // Arrange
        Transaction transaction = new Transaction();
        String jwtToken = "valid-jwt-token";

        when(kafkaTemplate.send(any(ProducerRecord.class))).thenReturn(CompletableFuture.failedFuture(new RuntimeException("Kafka failure")));

        // Act & Assert
        WalletException exception = assertThrows(WalletException.class, () -> {
            service.publishTransaction(transaction, jwtToken);
        });

        assertEquals("Problems to producer this message", exception.getMessage());
        verify(kafkaTemplate, times(1)).send(any(ProducerRecord.class));
    }

    @Test
    void testNotifySuccess() throws Exception {
        // Arrange
        Notification notification = new Notification();
        String jwtToken = "valid-jwt-token";

        when(kafkaTemplate.send(any(ProducerRecord.class))).thenReturn(CompletableFuture.completedFuture(null));

        // Act
        service.notify(notification, jwtToken);

        // Assert
        ArgumentCaptor<ProducerRecord<String, Object>> recordCaptor = ArgumentCaptor.forClass(ProducerRecord.class);
        verify(kafkaTemplate, times(1)).send(recordCaptor.capture());

        ProducerRecord<String, Object> capturedRecord = recordCaptor.getValue();

        assertEquals("notification", capturedRecord.topic());
        assertEquals(notification, capturedRecord.value());
        assertTrue(capturedRecord.headers().lastHeader("Authorization") != null);
        Header authHeader = capturedRecord.headers().lastHeader("Authorization");
        assertEquals("Bearer valid-jwt-token", new String(authHeader.value()));
    }

    @Test
    void testNotifyException() {
        // Arrange
        Notification notification = new Notification();
        String jwtToken = "valid-jwt-token";

        when(kafkaTemplate.send(any(ProducerRecord.class))).thenReturn(CompletableFuture.failedFuture(new RuntimeException("Kafka failure")));

        // Act & Assert
        WalletException exception = assertThrows(WalletException.class, () -> {
            service.notify(notification, jwtToken);
        });

        assertEquals("Problems to producer this message", exception.getMessage());
        verify(kafkaTemplate, times(1)).send(any(ProducerRecord.class));
    }
    
}
