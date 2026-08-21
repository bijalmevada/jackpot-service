package com.jackpot.controller;

import com.jackpot.dto.JackpotRewardDto;
import com.jackpot.dto.RewardEvaluationDto;

import com.jackpot.service.RewardEvaluationService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@AllArgsConstructor
@RequestMapping("/rewards")
public class RewardController {

    private final RewardEvaluationService rewardEvaluationService;

    private static final Logger log = LoggerFactory.getLogger(RewardController.class);

    @GetMapping("/evaluate")
    public ResponseEntity<?> evaluateReward(@Valid @ModelAttribute RewardEvaluationDto request, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            String errorMessage = bindingResult.getAllErrors().get(0).getDefaultMessage();
            log.warn("Validation error evaluating reward: {}", errorMessage);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMessage);
        }
        log.info("Received reward evaluation request: {}", request);
        try {
            JackpotRewardDto result = rewardEvaluationService.evaluateReward(request);
            log.info("Reward evaluation completed successfully for request: {}", request);
            return ResponseEntity.ok(result);
        } catch (IllegalStateException e) {
            log.warn("Validation error evaluating reward: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error evaluating reward: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred");
        }
    }
}