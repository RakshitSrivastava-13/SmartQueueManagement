package com.hospital.queue.service;

import com.hospital.queue.dto.DepartmentDTO;
import com.hospital.queue.dto.DoctorDTO;
import com.hospital.queue.entity.Department;
import com.hospital.queue.entity.Doctor;
import com.hospital.queue.exception.ResourceNotFoundException;
import com.hospital.queue.repository.DepartmentRepository;
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
public class DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final DoctorRepository doctorRepository;
    private final TokenRepository tokenRepository;

    public List<DepartmentDTO> getAllDepartments() {
        return departmentRepository.findByIsActiveTrue()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public DepartmentDTO getDepartmentById(Long id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department", "id", id));
        return toDTO(department);
    }

    public DepartmentDTO getDepartmentByCode(String code) {
        Department department = departmentRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Department", "code", code));
        return toDTO(department);
    }

    public List<DepartmentDTO> getAllDepartmentsWithDoctors() {
        return departmentRepository.findAllWithDoctors()
                .stream()
                .map(this::toDTOWithDoctors)
                .collect(Collectors.toList());
    }

    public List<DoctorDTO> getDoctorsByDepartment(Long departmentId) {
        return doctorRepository.findByDepartmentIdAndIsActiveTrue(departmentId)
                .stream()
                .map(this::doctorToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public DepartmentDTO createDepartment(DepartmentDTO dto) {
        Department department = Department.builder()
                .name(dto.getName())
                .code(dto.getCode())
                .description(dto.getDescription())
                .floorNumber(dto.getFloorNumber())
                .roomNumbers(dto.getRoomNumbers())
                .isActive(true)
                .build();
        department = departmentRepository.save(department);
        return toDTO(department);
    }

    private DepartmentDTO toDTO(Department department) {
        return DepartmentDTO.builder()
                .id(department.getId())
                .name(department.getName())
                .code(department.getCode())
                .description(department.getDescription())
                .floorNumber(department.getFloorNumber())
                .roomNumbers(department.getRoomNumbers())
                .isActive(department.getIsActive())
                .createdAt(department.getCreatedAt())
                .build();
    }

    private DepartmentDTO toDTOWithDoctors(Department department) {
        DepartmentDTO dto = toDTO(department);
        List<DoctorDTO> doctors = department.getDoctors().stream()
                .filter(Doctor::getIsActive)
                .map(this::doctorToDTO)
                .collect(Collectors.toList());
        dto.setDoctors(doctors);
        return dto;
    }

    private DoctorDTO doctorToDTO(Doctor doctor) {
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
