package com.jackpot.entity;

import com.jackpot.enums.StrategyType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "jackpot_reward_configuration")
@Getter
@Setter@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JackpotRewardConfiguration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "jackpot_id", nullable = false, unique = true)
    private Jackpot jackpot;

    @Enumerated(EnumType.STRING)
    @Column(name = "strategy_type", nullable = false, length = 32)
    private StrategyType strategyType;

    @Column(name = "start_amount", precision = 15, scale = 2)
    private BigDecimal startAmount;

    @Column(name = "max_amount", precision = 15, scale = 2)
    private BigDecimal maxAmount;

    @Column(name = "reward_probability_pct", precision = 5, scale = 2)
    private BigDecimal rewardProbabilityPct;

}
