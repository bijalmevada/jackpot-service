package com.jackpot.strategy.reward;

import com.jackpot.entity.JackpotRewardConfiguration;
import com.jackpot.enums.StrategyType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.function.DoubleSupplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import org.springframework.test.util.ReflectionTestUtils;

class VariableRewardStrategyTest {

    private final VariableRewardStrategy strategy = new VariableRewardStrategy();

    @Test
    void getStrategyType_ReturnsVariable() {
        assertEquals(StrategyType.VARIABLE, strategy.getStrategyType());
    }

    @Test
    void evaluateReward_PoolExceedsMax_AlwaysWins() {
        JackpotRewardConfiguration config = new JackpotRewardConfiguration();
        config.setStartAmount(new BigDecimal("1000"));
        config.setMaxAmount(new BigDecimal("5000"));

        BigDecimal currentPool = new BigDecimal("5500.00");

        boolean result = strategy.evaluateReward(config, currentPool);

        assertTrue(result);
    }

    @Test
    void evaluateReward_PoolBelowStart_UsesStartProbability() {
        DoubleSupplier mockSupplier = () -> 0.09;
        ReflectionTestUtils.setField(strategy, "randomSupplier", mockSupplier);
        
        JackpotRewardConfiguration config = new JackpotRewardConfiguration();
        config.setStartAmount(new BigDecimal("1000"));
        config.setMaxAmount(new BigDecimal("5000"));
        config.setRewardProbabilityPct(new BigDecimal("10.0")); // 10%

        BigDecimal currentPool = new BigDecimal("500.00");

        // Uses 10% exactly (0.1). 0.09 < 0.1 -> win
        boolean result = strategy.evaluateReward(config, currentPool);

        assertTrue(result);
    }

    @Test
    void evaluateReward_PoolHalfway_InterpolatesProbability() {
        JackpotRewardConfiguration config = new JackpotRewardConfiguration();
        config.setStartAmount(new BigDecimal("1000"));
        config.setMaxAmount(new BigDecimal("5000"));
        config.setRewardProbabilityPct(new BigDecimal("10.0")); // 10%

        // Halfway (3000).
        // progress = 0.5.
        // prob range = 100 - 10 = 90
        // interpolated = 10 + (90 * 0.5) = 55% (0.55)
        BigDecimal currentPool = new BigDecimal("3000.00");

        // 0.56 > 0.55 -> loss
        DoubleSupplier mockSupplierLoss = () -> 0.56;
        ReflectionTestUtils.setField(strategy, "randomSupplier", mockSupplierLoss);
        assertFalse(strategy.evaluateReward(config, currentPool));

        // 0.54 < 0.55 -> win
        DoubleSupplier mockSupplierWin = () -> 0.54;
        ReflectionTestUtils.setField(strategy, "randomSupplier", mockSupplierWin);
        assertTrue(strategy.evaluateReward(config, currentPool));
    }
}
