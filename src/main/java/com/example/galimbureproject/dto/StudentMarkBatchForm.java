package com.example.galimbureproject.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.ArrayList;
import java.util.List;

public class StudentMarkBatchForm {

    @NotNull(message = "Select a week plan.")
    private Long weekPlanId;

    @Valid
    private List<StudentMarkEntryForm> entries = new ArrayList<>();

    public Long getWeekPlanId() {
        return weekPlanId;
    }

    public void setWeekPlanId(Long weekPlanId) {
        this.weekPlanId = weekPlanId;
    }

    public List<StudentMarkEntryForm> getEntries() {
        return entries;
    }

    public void setEntries(List<StudentMarkEntryForm> entries) {
        this.entries = entries;
    }
}
