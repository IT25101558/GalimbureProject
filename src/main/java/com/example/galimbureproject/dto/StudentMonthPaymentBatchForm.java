package com.example.galimbureproject.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.ArrayList;
import java.util.List;

public class StudentMonthPaymentBatchForm {

    @NotNull(message = "Select a month.")
    private Long monthPlanId;

    @Valid
    private List<StudentMonthPaymentEntryForm> entries = new ArrayList<>();

    public Long getMonthPlanId() {
        return monthPlanId;
    }

    public void setMonthPlanId(Long monthPlanId) {
        this.monthPlanId = monthPlanId;
    }

    public List<StudentMonthPaymentEntryForm> getEntries() {
        return entries;
    }

    public void setEntries(List<StudentMonthPaymentEntryForm> entries) {
        this.entries = entries;
    }
}
