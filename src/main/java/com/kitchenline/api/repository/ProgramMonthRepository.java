package com.kitchenline.api.repository;

import com.kitchenline.api.domain.ProgramMonth;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProgramMonthRepository extends JpaRepository<ProgramMonth, Long> {

    // The user's current month = their highest month number
    Optional<ProgramMonth> findTopByUserIdOrderByMonthNumberDesc(Long userId);

    Optional<ProgramMonth> findByUserIdAndMonthNumber(Long userId, int monthNumber);

    // Full history, Month 1 first
    List<ProgramMonth> findByUserIdOrderByMonthNumberAsc(Long userId);
}