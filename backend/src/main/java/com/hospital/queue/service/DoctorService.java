package com.hospital.queue.service;

import com.hospital.queue.dto.DoctorDTO;
import com.hospital.queue.entity.Doctor;
import com.hospital.queue.exception.ResourceNotFoundException;
import com.hospital.queue.repository.DoctorRepository;
import com.hospital.queue.repository.TokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DoctorService {

    private final DoctorRepository doctorRepository;
    private final TokenRepository tokenRepository;

    public List<DoctorDTO> getAllDoctors() {
        return doctorRepository.findAllWithDepartment()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public DoctorDTO getDoctorById(Long id) {
        Doctor doctor = doctorRepository.findByIdWithDepartment(id)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor", "id", id));
        return toDTO(doctor);
    }

    public List<DoctorDTO> getAvailableDoctors() {
        return doctorRepository.findByIsAvailableTrueAndIsActiveTrue()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<DoctorDTO> getDoctorsByDepartment(Long departmentId) {
        return doctorRepository.findByDepartmentIdAndIsActiveTrue(departmentId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public DoctorDTO updateAvailability(Long doctorId, boolean available) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor", "id", doctorId));
        doctor.setIsAvailable(available);
        doctor = doctorRepository.save(doctor);
        return toDTO(doctor);
    }

    private DoctorDTO toDTO(Doctor doctor) {
        int queueLength = tokenRepository.countWaitingByDoctor(doctor.getId(), LocalDate.now());
        int estimatedWait = queueLength * doctor.getConsultationDurationMinutes();
        
        return DoctorDTO.builder()
                .id(doctor.getId())
                .employeeId(doctor.getEmployeeId())
                .firstName(doctor.getFirstName())
                .lastName(doctor.getLastName())
                .fullName(doctor.getFullName())
                .specialization(doctor.getSpecialization())
                .qualification(doctor.getQualification())
                .email(doctor.getEmail())
                .phone(doctor.getPhone())
                .roomNumber(doctor.getRoomNumber())
                .consultationDurationMinutes(doctor.getConsultationDurationMinutes())
                .maxPatientsPerDay(doctor.getMaxPatientsPerDay())
                .isAvailable(doctor.getIsAvailable())
                .isActive(doctor.getIsActive())
                .departmentId(doctor.getDepartment().getId())
                .departmentName(doctor.getDepartment().getName())
                .departmentCode(doctor.getDepartment().getCode())
                .currentQueueLength(queueLength)
                .estimatedWaitTime(estimatedWait)
                .build();
    }
}
