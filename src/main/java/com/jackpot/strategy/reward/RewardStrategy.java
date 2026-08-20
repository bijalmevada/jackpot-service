package com.jackpot.strategy.reward;

import com.jackpot.entity.JackpotRewardConfiguration;
import com.jackpot.enums.StrategyType;
import java.math.BigDecimal;

public interface RewardStrategy {
    StrategyType getStrategyType();
    boolean evaluateReward(JackpotRewardConfiguration config, BigDecimal currentPoolAmount);
}
