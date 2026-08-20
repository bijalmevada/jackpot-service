package com.jackpot.service;

import com.jackpot.dto.BetRequestDto;

public interface BetService {

    /**
     * Validates and publishes a bet request event to Kafka.
     *
     * @param betRequestDto the bet request payload
     * @throws IllegalArgumentException if validation fails
     * @throws RuntimeException if publishing to Kafka fails
     */
    void processBet(BetRequestDto betRequestDto);
}
