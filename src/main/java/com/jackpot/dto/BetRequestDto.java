package com.jackpot.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BetRequestDto {

    @NotBlank(message = "Bet ID cannot be empty")
    private String betId;

    @NotBlank(message = "User ID cannot be empty")
    private String userId;

    @NotBlank(message = "Jackpot ID cannot be empty")
    private String jackpotId;

    @NotNull(message = "Bet amount cannot be null")
    @Positive(message = "Bet amount must be greater than equal to one")
    @DecimalMin("1")
    private BigDecimal betAmount;
}
