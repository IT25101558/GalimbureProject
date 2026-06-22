package com.example.galimbureproject.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.OffsetDateTime;

@Entity
@Table(
        name = "student_marks",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_student_marks_student_week_plan",
                columnNames = {"student_id", "week_plan_id"}
        )
)
public class StudentMark {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private RegisteredUser student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "week_plan_id")
    private WeekPlan weekPlan;

    @Column(nullable = false)
    private Integer mark;

    @Column(nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(nullable = false)
    private OffsetDateTime updatedAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = OffsetDateTime.now();
        }

        updatedAt = createdAt;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public RegisteredUser getStudent() {
        return student;
    }

    public void setStudent(RegisteredUser student) {
        this.student = student;
    }

    public Integer getWeekNumber() {
        return weekPlan != null ? weekPlan.getWeekNumber() : null;
    }

    public WeekPlan getWeekPlan() {
        return weekPlan;
    }

    public void setWeekPlan(WeekPlan weekPlan) {
        this.weekPlan = weekPlan;
    }

    public Integer getMark() {
        return mark;
    }

    public void setMark(Integer mark) {
        this.mark = mark;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }
}
