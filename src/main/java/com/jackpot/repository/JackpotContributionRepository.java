package com.jackpot.repository;

import com.jackpot.entity.JackpotContribution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface JackpotContributionRepository extends JpaRepository<JackpotContribution, Long> {

    boolean existsByBetIdAndUserIdAndJackpot_Id(String betId, String userId, String jackpotId);
}