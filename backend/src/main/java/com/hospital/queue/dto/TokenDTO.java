package com.hospital.queue.dto;

import com.hospital.queue.entity.Token;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TokenDTO {
    private Long id;
    private String tokenNumber;

    @NotNull(message = "Patient ID is required")
    private Long patientId;
    private String patientName;
    private String patientPhone;

    @NotNull(message = "Department ID is required")
    private Long departmentId;
    private String departmentName;
    private String departmentCode;

    private Long doctorId;
    private String doctorName;
    private String roomNumber;

    private LocalDate tokenDate;
    private Token.Priority priority;
    private Integer priorityScore;
    private Token.Status status;

    private LocalDateTime generatedAt;
    private LocalDateTime calledAt;
    private LocalDateTime consultationStartedAt;
    private LocalDateTime consultationEndedAt;

    private String notes;

    // Queue information
    private Integer queuePosition;
    private Integer estimatedWaitMinutes;
    private Integer patientsAhead;
}
