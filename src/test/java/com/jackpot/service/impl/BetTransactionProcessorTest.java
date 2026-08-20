package com.jackpot.service.impl;

import com.jackpot.dto.BetRequestDto;
import com.jackpot.entity.Jackpot;
import com.jackpot.entity.JackpotContributionConfiguration;
import com.jackpot.entity.JackpotRewardConfiguration;
import com.jackpot.enums.StrategyType;
import com.jackpot.repository.*;
import com.jackpot.service.BetTransactionProcessor;
import com.jackpot.strategy.contribution.ContributionStrategy;
import com.jackpot.strategy.contribution.ContributionStrategyFactory;
import com.jackpot.strategy.reward.RewardStrategy;
import com.jackpot.strategy.reward.RewardStrategyFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BetTransactionProcessorTest {

    @Mock
    private JackpotContributionRepository jackpotContributionRepository;
    @Mock
    private JackpotRepository jackpotRepository;
    @Mock
    private JackpotContributionConfigurationRepository jackpotContributionConfigurationRepository;
    @Mock
    private JackpotRewardRepository jackpotRewardRepository;
    @Mock
    private JackpotRewardConfigurationRepository jackpotRewardConfigurationRepository;
    @Mock
    private ContributionStrategyFactory contributionStrategyFactory;
    @Mock
    private RewardStrategyFactory rewardStrategyFactory;

    @Mock
    private ContributionStrategy contributionStrategy;
    @Mock
    private RewardStrategy rewardStrategy;

    @InjectMocks
    private BetTransactionProcessor betTransactionProcessor;

    @Test
    void processBetContributionAndEvaluation_WinnerFlow() {
        BetRequestDto request = new BetRequestDto("bet-1", "user-1", "jackpot-1", new BigDecimal("10"));

        Jackpot jackpot = buildJackpot();
        JackpotContributionConfiguration contribConfig = buildJackpotContributionConfiguration(StrategyType.FIXED);
        JackpotRewardConfiguration rewardConfig = buildJackpotRewardConfiguration(StrategyType.FIXED);

        when(jackpotRepository.findByIdForUpdate("jackpot-1")).thenReturn(Optional.of(jackpot));
        when(jackpotContributionRepository.existsByBetIdAndUserIdAndJackpot_Id("bet-1", "user-1", "jackpot-1")).thenReturn(false);
        when(jackpotContributionConfigurationRepository.findByJackpot_Id("jackpot-1")).thenReturn(Optional.of(contribConfig));

        when(contributionStrategyFactory.getStrategy(StrategyType.FIXED)).thenReturn(contributionStrategy);
        when(contributionStrategy.calculateContribution(contribConfig, new BigDecimal("10"), new BigDecimal("90")))
                .thenReturn(new BigDecimal("10"));

        when(jackpotRewardConfigurationRepository.findByJackpot_Id("jackpot-1")).thenReturn(Optional.of(rewardConfig));
        
        when(rewardStrategyFactory.getStrategy(StrategyType.FIXED)).thenReturn(rewardStrategy);
        when(rewardStrategy.evaluateReward(rewardConfig, new BigDecimal("100"))).thenReturn(true);

        betTransactionProcessor.processBetContributionAndEvaluation(request);

        // Verify contribution saved
        verify(jackpotContributionRepository, times(1)).save(any());
        // Verify reward saved
        verify(jackpotRewardRepository, times(1)).save(any());
        // Verify jackpot updated twice (once for contribution, once for reset)
        verify(jackpotRepository, times(2)).save(jackpot);
    }

    @Test
    void processBetContributionAndEvaluation_NonWinnerFlow() {
        BetRequestDto request = new BetRequestDto("bet-2", "user-2", "jackpot-1", new BigDecimal("10"));

        Jackpot jackpot = buildJackpot();
        JackpotContributionConfiguration contribConfig = buildJackpotContributionConfiguration(StrategyType.FIXED);
        JackpotRewardConfiguration rewardConfig = buildJackpotRewardConfiguration(StrategyType.FIXED);

        when(jackpotRepository.findByIdForUpdate("jackpot-1")).thenReturn(Optional.of(jackpot));
        when(jackpotContributionRepository.existsByBetIdAndUserIdAndJackpot_Id("bet-2", "user-2", "jackpot-1")).thenReturn(false);
        when(jackpotContributionConfigurationRepository.findByJackpot_Id("jackpot-1")).thenReturn(Optional.of(contribConfig));

        when(contributionStrategyFactory.getStrategy(StrategyType.FIXED)).thenReturn(contributionStrategy);
        when(contributionStrategy.calculateContribution(contribConfig, new BigDecimal("10"), new BigDecimal("90")))
                .thenReturn(new BigDecimal("10"));

        when(jackpotRewardConfigurationRepository.findByJackpot_Id("jackpot-1")).thenReturn(Optional.of(rewardConfig));
        
        when(rewardStrategyFactory.getStrategy(StrategyType.FIXED)).thenReturn(rewardStrategy);
        when(rewardStrategy.evaluateReward(rewardConfig, new BigDecimal("100"))).thenReturn(false); // Does not win

        betTransactionProcessor.processBetContributionAndEvaluation(request);

        verify(jackpotContributionRepository, times(1)).save(any());
        verify(jackpotRewardRepository, never()).save(any());
        verify(jackpotRepository, times(1)).save(jackpot); // Only updated once for contribution
    }

    @Test
    void processBet_DuplicateMessage_SkipsProcessing() {
        BetRequestDto request = new BetRequestDto("dup", "user-1", "jackpot-1", new BigDecimal("10"));
        
        Jackpot jackpot = buildJackpot();
        when(jackpotRepository.findByIdForUpdate("jackpot-1")).thenReturn(Optional.of(jackpot));
        when(jackpotContributionRepository.existsByBetIdAndUserIdAndJackpot_Id("dup", "user-1", "jackpot-1")).thenReturn(true);
        
        betTransactionProcessor.processBetContributionAndEvaluation(request);
        
        verify(jackpotContributionConfigurationRepository, never()).findByJackpot_Id(anyString());
    }

    @Test
    void processBet_MissingJackpot_ThrowsException() {
        BetRequestDto request = new BetRequestDto("bet-3", "user-3", "invalid-jackpot", new BigDecimal("10"));
        when(jackpotRepository.findByIdForUpdate("invalid-jackpot")).thenReturn(Optional.empty());
        
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, 
                () -> betTransactionProcessor.processBetContributionAndEvaluation(request));
    }

    @Test
    void processBet_MissingConfigs_ThrowsException() {
        BetRequestDto request = new BetRequestDto("bet-4", "user-4", "jackpot-4", new BigDecimal("10"));
        Jackpot jackpot = Jackpot.builder().id("jackpot-4").currentAmount(BigDecimal.ZERO).build();

        when(jackpotRepository.findByIdForUpdate("jackpot-4")).thenReturn(Optional.of(jackpot));
        when(jackpotContributionRepository.existsByBetIdAndUserIdAndJackpot_Id("bet-4", "user-4", "jackpot-4")).thenReturn(false);
        when(jackpotContributionConfigurationRepository.findByJackpot_Id("jackpot-4")).thenReturn(Optional.empty());
        
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class,
                () -> betTransactionProcessor.processBetContributionAndEvaluation(request));
    }

    private Jackpot buildJackpot(){
        Jackpot jackpot = Jackpot.builder()
                .id("jackpot-1")
                .currentAmount(new BigDecimal("90"))
                .initialAmount(new BigDecimal("0"))
                .build();
        return jackpot;
    }

    private JackpotContributionConfiguration buildJackpotContributionConfiguration(StrategyType strategyType){
        JackpotContributionConfiguration contribConfig = JackpotContributionConfiguration.builder()
                .strategyType(strategyType)
                .build();
        return contribConfig;
    }

    private JackpotRewardConfiguration buildJackpotRewardConfiguration(StrategyType strategyType){
        JackpotRewardConfiguration rewardConfig = JackpotRewardConfiguration.builder()
                .strategyType(strategyType)
                .build();
        return rewardConfig;
    }
}
