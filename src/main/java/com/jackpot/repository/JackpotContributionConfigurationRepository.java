package com.jackpot.repository;

import com.jackpot.entity.JackpotContributionConfiguration;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import org.springframework.cache.annotation.Cacheable;

@Repository
public interface JackpotContributionConfigurationRepository extends JpaRepository<JackpotContributionConfiguration, Long> {

    /*
    * Assumption : jackpot configuration is considered immutable considering the previous bets calculation can be out of the place.
    *              Hence, there is no TTL added for cache.
    *  If it is bound to change then at the time of update we need to evict this cache.
    * */
    @Cacheable("contributionConfigs")
    @EntityGraph(attributePaths = {"jackpot"})
    Optional<JackpotContributionConfiguration> findByJackpot_Id(String jackpotId);

}

