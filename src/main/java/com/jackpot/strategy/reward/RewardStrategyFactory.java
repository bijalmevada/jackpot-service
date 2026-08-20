package com.jackpot.strategy.reward;

import com.jackpot.enums.StrategyType;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class RewardStrategyFactory {
    private final Map<StrategyType, RewardStrategy> strategyMap;

    public RewardStrategyFactory(List<RewardStrategy> strategies) {
        this.strategyMap = strategies.stream()
                .collect(Collectors.toMap(RewardStrategy::getStrategyType, Function.identity()));
    }

    public RewardStrategy getStrategy(StrategyType type) {
        RewardStrategy strategy = strategyMap.get(type);
        if (strategy == null) {
            throw new IllegalArgumentException("Unknown reward strategy type: " + type);
        }
        return strategy;
    }
}
