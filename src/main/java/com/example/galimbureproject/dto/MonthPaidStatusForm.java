package com.example.galimbureproject.dto;

import jakarta.validation.constraints.NotNull;

public class MonthPaidStatusForm {

    @NotNull(message = "Month is required.")
    private Long monthPlanId;

    private boolean paidStatus;

    public Long getMonthPlanId() {
        return monthPlanId;
    }

    public void setMonthPlanId(Long monthPlanId) {
        this.monthPlanId = monthPlanId;
    }

    public boolean isPaidStatus() {
        return paidStatus;
    }

    public void setPaidStatus(boolean paidStatus) {
        this.paidStatus = paidStatus;
    }
}
