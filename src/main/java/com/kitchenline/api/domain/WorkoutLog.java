package com.kitchenline.api.domain;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "workout_logs",
        uniqueConstraints = @UniqueConstraint(columnNames = {"program_month_id", "week_number", "day_label"}))
public class WorkoutLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "program_month_id", nullable = false)
    private ProgramMonth programMonth;

    @Column(name = "week_number", nullable = false)
    private int weekNumber;

    @Column(name = "day_label", nullable = false, length = 1)
    private String dayLabel;

    @Column(name = "completed_at", nullable = false, updatable = false)
    private Instant completedAt;

    protected WorkoutLog() {
        // Required by JPA
    }

    public WorkoutLog(ProgramMonth programMonth, int weekNumber, String dayLabel) {
        if (weekNumber < 1 || weekNumber > 4) throw new IllegalArgumentException("Week must be 1-4");
        this.programMonth = programMonth;
        this.weekNumber = weekNumber;
        this.dayLabel = dayLabel;
    }

    @PrePersist
    void onCreate() {
        completedAt = Instant.now();
    }

    public Long getId() { return id; }
    public ProgramMonth getProgramMonth() { return programMonth; }
    public int getWeekNumber() { return weekNumber; }
    public String getDayLabel() { return dayLabel; }
    public Instant getCompletedAt() { return completedAt; }
}