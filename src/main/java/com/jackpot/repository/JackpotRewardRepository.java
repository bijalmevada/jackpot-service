package com.jackpot.repository;

import com.jackpot.entity.JackpotReward;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface JackpotRewardRepository extends JpaRepository<JackpotReward, Long> {
    Optional<JackpotReward> findByBetIdAndUserIdAndJackpot_Id(String betId, String userId, String jackpotId);

}


