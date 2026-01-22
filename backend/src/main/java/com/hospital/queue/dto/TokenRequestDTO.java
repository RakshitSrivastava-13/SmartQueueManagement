package com.hospital.queue.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TokenRequestDTO {

    @NotNull(message = "Patient ID is required")
    private Long patientId;

    @NotNull(message = "Department ID is required")
    private Long departmentId;

    private Long doctorId;

    private String priority; // NORMAL, SENIOR_CITIZEN, PREGNANT, EMERGENCY, VIP

    private String notes;
}
