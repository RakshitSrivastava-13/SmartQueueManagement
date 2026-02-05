package com.hospital.queue.controller;

import com.hospital.queue.dto.ApiResponse;
import com.hospital.queue.dto.DomainDTO;
import com.hospital.queue.service.DomainService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/domains")
@RequiredArgsConstructor
@Tag(name = "Domains", description = "Domain management APIs")
@CrossOrigin(origins = "*")
public class DomainController {

    private final DomainService domainService;

    @GetMapping
    @Operation(summary = "Get all active domains", description = "Retrieve list of all active service domains")
    public ResponseEntity<ApiResponse<List<DomainDTO>>> getAllActiveDomains() {
        List<DomainDTO> domains = domainService.getAllActiveDomains();
        return ResponseEntity.ok(ApiResponse.success("Domains fetched successfully", domains));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get domain by ID", description = "Retrieve domain details by ID")
    public ResponseEntity<ApiResponse<DomainDTO>> getDomainById(@PathVariable Long id) {
        DomainDTO domain = domainService.getDomainById(id);
        return ResponseEntity.ok(ApiResponse.success("Domain fetched successfully", domain));
    }

    @PostMapping
    @Operation(summary = "Create new domain", description = "Create a new service domain")
    public ResponseEntity<ApiResponse<DomainDTO>> createDomain(@RequestBody DomainDTO domainDTO) {
        DomainDTO created = domainService.createDomain(domainDTO);
        return ResponseEntity.ok(ApiResponse.success("Domain created successfully", created));
    }
}
