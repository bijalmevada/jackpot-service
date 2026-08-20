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

class FixedRewardStrategyTest {

    private final FixedRewardStrategy strategy = new FixedRewardStrategy();

    @Test
    void getStrategyType_ReturnsFixed() {
        assertEquals(StrategyType.FIXED, strategy.getStrategyType());
    }

    @Test
    void evaluateReward_ProbabilityMet_ReturnsTrue() {
        DoubleSupplier mockSupplier = () -> 0.49;
        ReflectionTestUtils.setField(strategy, "randomSupplier", mockSupplier);
        
        // 50% chance
        JackpotRewardConfiguration config = new JackpotRewardConfiguration();
        config.setRewardProbabilityPct(new BigDecimal("50.0"));
        
        // 0.49 < 0.50 -> winner
        boolean result = strategy.evaluateReward(config, BigDecimal.ZERO);
        assertTrue(result);
    }

    @Test
    void evaluateReward_ProbabilityNotMet_ReturnsFalse() {
        DoubleSupplier mockSupplier = () -> 0.51;
        ReflectionTestUtils.setField(strategy, "randomSupplier", mockSupplier);
        
        // 50% chance
        JackpotRewardConfiguration config = new JackpotRewardConfiguration();
        config.setRewardProbabilityPct(new BigDecimal("50.0"));
        
        // 0.51 > 0.50 -> loser
        boolean result = strategy.evaluateReward(config, BigDecimal.ZERO);
        assertFalse(result);
    }
}
