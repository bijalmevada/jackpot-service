package com.jackpot.strategy.contribution;

import com.jackpot.entity.JackpotContributionConfiguration;
import com.jackpot.enums.StrategyType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class VariableContributionStrategyTest {

    private final VariableContributionStrategy strategy = new VariableContributionStrategy();

    @Test
    void getStrategyType_ReturnsVariable() {
        assertEquals(StrategyType.VARIABLE, strategy.getStrategyType());
    }

    @Test
    void calculateContribution_PoolBelowStart_UsesStartPct() {
        JackpotContributionConfiguration config = new JackpotContributionConfiguration();
        config.setStartAmount(new BigDecimal("1000"));
        config.setMaxAmount(new BigDecimal("5000"));
        config.setContributionPct(new BigDecimal("10.0")); // 10%
        config.setMinContributionPct(new BigDecimal("2.0")); // 2%

        BigDecimal stake = new BigDecimal("100.00");
        BigDecimal currentPool = new BigDecimal("500.00");

        BigDecimal result = strategy.calculateContribution(config, stake, currentPool);

        // Effective pool becomes max(500, 1000) = 1000. Progress = 0. Effective pct = 10.0
        assertEquals(new BigDecimal("10.00"), result);
    }

    @Test
    void calculateContribution_PoolAtMax_UsesMinPct() {
        JackpotContributionConfiguration config = new JackpotContributionConfiguration();
        config.setStartAmount(new BigDecimal("1000"));
        config.setMaxAmount(new BigDecimal("5000"));
        config.setContributionPct(new BigDecimal("10.0")); // 10%
        config.setMinContributionPct(new BigDecimal("2.0")); // 2%

        BigDecimal stake = new BigDecimal("100.00");
        BigDecimal currentPool = new BigDecimal("5000.00");

        BigDecimal result = strategy.calculateContribution(config, stake, currentPool);

        assertEquals(new BigDecimal("2.00"), result);
    }

    @Test
    void calculateContribution_PoolExactlyAtStart_UsesStartPct() {
        JackpotContributionConfiguration config = new JackpotContributionConfiguration();
        config.setStartAmount(new BigDecimal("1000"));
        config.setMaxAmount(new BigDecimal("5000"));
        config.setContributionPct(new BigDecimal("10.0")); // 10%
        config.setMinContributionPct(new BigDecimal("2.0")); // 2%

        BigDecimal stake = new BigDecimal("100.00");
        BigDecimal currentPool = new BigDecimal("1000.00"); // Exactly at start

        BigDecimal result = strategy.calculateContribution(config, stake, currentPool);

        assertEquals(new BigDecimal("10.00"), result);
    }

    @Test
    void calculateContribution_PoolHalfway_CalculatesLinearInterpolation() {
        JackpotContributionConfiguration config = new JackpotContributionConfiguration();
        config.setStartAmount(new BigDecimal("1000"));
        config.setMaxAmount(new BigDecimal("5000"));
        config.setContributionPct(new BigDecimal("10.0")); // 10%
        config.setMinContributionPct(new BigDecimal("2.0")); // 2%

        BigDecimal stake = new BigDecimal("100.00");
        BigDecimal currentPool = new BigDecimal("3000.00"); // Exactly halfway (5000-1000=4000 range. 3000-1000=2000. Progress = 0.5)
        
        // Pct range = 10 - 2 = 8
        // Pct to subtract = 8 * 0.5 = 4
        // Effective pct = 10 - 4 = 6%
        // Expected contribution = 100 * 0.06 = 6.00

        BigDecimal result = strategy.calculateContribution(config, stake, currentPool);

        assertEquals(new BigDecimal("6.00"), result);
    }

    @Test
    void calculateContribution_StartAmountGreaterThanMax_UsesMinPct() {
        JackpotContributionConfiguration config = new JackpotContributionConfiguration();
        config.setStartAmount(new BigDecimal("6000")); // Start > Max
        config.setMaxAmount(new BigDecimal("5000"));
        config.setContributionPct(new BigDecimal("10.0")); // 10%
        config.setMinContributionPct(new BigDecimal("2.0")); // 2%

        BigDecimal stake = new BigDecimal("100.00");
        BigDecimal currentPool = new BigDecimal("1000.00"); // Doesn't matter

        BigDecimal result = strategy.calculateContribution(config, stake, currentPool);

        assertEquals(new BigDecimal("2.00"), result);
    }
}
