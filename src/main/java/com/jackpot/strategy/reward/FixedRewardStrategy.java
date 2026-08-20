package com.jackpot.strategy.reward;

import com.jackpot.entity.JackpotRewardConfiguration;
import com.jackpot.enums.StrategyType;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.math.BigDecimal;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.DoubleSupplier;

@Component
public class FixedRewardStrategy implements RewardStrategy {
    private static final Logger log = LoggerFactory.getLogger(FixedRewardStrategy.class);
    
    // Extracted for testability while avoiding Random instance contention
    private DoubleSupplier randomSupplier = () -> ThreadLocalRandom.current().nextDouble();

    @Override
    public StrategyType getStrategyType() {
        return StrategyType.FIXED;
    }

    /**
     * Evaluates if a bet wins the jackpot using a fixed probability.
     *
     * @param config The reward configuration containing the fixed win probability percentage
     * @param currentPoolAmount The current amount in the jackpot pool (unused in fixed strategy)
     * @return true if the bet is a winner, false otherwise
     */
    @Override
    public boolean evaluateReward(JackpotRewardConfiguration config, BigDecimal currentPoolAmount) {
        log.info("Evaluating reward using FIXED strategy.");
        
        double probability = config.getRewardProbabilityPct().doubleValue() / 100.0;
        log.debug("Fixed win probability retrieved from config: {}", probability);
        
        boolean isWinner = randomSupplier.getAsDouble() < probability;
        log.info("FIXED strategy evaluation result - Is Winner: {}", isWinner);
        
        return isWinner;
    }
}
