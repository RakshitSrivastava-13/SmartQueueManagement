package com.hospital.queue.service;

import com.hospital.queue.dto.QueueStatusDTO;
import com.hospital.queue.dto.TokenDTO;
import com.hospital.queue.entity.Token;
import com.hospital.queue.exception.ResourceNotFoundException;
import com.hospital.queue.repository.DoctorRepository;
import com.hospital.queue.repository.QueueStatusRepository;
import com.hospital.queue.repository.TokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QueueService {

    private final TokenRepository tokenRepository;
    private final DoctorRepository doctorRepository;
    private final QueueStatusRepository queueStatusRepository;

    public QueueStatusDTO getQueueByDoctor(Long doctorId) {
        var doctor = doctorRepository.findByIdWithDepartment(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor", "id", doctorId));

        List<Token> waitingTokens = tokenRepository.findActiveQueueByDoctor(doctorId, LocalDate.now());
        Token currentToken = tokenRepository.findCurrentTokenByDoctor(doctorId, LocalDate.now()).orElse(null);

        // Calculate average wait time
        Double avgTime = tokenRepository.calculateAverageConsultationTime(doctorId, LocalDateTime.now().minusDays(30));
        int avgWaitTime = avgTime != null ? avgTime.intValue() : doctor.getConsultationDurationMinutes();

        return QueueStatusDTO.builder()
                .departmentId(doctor.getDepartment().getId())
                .departmentName(doctor.getDepartment().getName())
                .doctorId(doctorId)
                .doctorName(doctor.getFullName())
                .roomNumber(doctor.getRoomNumber())
                .currentToken(currentToken != null ? tokenToDTO(currentToken) : null)
                .totalWaiting(waitingTokens.size())
                .averageWaitTimeMinutes(avgWaitTime)
                .lastUpdated(LocalDateTime.now())
                .waitingTokens(waitingTokens.stream().map(this::tokenToDTO).collect(Collectors.toList()))
                .build();
    }

    public List<QueueStatusDTO> getQueueByDepartment(Long departmentId) {
        return doctorRepository.findByDepartmentIdAndIsActiveTrue(departmentId)
                .stream()
                .map(doctor -> getQueueByDoctor(doctor.getId()))
                .collect(Collectors.toList());
    }

    public TokenDTO getCurrentToken(Long doctorId) {
        Token token = tokenRepository.findCurrentTokenByDoctor(doctorId, LocalDate.now())
                .orElse(null);
        return token != null ? tokenToDTO(token) : null;
    }

    public List<TokenDTO> getWaitingTokens(Long doctorId) {
        return tokenRepository.findActiveQueueByDoctor(doctorId, LocalDate.now())
                .stream()
                .filter(t -> t.getStatus() == Token.Status.WAITING)
                .map(this::tokenToDTO)
                .collect(Collectors.toList());
    }

    public List<QueueStatusDTO> getAllQueues() {
        return doctorRepository.findByIsAvailableTrueAndIsActiveTrue()
                .stream()
                .map(doctor -> getQueueByDoctor(doctor.getId()))
                .collect(Collectors.toList());
    }

    public List<TokenDTO> getLiveQueueBoard() {
        // Get all tokens that are being served or waiting
        List<Token> tokens = tokenRepository.findAllByDateWithDetails(LocalDate.now());
        return tokens.stream()
                .filter(t -> t.getStatus() == Token.Status.WAITING || 
                           t.getStatus() == Token.Status.CALLED || 
                           t.getStatus() == Token.Status.IN_CONSULTATION)
                .map(this::tokenToDTO)
                .collect(Collectors.toList());
    }

    private TokenDTO tokenToDTO(Token token) {
        int position = 0;
        int waitMinutes = 0;
        LocalDateTime estimatedServiceTime = null;
        
        if (token.getDoctor() != null && token.getStatus() == Token.Status.WAITING) {
            position = tokenRepository.findPositionInDoctorQueue(
                    token.getDoctor().getId(),
                    token.getTokenDate(),
                    token.getPriorityScore(),
                    token.getGeneratedAt()
            ) + 1;
            
            Double avgTime = tokenRepository.calculateAverageConsultationTime(
                    token.getDoctor().getId(),
                    LocalDateTime.now().minusDays(30)
            );
            int consultTime = avgTime != null ? avgTime.intValue() : token.getDoctor().getConsultationDurationMinutes();
            waitMinutes = (position - 1) * consultTime;
            
            // Calculate exact estimated service time
            estimatedServiceTime = LocalDateTime.now().plusMinutes(waitMinutes);
        }

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
                .queuePosition(position)
                .patientsAhead(position > 0 ? position - 1 : 0)
                .estimatedWaitMinutes(waitMinutes)
                .estimatedServiceTime(estimatedServiceTime)
                .build();
    }
}
