package com.jackpot.service;

import com.jackpot.dto.JackpotRewardDto;
import com.jackpot.dto.RewardEvaluationDto;

public interface RewardEvaluationService {

    JackpotRewardDto evaluateReward(RewardEvaluationDto rewardEvaluationDto);
}
