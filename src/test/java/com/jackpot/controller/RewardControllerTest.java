package com.jackpot.controller;

import com.jackpot.dto.JackpotRewardDto;
import com.jackpot.dto.RewardEvaluationDto;
import com.jackpot.service.RewardEvaluationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RewardControllerTest {

    @Mock
    private RewardEvaluationService rewardEvaluationService;

    @InjectMocks
    private RewardController rewardController;

    @Test
    void evaluateReward_Success() {
        RewardEvaluationDto request = new RewardEvaluationDto();
        request.setBetId("bet-1");

        JackpotRewardDto mockResponse = JackpotRewardDto.builder()
                .betId("bet-1")
                .userId("user-1")
                .jackpotId("jackpot-1")
                .jackpotRewardAmount(BigDecimal.TEN)
                .build();

        when(rewardEvaluationService.evaluateReward(request)).thenReturn(mockResponse);
        BindingResult mockBindingResult = mock(BindingResult.class);
        when(mockBindingResult.hasErrors()).thenReturn(false);

        ResponseEntity<?> response = rewardController.evaluateReward(request, mockBindingResult);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockResponse, response.getBody());
        verify(rewardEvaluationService, times(1)).evaluateReward(request);
    }

    @Test
    void evaluateReward_IllegalArgumentException() {
        RewardEvaluationDto request = new RewardEvaluationDto();
        when(rewardEvaluationService.evaluateReward(request)).thenThrow(new IllegalStateException("Invalid ID"));
        BindingResult mockBindingResult = mock(BindingResult.class);
        when(mockBindingResult.hasErrors()).thenReturn(false);

        ResponseEntity<?> response = rewardController.evaluateReward(request, mockBindingResult);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid ID", response.getBody());
    }

    @Test
    void evaluateReward_UnexpectedException() {
        RewardEvaluationDto request = new RewardEvaluationDto();
        when(rewardEvaluationService.evaluateReward(request)).thenThrow(new RuntimeException("DB error"));
        BindingResult mockBindingResult = mock(BindingResult.class);
        when(mockBindingResult.hasErrors()).thenReturn(false);

        ResponseEntity<?> response = rewardController.evaluateReward(request, mockBindingResult);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("An unexpected error occurred", response.getBody());
    }
}
