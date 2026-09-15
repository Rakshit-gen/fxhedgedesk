package com.fxhedgedesk.repository;

import com.fxhedgedesk.domain.AppUser;
import com.fxhedgedesk.domain.Exposure;
import com.fxhedgedesk.domain.ForwardContract;
import com.fxhedgedesk.domain.ForwardStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ForwardContractRepository extends JpaRepository<ForwardContract, UUID> {
    List<ForwardContract> findByUserOrderByCreatedAtDesc(AppUser user);

    List<ForwardContract> findByExposure(Exposure exposure);

    List<ForwardContract> findByStatusAndSettlementSimDayLessThanEqual(ForwardStatus status, long simDay);
}
