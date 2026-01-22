package com.hospital.queue.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "queue_status")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QueueStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id")
    private Doctor doctor;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "current_token_id")
    private Token currentToken;

    @Column(name = "total_waiting")
    @Builder.Default
    private Integer totalWaiting = 0;

    @Column(name = "average_wait_time_minutes")
    @Builder.Default
    private Integer averageWaitTimeMinutes = 0;

    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;

    @PrePersist
    protected void onCreate() {
        lastUpdated = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        lastUpdated = LocalDateTime.now();
    }
}
