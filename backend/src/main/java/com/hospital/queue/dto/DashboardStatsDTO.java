package com.hospital.queue.dto;

import lombok.*;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardStatsDTO {
    private Integer totalPatientsToday;
    private Integer totalWaiting;
    private Integer totalInConsultation;
    private Integer totalCompleted;
    private Integer totalCancelled;
    private Integer averageWaitTime;
    private Map<String, Integer> departmentWiseCount;
    private Map<String, Integer> statusWiseCount;
}
