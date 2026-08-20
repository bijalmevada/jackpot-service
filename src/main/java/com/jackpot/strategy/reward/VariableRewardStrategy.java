package com.jackpot.strategy.reward;

import com.jackpot.entity.JackpotRewardConfiguration;
import com.jackpot.enums.StrategyType;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.DoubleSupplier;

@Component
public class VariableRewardStrategy implements RewardStrategy {
    private static final Logger log = LoggerFactory.getLogger(VariableRewardStrategy.class);
    
    // Extracted for testability while avoiding Random instance contention
    private DoubleSupplier randomSupplier = () -> ThreadLocalRandom.current().nextDouble();

    @Override
    public StrategyType getStrategyType() {
        return StrategyType.VARIABLE;
    }

    /**
     * Evaluates if a bet wins the jackpot using a variable probability based on the pool size.
     * The probability of winning increases as the pool grows toward the max amount.
     *
     * @param config The reward configuration with min/max amounts and starting probability
     * @param currentPoolAmount The current amount in the jackpot pool
     * @return true if the bet is a winner, false otherwise
     */
    @Override
    public boolean evaluateReward(JackpotRewardConfiguration config, BigDecimal currentPoolAmount) {
        log.info("Evaluating reward using VARIABLE strategy. Current pool: {}", currentPoolAmount);
        
        BigDecimal maxAmount = config.getMaxAmount();
        BigDecimal startAmount = config.getStartAmount();

        if (currentPoolAmount.compareTo(maxAmount) >= 0 || startAmount.compareTo(maxAmount) >= 0) {
            log.info("Pool meets or exceeds max amount. Guaranteed winner.");
            return true;
        }

        BigDecimal effectivePool = currentPoolAmount.max(startAmount);
        BigDecimal poolRange = maxAmount.subtract(startAmount);
        BigDecimal progress = effectivePool.subtract(startAmount)
                .divide(poolRange, 8, RoundingMode.HALF_UP);
                
        log.debug("Calculated pool progress: {}", progress);
        
        BigDecimal probRange = BigDecimal.valueOf(100).subtract(config.getRewardProbabilityPct());
        BigDecimal effectiveProbabilityPct = config.getRewardProbabilityPct().add(progress.multiply(probRange));

        double probability = effectiveProbabilityPct.doubleValue() / 100.0;
        log.info("Calculated dynamic win probability: {}", probability);
        
        boolean isWinner = randomSupplier.getAsDouble() < probability;
        log.info("VARIABLE strategy evaluation result - Is Winner: {}", isWinner);
        
        return isWinner;
    }
}
