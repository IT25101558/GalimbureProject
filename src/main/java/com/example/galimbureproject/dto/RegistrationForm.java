package com.example.galimbureproject.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class RegistrationForm {

    @NotBlank(message = "Full name is required.")
    @Size(max = 100, message = "Full name must be 100 characters or fewer.")
    private String fullName;

    @NotBlank(message = "Email is required.")
    @Email(message = "Enter a valid email address.")
    @Size(max = 150, message = "Email must be 150 characters or fewer.")
    private String email;

    @NotBlank(message = "Phone number is required.")
    @Pattern(regexp = "^[0-9+()\\-\\s]{7,20}$", message = "Enter a valid phone number.")
    private String phone;

    @NotBlank(message = "Address is required.")
    @Size(max = 300, message = "Address must be 300 characters or fewer.")
    private String address;

    @NotNull(message = "Batch year is required.")
    @Min(value = 1900, message = "Batch year must be valid.")
    @Max(value = 2100, message = "Batch year must be valid.")
    private Integer batchYear;

    @NotBlank(message = "Password is required.")
    @Size(min = 8, max = 72, message = "Password must be between 8 and 72 characters.")
    private String password;

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Integer getBatchYear() {
        return batchYear;
    }

    public void setBatchYear(Integer batchYear) {
        this.batchYear = batchYear;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
