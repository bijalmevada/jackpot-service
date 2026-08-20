package com.jackpot.strategy.contribution;

import com.jackpot.enums.StrategyType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ContributionStrategyFactoryTest {

    private ContributionStrategyFactory factory;
    private ContributionStrategy fixedStrategyMock;
    private ContributionStrategy variableStrategyMock;

    @BeforeEach
    void setUp() {
        fixedStrategyMock = mock(ContributionStrategy.class);
        variableStrategyMock = mock(ContributionStrategy.class);

        when(fixedStrategyMock.getStrategyType()).thenReturn(StrategyType.FIXED);
        when(variableStrategyMock.getStrategyType()).thenReturn(StrategyType.VARIABLE);

        factory = new ContributionStrategyFactory(Arrays.asList(fixedStrategyMock, variableStrategyMock));
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
