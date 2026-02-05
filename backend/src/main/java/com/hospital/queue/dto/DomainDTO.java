package com.hospital.queue.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DomainDTO {
    private Long id;
    private String name;
    private String description;
    private String icon;
    private Boolean active;
}
