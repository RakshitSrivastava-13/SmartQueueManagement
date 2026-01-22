package com.hospital.queue.controller;

import com.hospital.queue.dto.ApiResponse;
import com.hospital.queue.dto.DashboardStatsDTO;
import com.hospital.queue.dto.TokenDTO;
import com.hospital.queue.service.StaffService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/staff")
@RequiredArgsConstructor
@Tag(name = "Staff", description = "Staff operations APIs")
@CrossOrigin(origins = "*")
public class StaffController {

    private final StaffService staffService;

    @PostMapping("/call-next/{doctorId}")
    @Operation(summary = "Call next patient", description = "Call the next patient in queue")
    public ResponseEntity<ApiResponse<TokenDTO>> callNextPatient(@PathVariable Long doctorId) {
        TokenDTO token = staffService.callNextPatient(doctorId);
        return ResponseEntity.ok(ApiResponse.success("Patient called", token));
    }

    @PostMapping("/start-consultation/{tokenId}")
    @Operation(summary = "Start consultation", description = "Start consultation for a called patient")
    public ResponseEntity<ApiResponse<TokenDTO>> startConsultation(@PathVariable Long tokenId) {
        TokenDTO token = staffService.startConsultation(tokenId);
        return ResponseEntity.ok(ApiResponse.success("Consultation started", token));
    }

    @PostMapping("/end-consultation/{tokenId}")
    @Operation(summary = "End consultation", description = "End the current consultation")
    public ResponseEntity<ApiResponse<TokenDTO>> endConsultation(@PathVariable Long tokenId) {
        TokenDTO token = staffService.endConsultation(tokenId);
        return ResponseEntity.ok(ApiResponse.success("Consultation completed", token));
    }

    @PostMapping("/cancel-consultation/{tokenId}")
    @Operation(summary = "Cancel consultation", description = "Cancel an active consultation")
    public ResponseEntity<ApiResponse<TokenDTO>> cancelConsultation(@PathVariable Long tokenId) {
        TokenDTO token = staffService.cancelActiveConsultation(tokenId);
        return ResponseEntity.ok(ApiResponse.success("Consultation cancelled", token));
    }

    @GetMapping("/active-consultations")
    @Operation(summary = "Get active consultations", description = "Get all active consultations across all doctors")
    public ResponseEntity<ApiResponse<List<TokenDTO>>> getActiveConsultations() {
        List<TokenDTO> tokens = staffService.getAllActiveConsultations();
        return ResponseEntity.ok(ApiResponse.success(tokens));
    }

    @PostMapping("/no-show/{tokenId}")
    @Operation(summary = "Mark no-show", description = "Mark a called patient as no-show")
    public ResponseEntity<ApiResponse<TokenDTO>> markNoShow(@PathVariable Long tokenId) {
        TokenDTO token = staffService.markNoShow(tokenId);
        return ResponseEntity.ok(ApiResponse.success("Marked as no-show", token));
    }

    @PostMapping("/mark-priority/{tokenId}")
    @Operation(summary = "Mark priority", description = "Update token priority")
    public ResponseEntity<ApiResponse<TokenDTO>> markPriority(
            @PathVariable Long tokenId,
            @RequestParam String priority) {
        TokenDTO token = staffService.markPriority(tokenId, priority);
        return ResponseEntity.ok(ApiResponse.success("Priority updated", token));
    }

    @PostMapping("/skip/{tokenId}")
    @Operation(summary = "Skip patient", description = "Skip a called patient and move to next")
    public ResponseEntity<ApiResponse<TokenDTO>> skipPatient(@PathVariable Long tokenId) {
        TokenDTO token = staffService.skipPatient(tokenId);
        return ResponseEntity.ok(ApiResponse.success("Patient skipped", token));
    }

    @GetMapping("/dashboard")
    @Operation(summary = "Get dashboard stats", description = "Get dashboard statistics")
    public ResponseEntity<ApiResponse<DashboardStatsDTO>> getDashboardStats() {
        DashboardStatsDTO stats = staffService.getDashboardStats();
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    @GetMapping("/doctor-queue/{doctorId}")
    @Operation(summary = "Get doctor's queue", description = "Get all tokens in doctor's queue")
    public ResponseEntity<ApiResponse<List<TokenDTO>>> getDoctorQueue(@PathVariable Long doctorId) {
        List<TokenDTO> tokens = staffService.getDoctorQueue(doctorId);
        return ResponseEntity.ok(ApiResponse.success(tokens));
    }
}
