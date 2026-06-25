package com.example.galimbureproject.dto;

import jakarta.validation.constraints.NotNull;

public class StudentMonthPaymentEntryForm {

    @NotNull(message = "Student is required.")
    private Long studentId;

    private boolean paidStatus;

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public boolean isPaidStatus() {
        return paidStatus;
    }

    public void setPaidStatus(boolean paidStatus) {
        this.paidStatus = paidStatus;
    }
}
