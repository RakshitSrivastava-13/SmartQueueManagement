package com.hospital.queue.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "tokens", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"token_number", "token_date"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Token {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "token_number", nullable = false, length = 20)
    private String tokenNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id")
    private Doctor doctor;

    @Column(name = "token_date", nullable = false)
    private LocalDate tokenDate;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    @Builder.Default
    private Priority priority = Priority.NORMAL;

    @Column(name = "priority_score")
    @Builder.Default
    private Integer priorityScore = 0;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    @Builder.Default
    private Status status = Status.WAITING;

    @Column(name = "generated_at")
    private LocalDateTime generatedAt;

    @Column(name = "called_at")
    private LocalDateTime calledAt;

    @Column(name = "consultation_started_at")
    private LocalDateTime consultationStartedAt;

    @Column(name = "consultation_ended_at")
    private LocalDateTime consultationEndedAt;

    @Column(length = 500)
    private String notes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private Staff createdBy;

    @PrePersist
    protected void onCreate() {
        if (generatedAt == null) {
            generatedAt = LocalDateTime.now();
        }
        if (tokenDate == null) {
            tokenDate = LocalDate.now();
        }
        calculatePriorityScore();
    }

    public void calculatePriorityScore() {
        int score = 0;
        switch (priority) {
            case EMERGENCY -> score = 1000;
            case PREGNANT -> score = 800;
            case SENIOR_CITIZEN -> score = 600;
            case VIP -> score = 400;
            case NORMAL -> score = 0;
        }
        this.priorityScore = score;
    }

    public long getConsultationDurationMinutes() {
        if (consultationStartedAt != null && consultationEndedAt != null) {
            return java.time.Duration.between(consultationStartedAt, consultationEndedAt).toMinutes();
        }
        return 0;
    }

    public enum Priority {
        NORMAL, SENIOR_CITIZEN, PREGNANT, EMERGENCY, VIP
    }

    public enum Status {
        WAITING, CALLED, IN_CONSULTATION, COMPLETED, CANCELLED, NO_SHOW
    }
}
