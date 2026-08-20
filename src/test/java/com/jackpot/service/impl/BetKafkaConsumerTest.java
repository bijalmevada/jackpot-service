package com.jackpot.service.impl;

import com.jackpot.dto.BetRequestDto;
import com.jackpot.service.BetKafkaConsumer;
import com.jackpot.service.BetTransactionProcessor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BetKafkaConsumerTest {

    @Mock
    private BetTransactionProcessor betTransactionProcessor;

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    private BetKafkaConsumer betKafkaConsumer;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(betKafkaConsumer, "dlqTopic", "jackpot-bets-dlq");
    }

    @Test
    void consumeBetEvent_Success() {
        BetRequestDto request = new BetRequestDto("bet-5", "user-5", "jackpot-5", new BigDecimal("10"));
        
        doNothing().when(betTransactionProcessor).processBetContributionAndEvaluation(request);
        
        betKafkaConsumer.consumeBetEvent(request);
        
        verify(betTransactionProcessor, times(1)).processBetContributionAndEvaluation(request);
        verify(kafkaTemplate, never()).send(anyString(), anyString(), any());
    }

    @Test
    void consumeBetEvent_CatchesException_AndSendsToDLQ() {
        BetRequestDto request = new BetRequestDto("bet-6", "user-6", "jackpot-6", new BigDecimal("10"));
        
        doThrow(new RuntimeException("Simulated error")).when(betTransactionProcessor).processBetContributionAndEvaluation(request);
        
        // Should catch the exception, not throw it outwards, and send to DLQ
        betKafkaConsumer.consumeBetEvent(request);

        verify(betTransactionProcessor, times(1)).processBetContributionAndEvaluation(request);
        verify(kafkaTemplate, times(1)).send(eq("jackpot-bets-dlq"), eq("bet-6"), eq(request));
    }
}
