package com.hospital.queue.controller;

import com.hospital.queue.dto.ApiResponse;
import com.hospital.queue.dto.DoctorDTO;
import com.hospital.queue.service.DoctorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/doctors")
@RequiredArgsConstructor
@Tag(name = "Doctors", description = "Doctor management APIs")
@CrossOrigin(origins = "*")
public class DoctorController {

    private final DoctorService doctorService;

    @GetMapping
    @Operation(summary = "Get all doctors", description = "Retrieve list of all active doctors")
    public ResponseEntity<ApiResponse<List<DoctorDTO>>> getAllDoctors() {
        List<DoctorDTO> doctors = doctorService.getAllDoctors();
        return ResponseEntity.ok(ApiResponse.success(doctors));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get doctor by ID", description = "Retrieve doctor details by ID")
    public ResponseEntity<ApiResponse<DoctorDTO>> getDoctorById(@PathVariable Long id) {
        DoctorDTO doctor = doctorService.getDoctorById(id);
        return ResponseEntity.ok(ApiResponse.success(doctor));
    }

    @GetMapping("/available")
    @Operation(summary = "Get available doctors", description = "Retrieve list of available doctors")
    public ResponseEntity<ApiResponse<List<DoctorDTO>>> getAvailableDoctors() {
        List<DoctorDTO> doctors = doctorService.getAvailableDoctors();
        return ResponseEntity.ok(ApiResponse.success(doctors));
    }

    @GetMapping("/department/{departmentId}")
    @Operation(summary = "Get doctors by department", description = "Retrieve all doctors in a department")
    public ResponseEntity<ApiResponse<List<DoctorDTO>>> getDoctorsByDepartment(@PathVariable Long departmentId) {
        List<DoctorDTO> doctors = doctorService.getDoctorsByDepartment(departmentId);
        return ResponseEntity.ok(ApiResponse.success(doctors));
    }

    @PatchMapping("/{id}/availability")
    @Operation(summary = "Update doctor availability", description = "Update doctor's availability status")
    public ResponseEntity<ApiResponse<DoctorDTO>> updateAvailability(
            @PathVariable Long id,
            @RequestParam boolean available) {
        DoctorDTO doctor = doctorService.updateAvailability(id, available);
        return ResponseEntity.ok(ApiResponse.success("Availability updated", doctor));
    }
}
