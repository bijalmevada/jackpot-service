package com.jackpot.strategy.contribution;

import com.jackpot.entity.JackpotContributionConfiguration;
import com.jackpot.enums.StrategyType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FixedContributionStrategyTest {

    private final FixedContributionStrategy strategy = new FixedContributionStrategy();

    @Test
    void getStrategyType_ReturnsFixed() {
        assertEquals(StrategyType.FIXED, strategy.getStrategyType());
    }

    @Test
    void calculateContribution_AppliesFixedPercentage() {
        JackpotContributionConfiguration config = new JackpotContributionConfiguration();
        config.setContributionPct(new BigDecimal("10.0")); // 10%

        BigDecimal stake = new BigDecimal("100.00");
        BigDecimal currentPool = new BigDecimal("500.00");

        BigDecimal result = strategy.calculateContribution(config, stake, currentPool);

        assertEquals(new BigDecimal("10.00"), result);
    }

    @Test
    void calculateContribution_RoundingEdgeCases() {
        JackpotContributionConfiguration config = new JackpotContributionConfiguration();
        // 13.5% contribution
        config.setContributionPct(new BigDecimal("13.5")); 
        
        // Stake is 33.33, expected contribution = 33.33 * 0.135 = 4.49955 -> rounded to 4.50
        BigDecimal stake = new BigDecimal("33.33");
        BigDecimal currentPool = new BigDecimal("500.00");

        BigDecimal result = strategy.calculateContribution(config, stake, currentPool);

        assertEquals(new BigDecimal("4.50"), result);
    }
}
