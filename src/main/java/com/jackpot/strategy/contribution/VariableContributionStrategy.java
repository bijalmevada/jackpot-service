package com.jackpot.strategy.contribution;

import com.jackpot.entity.JackpotContributionConfiguration;
import com.jackpot.enums.StrategyType;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class VariableContributionStrategy implements ContributionStrategy {
    private static final Logger log = LoggerFactory.getLogger(VariableContributionStrategy.class);

    @Override
    public StrategyType getStrategyType() {
        return StrategyType.VARIABLE;
    }


    /**
     * Calculates the contribution to the jackpot using a variable percentage based on the pool size.
     * As the pool grows towards the max amount, the contribution percentage decreases linearly.
     *
     * @param config The contribution configuration with min/max amounts and percentages
     * @param stakeAmount The amount given by the user
     * @param currentAmount The current amount in the jackpot pool
     * @return The calculated contribution amount to be added to the jackpot
     */
    @Override
    public BigDecimal calculateContribution(JackpotContributionConfiguration config, BigDecimal stakeAmount, BigDecimal currentAmount) {
        log.info("Calculating contribution using VARIABLE strategy. Stake amount: {}, Current pool: {}", stakeAmount, currentAmount);
        
        BigDecimal maxAmount = config.getMaxAmount();
        BigDecimal startAmount = config.getStartAmount();

        BigDecimal effectivePool = currentAmount.max(startAmount);
        BigDecimal startPct = config.getContributionPct();
        BigDecimal minPct = config.getMinContributionPct();
        
        BigDecimal effectivePct;

        if (effectivePool.compareTo(maxAmount) >= 0 || startAmount.compareTo(maxAmount) >= 0) {
            log.debug("Pool exceeds or meets max amount. Applying minimum contribution percentage.");
            effectivePct = config.getMinContributionPct();
        } else {
            /*
             * Formula used :
             * progress = (currentPool - initialPool) / (maxPool - initialPool)
             * contribution_pct = maxContribution - progress * (maxContribution - minContribution)
             * Final contribution =  (bet_amount) * (contribution_pct)
             * */
            BigDecimal poolRange = maxAmount.subtract(startAmount);
            BigDecimal currentProgress = effectivePool.subtract(startAmount)
                    .divide(poolRange, 8, RoundingMode.HALF_UP);

            BigDecimal pctRange = startPct.subtract(minPct);
            effectivePct = startPct.subtract(currentProgress.multiply(pctRange));
            log.debug("Calculated dynamic contribution percentage based on pool progress.");
        }
        
        log.info("Effective contribution percentage: {}%", effectivePct);
        
        BigDecimal contribution = stakeAmount.multiply(effectivePct)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                
        log.info("Calculated VARIABLE contribution amount: {}", contribution);
        return contribution;
    }
}
