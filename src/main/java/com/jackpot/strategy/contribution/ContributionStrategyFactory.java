package com.jackpot.strategy.contribution;

import com.jackpot.enums.StrategyType;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class ContributionStrategyFactory {
    private final Map<StrategyType, ContributionStrategy> strategyMap;

    public ContributionStrategyFactory(List<ContributionStrategy> strategies) {
        this.strategyMap = strategies.stream()
                .collect(Collectors.toMap(ContributionStrategy::getStrategyType, Function.identity()));
    }

    public ContributionStrategy getStrategy(StrategyType type) {
        ContributionStrategy strategy = strategyMap.get(type);
        if (strategy == null) {
            throw new IllegalArgumentException("Unknown contribution strategy type: " + type);
        }
        return strategy;
    }
}
