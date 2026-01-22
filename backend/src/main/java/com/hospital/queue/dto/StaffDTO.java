package com.hospital.queue.dto;

import com.hospital.queue.entity.Staff;
import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StaffDTO {
    private Long id;
    private String employeeId;

    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    private String fullName;

    @Email(message = "Invalid email format")
    private String email;

    private String phone;

    @NotNull(message = "Role is required")
    private Staff.Role role;

    private Long departmentId;
    private String departmentName;
    private Long doctorId;
    private String doctorName;
    private Boolean isActive;
}
