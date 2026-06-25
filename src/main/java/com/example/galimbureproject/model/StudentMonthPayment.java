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
import jakarta.persistence.Transient;
import jakarta.persistence.UniqueConstraint;

import java.time.OffsetDateTime;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.Locale;

@Entity
@Table(
        name = "student_month_payments",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_student_month_payments_student_month",
                columnNames = {"student_id", "month_plan_id"}
        )
)
public class StudentMonthPayment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private RegisteredUser student;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "month_plan_id", nullable = false)
    private MonthPlan monthPlan;

    @Column(name = "paid_status", nullable = false)
    private boolean paidStatus;

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

    public MonthPlan getMonthPlan() {
        return monthPlan;
    }

    public void setMonthPlan(MonthPlan monthPlan) {
        this.monthPlan = monthPlan;
    }

    public boolean isPaidStatus() {
        return paidStatus;
    }

    public void setPaidStatus(boolean paidStatus) {
        this.paidStatus = paidStatus;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    @Transient
    public Batch getBatch() {
        return monthPlan != null ? monthPlan.getBatch() : null;
    }

    @Transient
    public YearPlan getYearPlan() {
        return monthPlan != null ? monthPlan.getYearPlan() : null;
    }

    @Transient
    public Integer getYearValue() {
        return monthPlan != null ? monthPlan.getYearValue() : null;
    }

    @Transient
    public Integer getMonthNumber() {
        return monthPlan != null ? monthPlan.getMonthNumber() : null;
    }

    @Transient
    public String getMonthLabel() {
        Integer monthNumber = getMonthNumber();
        if (monthNumber == null) {
            return "-";
        }

        return Month.of(monthNumber).getDisplayName(TextStyle.FULL, Locale.ENGLISH);
    }

    @Transient
    public String getDisplayLabel() {
        Batch batch = getBatch();
        Integer yearValue = getYearValue();
        StringBuilder label = new StringBuilder();

        if (batch != null) {
            label.append(batch.getCompactLabel()).append(" / ");
        }

        if (yearValue != null) {
            label.append(yearValue).append(" / ");
        }

        label.append(getMonthLabel());
        return label.toString();
    }
}
