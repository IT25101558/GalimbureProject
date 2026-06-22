package com.example.galimbureproject.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public class BatchForm {

    @NotNull(message = "Batch year is required.")
    @Min(value = 1900, message = "Batch year must be valid.")
    @Max(value = 2100, message = "Batch year must be valid.")
    private Integer batchYear;

    @NotBlank(message = "Place is required.")
    @Size(max = 150, message = "Place must be 150 characters or fewer.")
    private String place;

    @NotNull(message = "Date is required.")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate batchDate;

    public Integer getBatchYear() {
        return batchYear;
    }

    public void setBatchYear(Integer batchYear) {
        this.batchYear = batchYear;
    }

    public String getPlace() {
        return place;
    }

    public void setPlace(String place) {
        this.place = place;
    }

    public LocalDate getBatchDate() {
        return batchDate;
    }

    public void setBatchDate(LocalDate batchDate) {
        this.batchDate = batchDate;
    }
}
