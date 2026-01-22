package com.hospital.queue.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DoctorDTO {
    private Long id;
    private String employeeId;
    private String firstName;
    private String lastName;
    private String fullName;
    private String specialization;
    private String qualification;
    private String email;
    private String phone;
    private Long departmentId;
    private String departmentName;
    private String departmentCode;
    private String roomNumber;
    private Integer consultationDurationMinutes;
    private Integer maxPatientsPerDay;
    private Boolean isAvailable;
    private Boolean isActive;
    private Integer currentQueueLength;
    private Integer estimatedWaitTime;
}
