package com.hospital.queue.service;

import com.hospital.queue.dto.TokenDTO;
import com.hospital.queue.dto.TokenRequestDTO;
import com.hospital.queue.entity.*;
import com.hospital.queue.exception.BadRequestException;
import com.hospital.queue.exception.ResourceNotFoundException;
import com.hospital.queue.repository.*;
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
public class TokenService {

    private final TokenRepository tokenRepository;
    private final PatientRepository patientRepository;
    private final DepartmentRepository departmentRepository;
    private final DoctorRepository doctorRepository;
    private final QueueStatusRepository queueStatusRepository;

    @Transactional
    public TokenDTO generateToken(TokenRequestDTO request) {
        // Validate patient
        Patient patient = patientRepository.findById(request.getPatientId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient", "id", request.getPatientId()));

        // Validate department
        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department", "id", request.getDepartmentId()));

        // Validate doctor if provided
        Doctor doctor = null;
        if (request.getDoctorId() != null) {
            doctor = doctorRepository.findById(request.getDoctorId())
                    .orElseThrow(() -> new ResourceNotFoundException("Doctor", "id", request.getDoctorId()));
            
            // Check if doctor has reached max patients
            int currentCount = tokenRepository.countTotalByDoctorAndDate(doctor.getId(), LocalDate.now());
            if (currentCount >= doctor.getMaxPatientsPerDay()) {
                throw new BadRequestException("Doctor has reached maximum patients for today");
            }
        }

        // Determine priority
        Token.Priority priority = determinePriority(patient, request.getPriority());

        // Generate token number
        String tokenNumber = generateTokenNumber(department.getCode());

        // Create token
        Token token = Token.builder()
                .tokenNumber(tokenNumber)
                .patient(patient)
                .department(department)
                .doctor(doctor)
                .tokenDate(LocalDate.now())
                .priority(priority)
                .status(Token.Status.WAITING)
                .notes(request.getNotes())
                .build();
        
        token.calculatePriorityScore();
        token = tokenRepository.save(token);

        // Update queue status
        updateQueueStatus(doctor != null ? doctor.getId() : null, department.getId());

        return toDTO(token);
    }

    public TokenDTO getTokenById(Long id) {
        Token token = tokenRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Token", "id", id));
        return toDTO(token);
    }

    public TokenDTO getTokenByNumber(String tokenNumber) {
        Token token = tokenRepository.findByTokenNumberAndTokenDate(tokenNumber, LocalDate.now())
                .orElseThrow(() -> new ResourceNotFoundException("Token", "tokenNumber", tokenNumber));
        return toDTO(token);
    }

    public List<TokenDTO> getPatientTokens(Long patientId) {
        return tokenRepository.findByPatientIdAndTokenDate(patientId, LocalDate.now())
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public TokenDTO getQueuePosition(String tokenNumber) {
        Token token = tokenRepository.findByTokenNumberAndTokenDate(tokenNumber, LocalDate.now())
                .orElseThrow(() -> new ResourceNotFoundException("Token", "tokenNumber", tokenNumber));
        return toDTOWithQueueInfo(token);
    }

    public int getEstimatedWaitTime(Long tokenId) {
        Token token = tokenRepository.findById(tokenId)
                .orElseThrow(() -> new ResourceNotFoundException("Token", "id", tokenId));
        
        if (token.getDoctor() == null) {
            return 0;
        }

        int position = tokenRepository.findPositionInDoctorQueue(
                token.getDoctor().getId(),
                token.getTokenDate(),
                token.getPriorityScore(),
                token.getGeneratedAt()
        );

        // Get average consultation time
        Double avgTime = tokenRepository.calculateAverageConsultationTime(
                token.getDoctor().getId(),
                LocalDateTime.now().minusDays(30)
        );

        int consultationTime = avgTime != null ? avgTime.intValue() : token.getDoctor().getConsultationDurationMinutes();
        return position * consultationTime;
    }

    @Transactional
    public TokenDTO cancelToken(Long tokenId) {
        Token token = tokenRepository.findById(tokenId)
                .orElseThrow(() -> new ResourceNotFoundException("Token", "id", tokenId));
        
        if (token.getStatus() != Token.Status.WAITING) {
            throw new BadRequestException("Only waiting tokens can be cancelled");
        }

        token.setStatus(Token.Status.CANCELLED);
        token = tokenRepository.save(token);

        // Update queue status
        if (token.getDoctor() != null) {
            updateQueueStatus(token.getDoctor().getId(), token.getDepartment().getId());
        }

        return toDTO(token);
    }

    public List<TokenDTO> getTodayTokens() {
        return tokenRepository.findAllByDateWithDetails(LocalDate.now())
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    private Token.Priority determinePriority(Patient patient, String requestedPriority) {
        // If emergency is requested, prioritize it
        if (requestedPriority != null) {
            try {
                Token.Priority requested = Token.Priority.valueOf(requestedPriority.toUpperCase());
                if (requested == Token.Priority.EMERGENCY) {
                    return Token.Priority.EMERGENCY;
                }
            } catch (IllegalArgumentException ignored) {}
        }

        // Check patient attributes
        if (Boolean.TRUE.equals(patient.getIsPregnant())) {
            return Token.Priority.PREGNANT;
        }
        if (Boolean.TRUE.equals(patient.getIsSeniorCitizen())) {
            return Token.Priority.SENIOR_CITIZEN;
        }

        // Use requested priority or default to NORMAL
        if (requestedPriority != null) {
            try {
                return Token.Priority.valueOf(requestedPriority.toUpperCase());
            } catch (IllegalArgumentException ignored) {}
        }

        return Token.Priority.NORMAL;
    }

    private String generateTokenNumber(String departmentCode) {
        int count = tokenRepository.countByDepartmentAndDate(
                departmentRepository.findByCode(departmentCode)
                        .orElseThrow(() -> new ResourceNotFoundException("Department", "code", departmentCode))
                        .getId(),
                LocalDate.now()
        );
        String dateStr = LocalDate.now().toString().replace("-", "");
        return String.format("%s-%s-%04d", departmentCode, dateStr, count + 1);
    }

    private void updateQueueStatus(Long doctorId, Long departmentId) {
        if (doctorId != null) {
            QueueStatus status = queueStatusRepository.findByDoctorId(doctorId)
                    .orElseGet(() -> {
                        QueueStatus newStatus = new QueueStatus();
                        newStatus.setDoctor(doctorRepository.findById(doctorId).orElse(null));
                        newStatus.setDepartment(departmentRepository.findById(departmentId).orElse(null));
                        return newStatus;
                    });
            
            int waitingCount = tokenRepository.countWaitingByDoctor(doctorId, LocalDate.now());
            status.setTotalWaiting(waitingCount);
            
            Token currentToken = tokenRepository.findCurrentTokenByDoctor(doctorId, LocalDate.now()).orElse(null);
            status.setCurrentToken(currentToken);
            
            queueStatusRepository.save(status);
        }
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

    private TokenDTO toDTOWithQueueInfo(Token token) {
        TokenDTO dto = toDTO(token);
        
        if (token.getDoctor() != null && token.getStatus() == Token.Status.WAITING) {
            int position = tokenRepository.findPositionInDoctorQueue(
                    token.getDoctor().getId(),
                    token.getTokenDate(),
                    token.getPriorityScore(),
                    token.getGeneratedAt()
            );
            dto.setQueuePosition(position + 1);
            dto.setPatientsAhead(position);
            dto.setEstimatedWaitMinutes(getEstimatedWaitTime(token.getId()));
        } else {
            dto.setQueuePosition(0);
            dto.setPatientsAhead(0);
            dto.setEstimatedWaitMinutes(0);
        }
        
        return dto;
    }
}
