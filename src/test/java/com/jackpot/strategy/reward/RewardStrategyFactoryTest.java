package com.jackpot.strategy.reward;

import com.jackpot.enums.StrategyType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RewardStrategyFactoryTest {

    private RewardStrategyFactory factory;
    private RewardStrategy fixedStrategyMock;
    private RewardStrategy variableStrategyMock;

    @BeforeEach
    void setUp() {
        fixedStrategyMock = mock(RewardStrategy.class);
        variableStrategyMock = mock(RewardStrategy.class);

        when(fixedStrategyMock.getStrategyType()).thenReturn(StrategyType.FIXED);
        when(variableStrategyMock.getStrategyType()).thenReturn(StrategyType.VARIABLE);

        factory = new RewardStrategyFactory(Arrays.asList(fixedStrategyMock, variableStrategyMock));
    }

    @Test
    void getStrategy_ReturnsCorrectStrategy() {
        assertEquals(fixedStrategyMock, factory.getStrategy(StrategyType.FIXED));
        assertEquals(variableStrategyMock, factory.getStrategy(StrategyType.VARIABLE));
    }

    @Test
    void getStrategy_Null_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> factory.getStrategy(null));
    }
}
