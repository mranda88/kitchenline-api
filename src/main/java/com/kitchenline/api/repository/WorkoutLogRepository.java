package com.kitchenline.api.repository;

import com.kitchenline.api.domain.WorkoutLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WorkoutLogRepository extends JpaRepository<WorkoutLog, Long> {

    List<WorkoutLog> findByProgramMonthId(Long programMonthId);

    // For the progress bar: how many workouts done this month
    long countByProgramMonthId(Long programMonthId);

    boolean existsByProgramMonthIdAndWeekNumberAndDayLabel(Long programMonthId, int weekNumber, String dayLabel);

    // For un-checking a workout marked done by mistake
    long deleteByProgramMonthIdAndWeekNumberAndDayLabel(Long programMonthId, int weekNumber, String dayLabel);
}