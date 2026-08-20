package com.jackpot.strategy.contribution;

import com.jackpot.entity.JackpotContributionConfiguration;
import com.jackpot.enums.StrategyType;
import java.math.BigDecimal;

public interface ContributionStrategy {

    StrategyType getStrategyType();

    BigDecimal calculateContribution(JackpotContributionConfiguration config, BigDecimal stakeAmount, BigDecimal currentAmount);
}
