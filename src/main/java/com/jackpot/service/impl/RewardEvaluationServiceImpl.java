package com.jackpot.service.impl;

import com.jackpot.dto.JackpotRewardDto;
import com.jackpot.dto.RewardEvaluationDto;
import com.jackpot.entity.JackpotReward;
import com.jackpot.repository.JackpotContributionRepository;
import com.jackpot.repository.JackpotRewardRepository;
import com.jackpot.service.RewardEvaluationService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@AllArgsConstructor
public class RewardEvaluationServiceImpl implements RewardEvaluationService {

    private static final Logger log = LoggerFactory.getLogger(RewardEvaluationServiceImpl.class);

    private final JackpotRewardRepository jackpotRewardRepository;
    private final JackpotContributionRepository jackpotContributionRepository;

    @Override
    public JackpotRewardDto evaluateReward(RewardEvaluationDto rewardEvaluationDto) {

        log.info("Evaluating reward for bet request: {}", rewardEvaluationDto);

        if (!jackpotContributionRepository.existsByBetIdAndUserIdAndJackpot_Id(
                rewardEvaluationDto.getBetId(), rewardEvaluationDto.getUserId(), rewardEvaluationDto.getJackpotId())) {
            log.warn("Validation failed: Bet ID {} for user {} not found in jackpot {}", 
                    rewardEvaluationDto.getBetId(), rewardEvaluationDto.getUserId(), rewardEvaluationDto.getJackpotId());
            throw new IllegalStateException("Contribution not found for Bet ID " + rewardEvaluationDto.getBetId() + " in this jackpot.");
        }

        Optional<JackpotReward> jackpotRewardOptional = jackpotRewardRepository.findByBetIdAndUserIdAndJackpot_Id(
                rewardEvaluationDto.getBetId(), rewardEvaluationDto.getUserId(), rewardEvaluationDto.getJackpotId());

        return jackpotRewardOptional
                .map(reward -> JackpotRewardDto.builder()
                        .betId(reward.getBetId())
                        .userId(reward.getUserId())
                        .jackpotId(reward.getJackpot().getId())
                        .jackpotRewardAmount(reward.getJackpotRewardAmount())
                        .build())
                .orElse(JackpotRewardDto.builder()
                        .betId(rewardEvaluationDto.getBetId())
                        .userId(rewardEvaluationDto.getUserId())
                        .jackpotId(rewardEvaluationDto.getJackpotId())
                        .jackpotRewardAmount(BigDecimal.ZERO)
                        .build());
    }
}
