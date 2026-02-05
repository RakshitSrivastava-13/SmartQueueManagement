package com.hospital.queue.service;

import com.hospital.queue.dto.DashboardStatsDTO;
import com.hospital.queue.dto.TokenDTO;
import com.hospital.queue.entity.Token;
import com.hospital.queue.exception.BadRequestException;
import com.hospital.queue.exception.ResourceNotFoundException;
import com.hospital.queue.repository.DoctorRepository;
import com.hospital.queue.repository.QueueStatusRepository;
import com.hospital.queue.repository.TokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StaffService {

    private final TokenRepository tokenRepository;
    private final DoctorRepository doctorRepository;
    private final QueueStatusRepository queueStatusRepository;
    private final EmailService emailService;
    private final QueueNotificationService queueNotificationService;

    @Transactional
    public TokenDTO callNextPatient(Long doctorId) {
        // Find next waiting patient
        Token nextToken = tokenRepository.findNextWaitingTokenByDoctor(doctorId, LocalDate.now())
                .orElseThrow(() -> new BadRequestException("No patients waiting in queue"));

        // Check if there's a patient currently being served
        var currentToken = tokenRepository.findCurrentTokenByDoctor(doctorId, LocalDate.now());
        if (currentToken.isPresent()) {
            throw new BadRequestException("Please end current consultation before calling next patient");
        }

        // Update token status
        nextToken.setStatus(Token.Status.CALLED);
        nextToken.setCalledAt(LocalDateTime.now());
        nextToken = tokenRepository.save(nextToken);

        updateQueueStatus(doctorId);

        // Send turn notification email
        emailService.sendTurnNotificationEmail(nextToken);

        return toDTO(nextToken);
    }

    @Transactional
    public TokenDTO startConsultation(Long tokenId) {
        Token token = tokenRepository.findByIdWithDetails(tokenId)
                .orElseThrow(() -> new ResourceNotFoundException("Token", "id", tokenId));

        if (token.getStatus() != Token.Status.CALLED) {
            throw new BadRequestException("Patient must be called before starting consultation");
        }

        token.setStatus(Token.Status.IN_CONSULTATION);
        token.setConsultationStartedAt(LocalDateTime.now());
        token = tokenRepository.save(token);

        updateQueueStatus(token.getDoctor().getId());

        return toDTO(token);
    }

    @Transactional
    public TokenDTO endConsultation(Long tokenId) {
        Token token = tokenRepository.findByIdWithDetails(tokenId)
                .orElseThrow(() -> new ResourceNotFoundException("Token", "id", tokenId));

        if (token.getStatus() != Token.Status.IN_CONSULTATION && token.getStatus() != Token.Status.CALLED) {
            throw new BadRequestException("No active consultation to end");
        }

        token.setStatus(Token.Status.COMPLETED);
        token.setConsultationEndedAt(LocalDateTime.now());
        if (token.getConsultationStartedAt() == null) {
            token.setConsultationStartedAt(LocalDateTime.now());
        }
        token = tokenRepository.save(token);

        updateQueueStatus(token.getDoctor().getId());

        // Send consultation completed email
        emailService.sendConsultationCompletedEmail(token);

        // Remove from position tracking and notify other patients in queue
        queueNotificationService.removeFromTracking(token.getId());
        if (token.getDoctor() != null) {
            queueNotificationService.notifyQueueAdvancement(token.getDoctor().getId());
        }

        return toDTO(token);
    }

    @Transactional
    public TokenDTO cancelActiveConsultation(Long tokenId) {
        Token token = tokenRepository.findByIdWithDetails(tokenId)
                .orElseThrow(() -> new ResourceNotFoundException("Token", "id", tokenId));

        if (token.getStatus() != Token.Status.IN_CONSULTATION && token.getStatus() != Token.Status.CALLED) {
            throw new BadRequestException("No active consultation to cancel");
        }

        token.setStatus(Token.Status.CANCELLED);
        token.setConsultationEndedAt(LocalDateTime.now());
        token = tokenRepository.save(token);

        if (token.getDoctor() != null) {
            updateQueueStatus(token.getDoctor().getId());
        }

        return toDTO(token);
    }

    public List<TokenDTO> getAllActiveConsultations() {
        return tokenRepository.findAllActiveTokens(LocalDate.now())
                .stream()
                .map(this::toDTO)
                .toList();
    }

    @Transactional
    public TokenDTO markNoShow(Long tokenId) {
        Token token = tokenRepository.findByIdWithDetails(tokenId)
                .orElseThrow(() -> new ResourceNotFoundException("Token", "id", tokenId));

        if (token.getStatus() != Token.Status.CALLED) {
            throw new BadRequestException("Only called patients can be marked as no-show");
        }

        token.setStatus(Token.Status.NO_SHOW);
        token = tokenRepository.save(token);

        updateQueueStatus(token.getDoctor().getId());

        // Notify other patients about queue advancement
        queueNotificationService.notifyNoShowOrSkip(token);

        return toDTO(token);
    }

    @Transactional
    public TokenDTO markPriority(Long tokenId, String priority) {
        Token token = tokenRepository.findByIdWithDetails(tokenId)
                .orElseThrow(() -> new ResourceNotFoundException("Token", "id", tokenId));

        if (token.getStatus() != Token.Status.WAITING) {
            throw new BadRequestException("Only waiting tokens can have priority changed");
        }

        try {
            Token.Priority newPriority = Token.Priority.valueOf(priority.toUpperCase());
            token.setPriority(newPriority);
            token.calculatePriorityScore();
            token = tokenRepository.save(token);
            
            // Notify affected patients about position changes due to priority update
            queueNotificationService.notifyPriorityChange(token);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid priority: " + priority);
        }

        return toDTO(token);
    }

    @Transactional
    public TokenDTO skipPatient(Long tokenId) {
        Token token = tokenRepository.findByIdWithDetails(tokenId)
                .orElseThrow(() -> new ResourceNotFoundException("Token", "id", tokenId));

        if (token.getStatus() != Token.Status.CALLED) {
            throw new BadRequestException("Only called patients can be skipped");
        }

        // Reset to waiting with reduced priority
        token.setStatus(Token.Status.WAITING);
        token.setPriorityScore(token.getPriorityScore() - 100); // Lower priority
        token.setCalledAt(null);
        token = tokenRepository.save(token);

        updateQueueStatus(token.getDoctor().getId());

        // Notify other patients about queue changes
        queueNotificationService.notifyNoShowOrSkip(token);

        return toDTO(token);
    }

    public DashboardStatsDTO getDashboardStats() {
        LocalDate today = LocalDate.now();
        
        List<Object[]> statusCounts = tokenRepository.getStatusCountsByDate(today);
        List<Object[]> deptCounts = tokenRepository.getDepartmentCountsByDate(today);

        Map<String, Integer> statusMap = new HashMap<>();
        int totalToday = 0;
        for (Object[] row : statusCounts) {
            String status = row[0].toString();
            int count = ((Number) row[1]).intValue();
            statusMap.put(status, count);
            totalToday += count;
        }

        Map<String, Integer> deptMap = new HashMap<>();
        for (Object[] row : deptCounts) {
            deptMap.put(row[0].toString(), ((Number) row[1]).intValue());
        }

        return DashboardStatsDTO.builder()
                .totalPatientsToday(totalToday)
                .totalWaiting(statusMap.getOrDefault("WAITING", 0))
                .totalInConsultation(statusMap.getOrDefault("IN_CONSULTATION", 0))
                .totalCompleted(statusMap.getOrDefault("COMPLETED", 0))
                .totalCancelled(statusMap.getOrDefault("CANCELLED", 0) + statusMap.getOrDefault("NO_SHOW", 0))
                .averageWaitTime(15) // Default average
                .departmentWiseCount(deptMap)
                .statusWiseCount(statusMap)
                .build();
    }

    public List<TokenDTO> getDoctorQueue(Long doctorId) {
        return tokenRepository.findActiveQueueByDoctor(doctorId, LocalDate.now())
                .stream()
                .map(this::toDTO)
                .toList();
    }

    private void updateQueueStatus(Long doctorId) {
        var doctor = doctorRepository.findById(doctorId).orElse(null);
        if (doctor == null) return;

        var status = queueStatusRepository.findByDoctorId(doctorId)
                .orElseGet(() -> {
                    var newStatus = new com.hospital.queue.entity.QueueStatus();
                    newStatus.setDoctor(doctor);
                    newStatus.setDepartment(doctor.getDepartment());
                    return newStatus;
                });

        int waitingCount = tokenRepository.countWaitingByDoctor(doctorId, LocalDate.now());
        status.setTotalWaiting(waitingCount);

        Token currentToken = tokenRepository.findCurrentTokenByDoctor(doctorId, LocalDate.now()).orElse(null);
        status.setCurrentToken(currentToken);

        queueStatusRepository.save(status);
    }

    private TokenDTO toDTO(Token token) {
        return TokenDTO.builder()
                .id(token.getId())
                .tokenNumber(token.getTokenNumber())
                .patientId(token.getPatient().getId())
                .patientName(token.getPatient().getFullName())
                .patientPhone(token.getPatient().getPhone())
                .departmentId(token.getDepartment().getId())
                .departmentName(token.getDepartment().getName())
                .departmentCode(token.getDepartment().getCode())
                .doctorId(token.getDoctor() != null ? token.getDoctor().getId() : null)
                .doctorName(token.getDoctor() != null ? token.getDoctor().getFullName() : null)
                .roomNumber(token.getDoctor() != null ? token.getDoctor().getRoomNumber() : null)
                .tokenDate(token.getTokenDate())
                .priority(token.getPriority())
                .priorityScore(token.getPriorityScore())
                .status(token.getStatus())
                .generatedAt(token.getGeneratedAt())
                .calledAt(token.getCalledAt())
                .consultationStartedAt(token.getConsultationStartedAt())
                .consultationEndedAt(token.getConsultationEndedAt())
                .notes(token.getNotes())
                .build();
    }
}
