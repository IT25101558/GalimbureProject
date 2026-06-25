package com.example.galimbureproject.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
        name = "batch_table",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_batch_table_batch_year_place",
                columnNames = {"batch_year", "place"}
        )
)
public class Batch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "batch_year", nullable = false)
    private Integer batchYear;

    @Column(nullable = false, length = 150)
    private String place;

    @Column(name = "batch_date", nullable = false, length = 255)
    private String batchDate;

    public Long getId() {
        return id;
    }

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

    public String getBatchDate() {
        return batchDate;
    }

    public void setBatchDate(String batchDate) {
        this.batchDate = batchDate;
    }

    public String getCompactLabel() {
        if (batchYear == null && (place == null || place.isBlank())) {
            return "-";
        }

        if (batchYear == null) {
            return place;
        }

        if (place == null || place.isBlank()) {
            return String.valueOf(batchYear);
        }

        return batchYear + " - " + place;
    }

    public String getDisplayLabel() {
        String compactLabel = getCompactLabel();
        if (batchDate == null || batchDate.isBlank()) {
            return compactLabel;
        }

        return compactLabel + " - " + batchDate.trim();
    }
}
