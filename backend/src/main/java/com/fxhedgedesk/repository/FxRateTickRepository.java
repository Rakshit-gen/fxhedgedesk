package com.fxhedgedesk.repository;

import com.fxhedgedesk.domain.FxRateTick;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FxRateTickRepository extends JpaRepository<FxRateTick, java.util.UUID> {
    Optional<FxRateTick> findTopByPairCodeOrderBySimDayDescCreatedAtDesc(String pairCode);

    List<FxRateTick> findByPairCodeOrderBySimDayDescCreatedAtDesc(String pairCode, Pageable pageable);
}
