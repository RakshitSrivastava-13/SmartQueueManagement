package com.hospital.queue.controller;

import com.hospital.queue.dto.ApiResponse;
import com.hospital.queue.dto.TokenDTO;
import com.hospital.queue.dto.TokenRequestDTO;
import com.hospital.queue.service.TokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tokens")
@RequiredArgsConstructor
@Tag(name = "Tokens", description = "Token management APIs")
@CrossOrigin(origins = "*")
public class TokenController {

    private final TokenService tokenService;

    @PostMapping
    @Operation(summary = "Generate token", description = "Generate a new token for patient")
    public ResponseEntity<ApiResponse<TokenDTO>> generateToken(@Valid @RequestBody TokenRequestDTO request) {
        TokenDTO token = tokenService.generateToken(request);
        return ResponseEntity.ok(ApiResponse.success("Token generated successfully", token));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get token by ID", description = "Retrieve token details by ID")
    public ResponseEntity<ApiResponse<TokenDTO>> getTokenById(@PathVariable Long id) {
        TokenDTO token = tokenService.getTokenById(id);
        return ResponseEntity.ok(ApiResponse.success(token));
    }

    @GetMapping("/number/{tokenNumber}")
    @Operation(summary = "Get token by number", description = "Retrieve token details by token number")
    public ResponseEntity<ApiResponse<TokenDTO>> getTokenByNumber(@PathVariable String tokenNumber) {
        TokenDTO token = tokenService.getTokenByNumber(tokenNumber);
        return ResponseEntity.ok(ApiResponse.success(token));
    }

    @GetMapping("/patient/{patientId}")
    @Operation(summary = "Get patient's tokens", description = "Retrieve all active tokens for a patient")
    public ResponseEntity<ApiResponse<List<TokenDTO>>> getPatientTokens(@PathVariable Long patientId) {
        List<TokenDTO> tokens = tokenService.getPatientTokens(patientId);
        return ResponseEntity.ok(ApiResponse.success(tokens));
    }

    @GetMapping("/queue-position/{tokenNumber}")
    @Operation(summary = "Get queue position", description = "Get token's position in queue with wait time")
    public ResponseEntity<ApiResponse<TokenDTO>> getQueuePosition(@PathVariable String tokenNumber) {
        TokenDTO token = tokenService.getQueuePosition(tokenNumber);
        return ResponseEntity.ok(ApiResponse.success(token));
    }

    @GetMapping("/waiting-time/{tokenId}")
    @Operation(summary = "Get estimated waiting time", description = "Get estimated waiting time for a token")
    public ResponseEntity<ApiResponse<Integer>> getEstimatedWaitTime(@PathVariable Long tokenId) {
        int waitTime = tokenService.getEstimatedWaitTime(tokenId);
        return ResponseEntity.ok(ApiResponse.success(waitTime));
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "Cancel token", description = "Cancel a waiting token")
    public ResponseEntity<ApiResponse<TokenDTO>> cancelToken(@PathVariable Long id) {
        TokenDTO token = tokenService.cancelToken(id);
        return ResponseEntity.ok(ApiResponse.success("Token cancelled", token));
    }

    @GetMapping("/today")
    @Operation(summary = "Get today's tokens", description = "Retrieve all tokens for today")
    public ResponseEntity<ApiResponse<List<TokenDTO>>> getTodayTokens() {
        List<TokenDTO> tokens = tokenService.getTodayTokens();
        return ResponseEntity.ok(ApiResponse.success(tokens));
    }
}
