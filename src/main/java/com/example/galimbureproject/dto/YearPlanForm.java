package com.example.galimbureproject.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class YearPlanForm {

    @NotNull(message = "Batch is required.")
    private Long batchId;

    @NotNull(message = "Year is required.")
    @Min(value = 1900, message = "Year must be valid.")
    @Max(value = 2100, message = "Year must be valid.")
    private Integer yearValue;

    public Long getBatchId() {
        return batchId;
    }

    public void setBatchId(Long batchId) {
        this.batchId = batchId;
    }

    public Integer getYearValue() {
        return yearValue;
    }

    public void setYearValue(Integer yearValue) {
        this.yearValue = yearValue;
    }
}
