package com.jackpot.controller;

import com.jackpot.dto.BetRequestDto;
import com.jackpot.service.BetService;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@AllArgsConstructor
@RequestMapping("/bets")
public class BetController {

    private static final Logger log = LoggerFactory.getLogger(BetController.class);

    private final BetService betService;

    @PostMapping
    public ResponseEntity<?> publishBet(@Valid @RequestBody BetRequestDto betRequestDto, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            String errorMessage = bindingResult.getAllErrors().get(0).getDefaultMessage();
            log.warn("Validation error publishing bet: {}", errorMessage);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMessage);
        }
        log.info("Received request to publish bet: {}", betRequestDto);

        try {
            betService.processBet(betRequestDto);
            log.info("Bet published successfully: {}", betRequestDto.getBetId());
            return ResponseEntity.status(HttpStatus.ACCEPTED).body("Bet published successfully");
        } catch (IllegalArgumentException e) {
            log.warn("Validation error while publishing bet: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error publishing bet: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred");
        }
    }
}
