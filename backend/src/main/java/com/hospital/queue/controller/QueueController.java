package com.hospital.queue.controller;

import com.hospital.queue.dto.ApiResponse;
import com.hospital.queue.dto.QueueStatusDTO;
import com.hospital.queue.dto.TokenDTO;
import com.hospital.queue.service.QueueService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/queue")
@RequiredArgsConstructor
@Tag(name = "Queue", description = "Queue management APIs")
@CrossOrigin(origins = "*")
public class QueueController {

    private final QueueService queueService;

    @GetMapping("/doctor/{doctorId}")
    @Operation(summary = "Get doctor's queue", description = "Retrieve queue status for a doctor")
    public ResponseEntity<ApiResponse<QueueStatusDTO>> getQueueByDoctor(@PathVariable Long doctorId) {
        QueueStatusDTO queue = queueService.getQueueByDoctor(doctorId);
        return ResponseEntity.ok(ApiResponse.success(queue));
    }

    @GetMapping("/department/{departmentId}")
    @Operation(summary = "Get department queue", description = "Retrieve queue status for all doctors in a department")
    public ResponseEntity<ApiResponse<List<QueueStatusDTO>>> getQueueByDepartment(@PathVariable Long departmentId) {
        List<QueueStatusDTO> queues = queueService.getQueueByDepartment(departmentId);
        return ResponseEntity.ok(ApiResponse.success(queues));
    }

    @GetMapping("/current/{doctorId}")
    @Operation(summary = "Get current token", description = "Get the token currently being served by a doctor")
    public ResponseEntity<ApiResponse<TokenDTO>> getCurrentToken(@PathVariable Long doctorId) {
        TokenDTO token = queueService.getCurrentToken(doctorId);
        return ResponseEntity.ok(ApiResponse.success(token));
    }

    @GetMapping("/waiting/{doctorId}")
    @Operation(summary = "Get waiting tokens", description = "Get all waiting tokens for a doctor")
    public ResponseEntity<ApiResponse<List<TokenDTO>>> getWaitingTokens(@PathVariable Long doctorId) {
        List<TokenDTO> tokens = queueService.getWaitingTokens(doctorId);
        return ResponseEntity.ok(ApiResponse.success(tokens));
    }

    @GetMapping("/all")
    @Operation(summary = "Get all queues", description = "Retrieve queue status for all available doctors")
    public ResponseEntity<ApiResponse<List<QueueStatusDTO>>> getAllQueues() {
        List<QueueStatusDTO> queues = queueService.getAllQueues();
        return ResponseEntity.ok(ApiResponse.success(queues));
    }

    @GetMapping("/live-board")
    @Operation(summary = "Get live queue board", description = "Get live queue status for display boards")
    public ResponseEntity<ApiResponse<List<TokenDTO>>> getLiveQueueBoard() {
        List<TokenDTO> tokens = queueService.getLiveQueueBoard();
        return ResponseEntity.ok(ApiResponse.success(tokens));
    }
}
