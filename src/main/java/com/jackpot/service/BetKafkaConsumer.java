package com.jackpot.service;


import com.jackpot.dto.BetRequestDto;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BetKafkaConsumer {

    private static final Logger log = LoggerFactory.getLogger(BetKafkaConsumer.class);

    private final BetTransactionProcessor betTransactionProcessor;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${jackpot-bets.kafka.dlq-topic:jackpot-bets-dlq}")
    private String dlqTopic;


    @KafkaListener(topics = "${jackpot-bets.kafka.topic:jackpot-bets}", groupId = "${spring.kafka.consumer.group-id:jackpot-group}")
    public void consumeBetEvent(BetRequestDto event) {
        log.info("Received Kafka bet event: {}", event);
        try {
            betTransactionProcessor.processBetContributionAndEvaluation(event);
        } catch (Exception e) {
            log.error("Error processing consumed bet event: {}. Sending to DLQ topic: {}", event, dlqTopic, e);
            try {
                kafkaTemplate.send(dlqTopic, event.getBetId(), event);
            } catch (Exception dlqException) {
                log.error("Failed to send event {} to DLQ!", event.getBetId(), dlqException);
            }
        }
    }
}
