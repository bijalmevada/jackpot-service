package com.jackpot.service.impl;

import com.jackpot.dto.JackpotRewardDto;
import com.jackpot.dto.RewardEvaluationDto;
import com.jackpot.entity.Jackpot;
import com.jackpot.entity.JackpotReward;
import com.jackpot.repository.JackpotContributionRepository;
import com.jackpot.repository.JackpotRewardRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RewardEvaluationServiceImplTest {

    @Mock
    private JackpotRewardRepository jackpotRewardRepository;

    @Mock
    private JackpotContributionRepository jackpotContributionRepository;

    @InjectMocks
    private RewardEvaluationServiceImpl rewardEvaluationService;

    @Test
    void evaluateReward_WhenRewardExists_ReturnsAmount() {
        RewardEvaluationDto request = new RewardEvaluationDto("bet-1", "user-1", "jackpot-1");
        
        Jackpot jackpot = new Jackpot();
        jackpot.setId("jackpot-1");
        
        JackpotReward mockReward = JackpotReward.builder()
                .betId("bet-1")
                .userId("user-1")
                .jackpot(jackpot)
                .jackpotRewardAmount(new BigDecimal("100.50"))
                .build();

        when(jackpotContributionRepository.existsByBetIdAndUserIdAndJackpot_Id("bet-1", "user-1", "jackpot-1"))
                .thenReturn(true);
        when(jackpotRewardRepository.findByBetIdAndUserIdAndJackpot_Id("bet-1", "user-1", "jackpot-1"))
                .thenReturn(Optional.of(mockReward));

        JackpotRewardDto result = rewardEvaluationService.evaluateReward(request);

        assertEquals(new BigDecimal("100.50"), result.getJackpotRewardAmount());
        assertEquals("bet-1", result.getBetId());
        assertEquals("user-1", result.getUserId());
        assertEquals("jackpot-1", result.getJackpotId());
        verify(jackpotRewardRepository, times(1)).findByBetIdAndUserIdAndJackpot_Id("bet-1", "user-1", "jackpot-1");
    }

    @Test
    void evaluateReward_WhenNoReward_ReturnsZero() {
        RewardEvaluationDto request = new RewardEvaluationDto("bet-2", "user-2", "jackpot-2");

        when(jackpotContributionRepository.existsByBetIdAndUserIdAndJackpot_Id("bet-2", "user-2", "jackpot-2"))
                .thenReturn(true);
        when(jackpotRewardRepository.findByBetIdAndUserIdAndJackpot_Id("bet-2", "user-2", "jackpot-2"))
                .thenReturn(Optional.empty());

        JackpotRewardDto result = rewardEvaluationService.evaluateReward(request);

        assertEquals(BigDecimal.ZERO, result.getJackpotRewardAmount());
        assertEquals("bet-2", result.getBetId());
        assertEquals("user-2", result.getUserId());
        assertEquals("jackpot-2", result.getJackpotId());
        verify(jackpotRewardRepository, times(1)).findByBetIdAndUserIdAndJackpot_Id("bet-2", "user-2", "jackpot-2");
    }

    @Test
    void evaluateReward_WhenBetNotFound_ThrowsResourceNotFoundException() {
        RewardEvaluationDto request = new RewardEvaluationDto("bet-3", "user-3", "jackpot-3");

        when(jackpotContributionRepository.existsByBetIdAndUserIdAndJackpot_Id("bet-3", "user-3", "jackpot-3"))
                .thenReturn(false);

        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
                rewardEvaluationService.evaluateReward(request));

        assertEquals("Contribution not found for Bet ID bet-3 in this jackpot.", exception.getMessage());
        verify(jackpotContributionRepository, times(1)).existsByBetIdAndUserIdAndJackpot_Id("bet-3", "user-3", "jackpot-3");
        verifyNoInteractions(jackpotRewardRepository);
    }
}
