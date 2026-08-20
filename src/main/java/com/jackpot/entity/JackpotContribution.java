package com.jackpot.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.FetchType;
import jakarta.persistence.Table;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "jackpot_contribution")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class JackpotContribution {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "bet_id", nullable = false, length = 64)
    private String betId;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "jackpot_id", nullable = false)
    private Jackpot jackpot;

    @Column(name = "stake_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal stakeAmount;

    @Column(name = "contribution_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal contributionAmount;

    @Column(name = "current_jackpot_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal currentJackpotAmount;

    @Column(name = "created_at_date", nullable = false, updatable = false)
    private LocalDateTime createdAtDate;
}
