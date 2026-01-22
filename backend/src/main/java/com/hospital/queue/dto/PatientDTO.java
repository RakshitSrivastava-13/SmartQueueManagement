package com.hospital.queue.dto;

import com.hospital.queue.entity.Patient;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PatientDTO {
    private Long id;
    private String patientId;

    @NotBlank(message = "First name is required")
    @Size(max = 50, message = "First name cannot exceed 50 characters")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 50, message = "Last name cannot exceed 50 characters")
    private String lastName;

    private String fullName;

    private LocalDate dateOfBirth;

    private Patient.Gender gender;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^[0-9]{10}$", message = "Phone number must be 10 digits")
    private String phone;

    @Email(message = "Invalid email format")
    private String email;

    private String address;
    private String city;
    private String state;
    private String pincode;
    private String bloodGroup;
    private String emergencyContactName;
    private String emergencyContactPhone;
    private Boolean isSeniorCitizen;
    private Boolean isPregnant;
}
