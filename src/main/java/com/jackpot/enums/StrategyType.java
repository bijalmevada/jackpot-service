package com.jackpot.enums;

import lombok.Getter;

@Getter
public enum StrategyType {

    FIXED("Fixed Percentage"),
    VARIABLE("Variable Percentage");

    private final String value;

    StrategyType(String value) {
        this.value = value;
    }

}