package com.example.galimbureproject.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class StudentMarkEntryForm {

    @NotNull(message = "Student is required.")
    private Long studentId;

    @Min(value = 0, message = "Mark must be at least 0.")
    @Max(value = 100, message = "Mark must be 100 or less.")
    private Integer mark;

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public Integer getMark() {
        return mark;
    }

    public void setMark(Integer mark) {
        this.mark = mark;
    }
}
