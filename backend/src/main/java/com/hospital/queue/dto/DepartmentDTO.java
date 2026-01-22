package com.hospital.queue.dto;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DepartmentDTO {
    private Long id;
    private String name;
    private String code;
    private String description;
    private Integer floorNumber;
    private String roomNumbers;
    private Boolean isActive;
    private List<DoctorDTO> doctors;
    private LocalDateTime createdAt;
}
