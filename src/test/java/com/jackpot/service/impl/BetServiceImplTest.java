package com.jackpot.service.impl;

import com.jackpot.dto.BetRequestDto;
import com.jackpot.repository.JackpotContributionRepository;
import com.jackpot.repository.JackpotRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;
import org.springframework.kafka.support.SendResult;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BetServiceImplTest {

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Mock
    private JackpotRepository jackpotRepository;

    @Mock
    private JackpotContributionRepository jackpotContributionRepository;

    @InjectMocks
    private BetServiceImpl betService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(betService, "topicName", "jackpot-bets");
    }

    @Test
    void processBet_Success() {
        BetRequestDto request = new BetRequestDto("bet-1", "user-1", "jackpot-1", new BigDecimal("10"));

        when(jackpotRepository.existsById("jackpot-1")).thenReturn(true);
        when(jackpotContributionRepository.existsByBetIdAndUserIdAndJackpot_Id("bet-1", "user-1", "jackpot-1")).thenReturn(false);

        CompletableFuture<SendResult<String, Object>> future = CompletableFuture.completedFuture(null);
        when(kafkaTemplate.send(eq("jackpot-bets"), eq("jackpot-1"), eq(request))).thenReturn(future);

        betService.processBet(request);

        verify(kafkaTemplate, times(1)).send("jackpot-bets", "jackpot-1", request);
    }

    @Test
    void processBet_NullRequest_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> betService.processBet(null));
    }

    @Test
    void processBet_MissingJackpot_ThrowsIllegalArgumentException() {
        BetRequestDto request = new BetRequestDto("bet-1", "user-1", "invalid-jackpot", new BigDecimal("10"));

        when(jackpotRepository.existsById("invalid-jackpot")).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> betService.processBet(request));
    }

    @Test
    void processBet_DuplicateBet_ThrowsIllegalArgumentException() {
        BetRequestDto request = new BetRequestDto("dup-bet", "user-1", "jackpot-1", new BigDecimal("10"));

        when(jackpotRepository.existsById("jackpot-1")).thenReturn(true);
        when(jackpotContributionRepository.existsByBetIdAndUserIdAndJackpot_Id("dup-bet", "user-1", "jackpot-1")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> betService.processBet(request));
    }

    @Test
    void processBet_MissingBetId_ThrowsIllegalArgumentException() {
        BetRequestDto request = new BetRequestDto("", "user-1", "jackpot-1", new BigDecimal("10"));
        assertThrows(IllegalArgumentException.class, () -> betService.processBet(request));
    }

    @Test
    void processBet_MissingUserId_ThrowsIllegalArgumentException() {
        BetRequestDto request = new BetRequestDto("bet-1", null, "jackpot-1", new BigDecimal("10"));
        assertThrows(IllegalArgumentException.class, () -> betService.processBet(request));
    }

    @Test
    void processBet_InvalidBetAmount_ThrowsIllegalArgumentException() {
        BetRequestDto request = new BetRequestDto("bet-1", "user-1", "jackpot-1", BigDecimal.ZERO);
        assertThrows(IllegalArgumentException.class, () -> betService.processBet(request));
    }


    @Test
    void processBet_KafkaTimeoutException_ThrowsRuntimeException() throws Exception {
        BetRequestDto request = new BetRequestDto("bet-1", "user-1", "jackpot-1", new BigDecimal("10"));

        when(jackpotContributionRepository.existsByBetIdAndUserIdAndJackpot_Id("bet-1", "user-1", "jackpot-1")).thenReturn(false);
        when(jackpotRepository.existsById("jackpot-1")).thenReturn(true);

        CompletableFuture<SendResult<String, Object>> future = mock(CompletableFuture.class);
        when(future.get(anyLong(), any())).thenThrow(new java.util.concurrent.TimeoutException("Kafka timeout"));
        when(kafkaTemplate.send(eq("jackpot-bets"), eq("jackpot-1"), eq(request))).thenReturn(future);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> betService.processBet(request));
        org.junit.jupiter.api.Assertions.assertTrue(ex.getMessage().contains("Kafka is down"));
    }

    @Test
    void processBet_KafkaExecutionException_ThrowsRuntimeException() throws Exception {
        BetRequestDto request = new BetRequestDto("bet-1", "user-1", "jackpot-1", new BigDecimal("10"));

        when(jackpotContributionRepository.existsByBetIdAndUserIdAndJackpot_Id("bet-1", "user-1", "jackpot-1")).thenReturn(false);
        when(jackpotRepository.existsById("jackpot-1")).thenReturn(true);

        CompletableFuture<SendResult<String, Object>> future = mock(CompletableFuture.class);
        when(future.get(anyLong(), any())).thenThrow(new java.util.concurrent.ExecutionException(new RuntimeException("Broker failure")));
        when(kafkaTemplate.send(eq("jackpot-bets"), eq("jackpot-1"), eq(request))).thenReturn(future);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> betService.processBet(request));
        org.junit.jupiter.api.Assertions.assertTrue(ex.getMessage().contains("Failed to publish bet to Kafka"));
    }

    @Test
    void processBet_MissingJackpotId_ThrowsIllegalArgumentException() {
        BetRequestDto request = new BetRequestDto("bet-1", "user-1", "", new BigDecimal("10"));
        assertThrows(IllegalArgumentException.class, () -> betService.processBet(request));

        BetRequestDto requestNullJackpot = new BetRequestDto("bet-1", "user-1", null, new BigDecimal("10"));
        assertThrows(IllegalArgumentException.class, () -> betService.processBet(requestNullJackpot));
    }

    @Test
    void processBet_KafkaInterruptedException_ThrowsRuntimeException() throws Exception {
        BetRequestDto request = new BetRequestDto("bet-1", "user-1", "jackpot-1", new BigDecimal("10"));

        when(jackpotContributionRepository.existsByBetIdAndUserIdAndJackpot_Id("bet-1", "user-1", "jackpot-1")).thenReturn(false);
        when(jackpotRepository.existsById("jackpot-1")).thenReturn(true);

        CompletableFuture<org.springframework.kafka.support.SendResult<String, Object>> future = mock(CompletableFuture.class);
        when(future.get(anyLong(), any())).thenThrow(new InterruptedException("Thread interrupted"));
        when(kafkaTemplate.send(eq("jackpot-bets"), eq("jackpot-1"), eq(request))).thenReturn(future);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> betService.processBet(request));
        org.junit.jupiter.api.Assertions.assertTrue(ex.getMessage().contains("Kafka publishing interrupted"));
    }
}
