package com.example.financial.repository;

import com.example.financial.model.UsageData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface UsageDataRepository extends JpaRepository<UsageData, Long> {
    Optional<UsageData> findByDeviceIdAndDate(String deviceId, LocalDate date);
}
