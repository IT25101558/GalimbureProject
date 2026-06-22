package com.example.galimbureproject.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public class WeekPlanForm {

    @NotNull(message = "Batch is required.")
    private Long batchId;

    @NotNull(message = "Week number is required.")
    @Min(value = 1, message = "Week number must be at least 1.")
    @Max(value = 52, message = "Week number must be 52 or less.")
    private Integer weekNumber;

    @NotBlank(message = "Task is required.")
    @Size(max = 250, message = "Task must be 250 characters or fewer.")
    private String task;

    @NotNull(message = "Week start date is required.")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate weekStartDate;

    @NotNull(message = "Week end date is required.")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate weekEndDate;

    public Long getBatchId() {
        return batchId;
    }

    public void setBatchId(Long batchId) {
        this.batchId = batchId;
    }

    public Integer getWeekNumber() {
        return weekNumber;
    }

    public void setWeekNumber(Integer weekNumber) {
        this.weekNumber = weekNumber;
    }

    public String getTask() {
        return task;
    }

    public void setTask(String task) {
        this.task = task;
    }

    public LocalDate getWeekStartDate() {
        return weekStartDate;
    }

    public void setWeekStartDate(LocalDate weekStartDate) {
        this.weekStartDate = weekStartDate;
    }

    public LocalDate getWeekEndDate() {
        return weekEndDate;
    }

    public void setWeekEndDate(LocalDate weekEndDate) {
        this.weekEndDate = weekEndDate;
    }

    public boolean isDateRangeValid() {
        return weekStartDate == null || weekEndDate == null || !weekEndDate.isBefore(weekStartDate);
    }
}
