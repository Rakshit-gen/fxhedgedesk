package com.fxhedgedesk.repository;

import com.fxhedgedesk.domain.CurrencyPair;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CurrencyPairRepository extends JpaRepository<CurrencyPair, String> {
}
