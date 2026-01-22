package com.hospital.queue.dto;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QueueStatusDTO {
    private Long id;
    private Long departmentId;
    private String departmentName;
    private Long doctorId;
    private String doctorName;
    private String roomNumber;
    private TokenDTO currentToken;
    private Integer totalWaiting;
    private Integer averageWaitTimeMinutes;
    private LocalDateTime lastUpdated;
    private List<TokenDTO> waitingTokens;
}
