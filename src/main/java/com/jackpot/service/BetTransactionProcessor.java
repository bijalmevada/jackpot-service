package com.jackpot.service;

import com.jackpot.dto.BetRequestDto;
import com.jackpot.entity.Jackpot;
import com.jackpot.entity.JackpotContribution;
import com.jackpot.entity.JackpotContributionConfiguration;
import com.jackpot.entity.JackpotReward;
import com.jackpot.entity.JackpotRewardConfiguration;
import com.jackpot.repository.JackpotContributionConfigurationRepository;
import com.jackpot.repository.JackpotContributionRepository;
import com.jackpot.repository.JackpotRepository;
import com.jackpot.repository.JackpotRewardConfigurationRepository;
import com.jackpot.repository.JackpotRewardRepository;
import com.jackpot.strategy.contribution.ContributionStrategyFactory;
import com.jackpot.strategy.reward.RewardStrategyFactory;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class BetTransactionProcessor {

    private static final Logger log = LoggerFactory.getLogger(BetTransactionProcessor.class);

    private final JackpotContributionRepository jackpotContributionRepository;
    private final JackpotRepository jackpotRepository;
    private final JackpotContributionConfigurationRepository jackpotContributionConfigurationRepository;
    private final JackpotRewardRepository jackpotRewardRepository;
    private final JackpotRewardConfigurationRepository jackpotRewardConfigurationRepository;
    private final ContributionStrategyFactory contributionStrategyFactory;
    private final RewardStrategyFactory rewardStrategyFactory;

    @Transactional
    public void processBetContributionAndEvaluation(BetRequestDto betRequestDto) {
        log.info("Processing bet event: betId={}, userId={}, jackpotId={}, stake={}",
                betRequestDto.getBetId(), betRequestDto.getUserId(), betRequestDto.getJackpotId(), betRequestDto.getBetAmount());

        String jackpotId = betRequestDto.getJackpotId();

        Jackpot jackpot = jackpotRepository.findByIdForUpdate(jackpotId)
                .orElseThrow(() -> new IllegalArgumentException("Jackpot not found: " + jackpotId));

        // check duplicate processing of the bet
        if (jackpotContributionRepository.existsByBetIdAndUserIdAndJackpot_Id(betRequestDto.getBetId(), betRequestDto.getUserId(), jackpotId)) {
            log.info("Bet ID {} already processed. Skipping duplicate message.", betRequestDto.getBetId());
            return;
        }

        // check reward and contribution configuration before processing the request to avoid rollbacks.
        JackpotContributionConfiguration jackpotContributionConfiguration = jackpotContributionConfigurationRepository.findByJackpot_Id(jackpotId)
                .orElseThrow(() -> new IllegalArgumentException("Contribution configuration not found for jackpot: " + jackpotId));

        JackpotRewardConfiguration rewardConfig = jackpotRewardConfigurationRepository.findByJackpot_Id(jackpotId)
                .orElseThrow(() -> new IllegalArgumentException("Reward configuration not found for jackpot: " + jackpotId));

        // calculate contribution amount based on the strategy type
        BigDecimal contributionAmount = contributionStrategyFactory.getStrategy(jackpotContributionConfiguration.getStrategyType())
                .calculateContribution(jackpotContributionConfiguration, betRequestDto.getBetAmount(), jackpot.getCurrentAmount());

        // Update Jackpot current amount
        BigDecimal newAmount = jackpot.getCurrentAmount().add(contributionAmount);
        jackpot.setCurrentAmount(newAmount);
        jackpot.setUpdatedAtDate(LocalDateTime.now());
        jackpotRepository.save(jackpot);

        // save jackpot contribution
        JackpotContribution contributionRecord = JackpotContribution.builder()
                .betId(betRequestDto.getBetId())
                .userId(betRequestDto.getUserId())
                .jackpot(jackpot)
                .stakeAmount(betRequestDto.getBetAmount())
                .contributionAmount(contributionAmount)
                .currentJackpotAmount(newAmount)
                .createdAtDate(LocalDateTime.now())
                .build();

        jackpotContributionRepository.save(contributionRecord);


        log.info("Bet {} contributed {} to Jackpot {}. New Pool: {}",
                betRequestDto.getBetId(), contributionAmount, betRequestDto.getJackpotId(), newAmount);


        /*
        * win probability calculation based on the strategy type
        *
        * Design decision: Reward evaluation is performed during Kafka bet processing. The reward API returns the persisted evaluation
        * result rather than performing a new evaluation.This avoids ambiguity caused by delayed or concurrent reward requests.
        *
        * Alternative considered: Reward evaluation could instead be performed when the reward API is called. In that model,
        * the API would need to handle concurrent requests for bets belonging to the same jackpot and ensure that only one request
        * can claim and reset the jackpot. Additional state management or locking would therefore be required to define how pending
        * bets are handled after a jackpot is awarded.*/

        boolean isWinner = rewardStrategyFactory.getStrategy(rewardConfig.getStrategyType())
                .evaluateReward(rewardConfig, newAmount);

        if (isWinner) {

            JackpotReward rewardRecord = JackpotReward.builder()
                    .betId(betRequestDto.getBetId())
                    .userId(betRequestDto.getUserId())
                    .jackpot(jackpot)
                    .jackpotRewardAmount(newAmount)
                    .createdAtDate(LocalDateTime.now())
                    .build();
            jackpotRewardRepository.save(rewardRecord);

            // Reset Jackpot Pool to Initial Seed Value
            jackpot.setCurrentAmount(jackpot.getInitialAmount());
            jackpot.setUpdatedAtDate(LocalDateTime.now());
            jackpotRepository.save(jackpot);

            log.info("JACKPOT WINNER! User {} won {} on Bet {} and jackpot {}. Pool reset to {}",
                    betRequestDto.getUserId(), newAmount, betRequestDto.getBetId(), jackpotId, jackpot.getInitialAmount());
        } else {
            log.info("Bet {} did not win reward for Jackpot {}", betRequestDto.getBetId(), jackpotId);
        }
    }
}
