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
import jakarta.persistence.Transient;

import java.time.Month;
import java.time.OffsetDateTime;
import java.time.format.TextStyle;
import java.util.Locale;

@Entity
@Table(
        name = "monthplan_table",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_monthplan_table_year_month_number",
                columnNames = {"year_plan_id", "month_number"}
        )
)
public class MonthPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "year_plan_id", nullable = false)
    private YearPlan yearPlan;

    @Column(name = "month_number", nullable = false)
    private Integer monthNumber;

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

    public YearPlan getYearPlan() {
        return yearPlan;
    }

    public void setYearPlan(YearPlan yearPlan) {
        this.yearPlan = yearPlan;
    }

    public Integer getMonthNumber() {
        return monthNumber;
    }

    public void setMonthNumber(Integer monthNumber) {
        this.monthNumber = monthNumber;
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
        return yearPlan != null ? yearPlan.getBatch() : null;
    }

    @Transient
    public Integer getYearValue() {
        return yearPlan != null ? yearPlan.getYearValue() : null;
    }

    public String getMonthLabel() {
        if (monthNumber == null) {
            return "-";
        }

        Month month = Month.of(monthNumber);
        return month.getDisplayName(TextStyle.FULL, Locale.ENGLISH);
    }

    public String getDisplayLabel() {
        String batchLabel = getBatch() != null ? getBatch().getCompactLabel() : null;
        Integer yearValue = getYearValue();
        if (batchLabel == null && yearValue == null) {
            return getMonthLabel();
        }

        StringBuilder label = new StringBuilder();
        if (batchLabel != null) {
            label.append(batchLabel).append(" / ");
        }
        if (yearValue != null) {
            label.append(yearValue).append(" / ");
        }
        label.append(getMonthLabel());
        return label.toString();
    }
}
