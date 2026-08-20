package com.jackpot.strategy.contribution;

import com.jackpot.entity.JackpotContributionConfiguration;
import com.jackpot.enums.StrategyType;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class FixedContributionStrategy implements ContributionStrategy {

    private static final Logger log = LoggerFactory.getLogger(FixedContributionStrategy.class);

    @Override
    public StrategyType getStrategyType() {
        return StrategyType.FIXED;
    }

    /**
     * Calculates the contribution to the jackpot based on a fixed percentage of the stake amount.
     *
     * @param config The contribution configuration containing the fixed percentage
     * @param stakeAmount The amount given by the user
     * @param currentAmount The current amount in the jackpot pool (unused in fixed strategy)
     * @return The calculated contribution amount to be added to the jackpot
     */
    @Override
    public BigDecimal calculateContribution(JackpotContributionConfiguration config, BigDecimal stakeAmount, BigDecimal currentAmount) {
        log.info("Calculating contribution using FIXED strategy. Stake amount: {}", stakeAmount);
        
        BigDecimal effectivePct = config.getContributionPct();
        log.debug("Fixed contribution percentage retrieved from config: {}%", effectivePct);
        
        BigDecimal contribution = stakeAmount.multiply(effectivePct)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                
        log.info("Calculated FIXED contribution amount: {}", contribution);
        return contribution;
    }
}
