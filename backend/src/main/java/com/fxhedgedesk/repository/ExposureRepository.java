package com.fxhedgedesk.repository;

import com.fxhedgedesk.domain.AppUser;
import com.fxhedgedesk.domain.Exposure;
import com.fxhedgedesk.domain.ExposureStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ExposureRepository extends JpaRepository<Exposure, UUID> {
    List<Exposure> findByUserOrderByDueSimDayAsc(AppUser user);

    List<Exposure> findByStatusNotAndDueSimDayLessThanEqual(ExposureStatus status, long simDay);
}
