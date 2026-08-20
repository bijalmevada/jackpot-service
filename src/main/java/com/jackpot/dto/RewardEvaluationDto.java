package com.jackpot.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RewardEvaluationDto {

    @NotBlank(message = "Bet ID cannot be empty")
    private String betId;

    @NotBlank(message = "User ID cannot be empty")
    private String userId;

    @NotBlank(message = "Jackpot ID cannot be empty")
    private String jackpotId;
}