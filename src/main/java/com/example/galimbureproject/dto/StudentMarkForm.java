package com.example.galimbureproject.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class StudentMarkForm {

    @NotNull(message = "Select a student.")
    private Long studentId;

    @NotNull(message = "Select a week plan.")
    private Long weekPlanId;

    @NotNull(message = "Mark is required.")
    @Min(value = 0, message = "Mark must be at least 0.")
    @Max(value = 100, message = "Mark must be 100 or less.")
    private Integer mark;

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public Long getWeekPlanId() {
        return weekPlanId;
    }

    public void setWeekPlanId(Long weekPlanId) {
        this.weekPlanId = weekPlanId;
    }

    public Integer getMark() {
        return mark;
    }

    public void setMark(Integer mark) {
        this.mark = mark;
    }
}
