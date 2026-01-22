package com.hospital.queue.controller;

import com.hospital.queue.dto.ApiResponse;
import com.hospital.queue.dto.PatientDTO;
import com.hospital.queue.service.PatientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/patients")
@RequiredArgsConstructor
@Tag(name = "Patients", description = "Patient management APIs")
@CrossOrigin(origins = "*")
public class PatientController {

    private final PatientService patientService;

    @GetMapping("/{id}")
    @Operation(summary = "Get patient by ID", description = "Retrieve patient details by ID")
    public ResponseEntity<ApiResponse<PatientDTO>> getPatientById(@PathVariable Long id) {
        PatientDTO patient = patientService.getPatientById(id);
        return ResponseEntity.ok(ApiResponse.success(patient));
    }

    @GetMapping("/patient-id/{patientId}")
    @Operation(summary = "Get patient by patient ID", description = "Retrieve patient by their patient ID")
    public ResponseEntity<ApiResponse<PatientDTO>> getPatientByPatientId(@PathVariable String patientId) {
        PatientDTO patient = patientService.getPatientByPatientId(patientId);
        return ResponseEntity.ok(ApiResponse.success(patient));
    }

    @GetMapping("/phone/{phone}")
    @Operation(summary = "Get patient by phone", description = "Retrieve patient by phone number")
    public ResponseEntity<ApiResponse<PatientDTO>> getPatientByPhone(@PathVariable String phone) {
        PatientDTO patient = patientService.getPatientByPhone(phone);
        return ResponseEntity.ok(ApiResponse.success(patient));
    }

    @GetMapping("/search")
    @Operation(summary = "Search patients", description = "Search patients by name")
    public ResponseEntity<ApiResponse<List<PatientDTO>>> searchPatients(@RequestParam String query) {
        List<PatientDTO> patients = patientService.searchPatients(query);
        return ResponseEntity.ok(ApiResponse.success(patients));
    }

    @PostMapping
    @Operation(summary = "Register patient", description = "Register a new patient")
    public ResponseEntity<ApiResponse<PatientDTO>> registerPatient(@Valid @RequestBody PatientDTO dto) {
        PatientDTO patient = patientService.registerPatient(dto);
        return ResponseEntity.ok(ApiResponse.success("Patient registered successfully", patient));
    }

    @PostMapping("/find-or-register")
    @Operation(summary = "Find or register patient", description = "Find existing patient by phone or register new")
    public ResponseEntity<ApiResponse<PatientDTO>> findOrRegister(@Valid @RequestBody PatientDTO dto) {
        PatientDTO patient = patientService.findOrRegister(dto);
        return ResponseEntity.ok(ApiResponse.success(patient));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update patient", description = "Update patient details")
    public ResponseEntity<ApiResponse<PatientDTO>> updatePatient(
            @PathVariable Long id,
            @RequestBody PatientDTO dto) {
        PatientDTO patient = patientService.updatePatient(id, dto);
        return ResponseEntity.ok(ApiResponse.success("Patient updated successfully", patient));
    }
}
