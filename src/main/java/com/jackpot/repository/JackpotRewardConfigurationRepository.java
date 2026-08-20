package com.jackpot.repository;

import com.jackpot.entity.JackpotRewardConfiguration;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import org.springframework.cache.annotation.Cacheable;

@Repository
public interface JackpotRewardConfigurationRepository extends JpaRepository<JackpotRewardConfiguration, Long> {


    /*
     * Assumption : jackpot configuration is considered immutable considering the previous bets calculation can be out of the place.
     *              Hence, there is no TTL added for cache.
     *  If it is bound to change then at the time of update we need to evict this cache.
     * */
    @Cacheable("rewardConfigs")
    @EntityGraph(attributePaths = {"jackpot"})
    Optional<JackpotRewardConfiguration> findByJackpot_Id(String jackpotId);
}
