package com.jackpot.service.impl;

import com.jackpot.dto.BetRequestDto;
import com.jackpot.repository.JackpotContributionRepository;
import com.jackpot.repository.JackpotRepository;
import com.jackpot.service.BetService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Service
@RequiredArgsConstructor
public class BetServiceImpl implements BetService {

    private static final Logger log = LoggerFactory.getLogger(BetServiceImpl.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;

    private final JackpotRepository jackpotRepository;
    private final JackpotContributionRepository jackpotContributionRepository;

    @Value("${jackpot-bets.kafka.topic:jackpot-bets}")
    private String topicName;

    @Override
    public void processBet(BetRequestDto betRequestDto) throws RuntimeException {
        log.info("Processing bet request: {}", betRequestDto);
       
        validateBetRequest(betRequestDto);
        
        try {
            /*
             * ARCHITECTURE NOTE - KAFKA PARTITION KEY:
             * We use `jackpotId` as the Kafka message key to guarantee that all bets for a 
             * specific jackpot are routed to the same partition and processed sequentially. 
             *
             * FUTURE SCALABILITY: 
             * If a single jackpot's traffic exceeds one partition's capacity, change the key 
             * to a composite like `jackpotId + "_" + userId` or a random UUID to distribute load 
             * across all partitions.
             */
            kafkaTemplate.send(topicName, betRequestDto.getJackpotId(), betRequestDto).get(5, TimeUnit.SECONDS);

            log.info("Successfully published bet event for betId: {}", betRequestDto.getBetId());
            
        } catch (TimeoutException e) {
            log.error("Timeout while trying to connect to Kafka for betId: {}", betRequestDto.getBetId(), e);
            throw new RuntimeException("Kafka is down or unreachable. Publishing timed out for betId: " + betRequestDto.getBetId(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Thread interrupted while publishing bet event for betId: {}", betRequestDto.getBetId(), e);
            throw new RuntimeException("Kafka publishing interrupted for betId: " + betRequestDto.getBetId(), e);
        } catch (ExecutionException e) {
            log.error("Execution error while publishing bet event to Kafka for betId: {}", betRequestDto.getBetId(), e.getCause());
            throw new RuntimeException("Failed to publish bet to Kafka: " + e.getCause().getMessage(), e.getCause());
        } catch (Exception e) {
            log.error("Unexpected error while publishing bet event to Kafka for betId: {}", betRequestDto.getBetId(), e);
            throw new RuntimeException("Failed to publish bet event to Kafka: " + e.getMessage(), e);
        }
    }
    private void validateBetRequest(BetRequestDto request) {

        if (request == null) {
            throw new IllegalArgumentException("Bet request cannot be null");
        }

        if (!jackpotRepository.existsById(request.getJackpotId())) {
            log.error("Jackpot not found: {}", request.getJackpotId());
            throw new IllegalArgumentException("Jackpot not found: " + request.getJackpotId());
        }

        // Validate if duplicate bet is present
        if (jackpotContributionRepository.existsByBetIdAndUserIdAndJackpot_Id(request.getBetId(), request.getUserId(), request.getJackpotId())) {
            log.error("Bet ID {} already present.", request.getBetId());
            throw new IllegalArgumentException("Bet ID already present : " + request.getBetId());
        }
    }
}
