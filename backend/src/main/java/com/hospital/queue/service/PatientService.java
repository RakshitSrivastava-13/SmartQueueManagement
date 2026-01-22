package com.hospital.queue.service;

import com.hospital.queue.dto.PatientDTO;
import com.hospital.queue.entity.Patient;
import com.hospital.queue.exception.BadRequestException;
import com.hospital.queue.exception.ResourceNotFoundException;
import com.hospital.queue.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PatientService {

    private final PatientRepository patientRepository;

    public PatientDTO getPatientById(Long id) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Patient", "id", id));
        return toDTO(patient);
    }

    public PatientDTO getPatientByPatientId(String patientId) {
        Patient patient = patientRepository.findByPatientId(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient", "patientId", patientId));
        return toDTO(patient);
    }

    public PatientDTO getPatientByPhone(String phone) {
        Patient patient = patientRepository.findByPhone(phone)
                .orElseThrow(() -> new ResourceNotFoundException("Patient", "phone", phone));
        return toDTO(patient);
    }

    public List<PatientDTO> searchPatients(String query) {
        return patientRepository.searchByName(query)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public PatientDTO registerPatient(PatientDTO dto) {
        // Check if phone already exists
        if (patientRepository.existsByPhone(dto.getPhone())) {
            throw new BadRequestException("A patient with this phone number already exists");
        }

        // Generate unique patient ID
        String patientId = generatePatientId();

        // Determine if senior citizen based on DOB
        boolean isSeniorCitizen = false;
        if (dto.getDateOfBirth() != null) {
            int age = Period.between(dto.getDateOfBirth(), LocalDate.now()).getYears();
            isSeniorCitizen = age >= 60;
        }

        Patient patient = Patient.builder()
                .patientId(patientId)
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .dateOfBirth(dto.getDateOfBirth())
                .gender(dto.getGender())
                .phone(dto.getPhone())
                .email(dto.getEmail())
                .address(dto.getAddress())
                .city(dto.getCity())
                .state(dto.getState())
                .pincode(dto.getPincode())
                .bloodGroup(dto.getBloodGroup())
                .emergencyContactName(dto.getEmergencyContactName())
                .emergencyContactPhone(dto.getEmergencyContactPhone())
                .isSeniorCitizen(isSeniorCitizen || Boolean.TRUE.equals(dto.getIsSeniorCitizen()))
                .isPregnant(Boolean.TRUE.equals(dto.getIsPregnant()))
                .build();

        patient = patientRepository.save(patient);
        return toDTO(patient);
    }

    @Transactional
    public PatientDTO updatePatient(Long id, PatientDTO dto) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Patient", "id", id));

        if (dto.getFirstName() != null) patient.setFirstName(dto.getFirstName());
        if (dto.getLastName() != null) patient.setLastName(dto.getLastName());
        if (dto.getDateOfBirth() != null) patient.setDateOfBirth(dto.getDateOfBirth());
        if (dto.getGender() != null) patient.setGender(dto.getGender());
        if (dto.getEmail() != null) patient.setEmail(dto.getEmail());
        if (dto.getAddress() != null) patient.setAddress(dto.getAddress());
        if (dto.getCity() != null) patient.setCity(dto.getCity());
        if (dto.getState() != null) patient.setState(dto.getState());
        if (dto.getPincode() != null) patient.setPincode(dto.getPincode());
        if (dto.getBloodGroup() != null) patient.setBloodGroup(dto.getBloodGroup());
        if (dto.getEmergencyContactName() != null) patient.setEmergencyContactName(dto.getEmergencyContactName());
        if (dto.getEmergencyContactPhone() != null) patient.setEmergencyContactPhone(dto.getEmergencyContactPhone());
        if (dto.getIsSeniorCitizen() != null) patient.setIsSeniorCitizen(dto.getIsSeniorCitizen());
        if (dto.getIsPregnant() != null) patient.setIsPregnant(dto.getIsPregnant());

        patient = patientRepository.save(patient);
        return toDTO(patient);
    }

    public PatientDTO findOrRegister(PatientDTO dto) {
        // Try to find existing patient by phone
        return patientRepository.findByPhone(dto.getPhone())
                .map(this::toDTO)
                .orElseGet(() -> registerPatient(dto));
    }

    private String generatePatientId() {
        Integer maxNumber = patientRepository.findMaxPatientIdNumber();
        int nextNumber = (maxNumber != null ? maxNumber : 0) + 1;
        return String.format("PAT%06d", nextNumber);
    }

    private PatientDTO toDTO(Patient patient) {
        return PatientDTO.builder()
                .id(patient.getId())
                .patientId(patient.getPatientId())
                .firstName(patient.getFirstName())
                .lastName(patient.getLastName())
                .fullName(patient.getFullName())
                .dateOfBirth(patient.getDateOfBirth())
                .gender(patient.getGender())
                .phone(patient.getPhone())
                .email(patient.getEmail())
                .address(patient.getAddress())
                .city(patient.getCity())
                .state(patient.getState())
                .pincode(patient.getPincode())
                .bloodGroup(patient.getBloodGroup())
                .emergencyContactName(patient.getEmergencyContactName())
                .emergencyContactPhone(patient.getEmergencyContactPhone())
                .isSeniorCitizen(patient.getIsSeniorCitizen())
                .isPregnant(patient.getIsPregnant())
                .build();
    }
}
