package com.hospital.queue.controller;

import com.hospital.queue.dto.ApiResponse;
import com.hospital.queue.dto.DepartmentDTO;
import com.hospital.queue.dto.DoctorDTO;
import com.hospital.queue.service.DepartmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/departments")
@RequiredArgsConstructor
@Tag(name = "Departments", description = "Department management APIs")
@CrossOrigin(origins = "*")
public class DepartmentController {

    private final DepartmentService departmentService;

    @GetMapping
    @Operation(summary = "Get all departments", description = "Retrieve list of all active departments")
    public ResponseEntity<ApiResponse<List<DepartmentDTO>>> getAllDepartments() {
        List<DepartmentDTO> departments = departmentService.getAllDepartments();
        return ResponseEntity.ok(ApiResponse.success(departments));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get department by ID", description = "Retrieve department details by ID")
    public ResponseEntity<ApiResponse<DepartmentDTO>> getDepartmentById(@PathVariable Long id) {
        DepartmentDTO department = departmentService.getDepartmentById(id);
        return ResponseEntity.ok(ApiResponse.success(department));
    }

    @GetMapping("/code/{code}")
    @Operation(summary = "Get department by code", description = "Retrieve department details by code")
    public ResponseEntity<ApiResponse<DepartmentDTO>> getDepartmentByCode(@PathVariable String code) {
        DepartmentDTO department = departmentService.getDepartmentByCode(code);
        return ResponseEntity.ok(ApiResponse.success(department));
    }

    @GetMapping("/with-doctors")
    @Operation(summary = "Get all departments with doctors", description = "Retrieve all departments with their doctors")
    public ResponseEntity<ApiResponse<List<DepartmentDTO>>> getAllDepartmentsWithDoctors() {
        List<DepartmentDTO> departments = departmentService.getAllDepartmentsWithDoctors();
        return ResponseEntity.ok(ApiResponse.success(departments));
    }

    @GetMapping("/{id}/doctors")
    @Operation(summary = "Get doctors by department", description = "Retrieve all doctors in a department")
    public ResponseEntity<ApiResponse<List<DoctorDTO>>> getDoctorsByDepartment(@PathVariable Long id) {
        List<DoctorDTO> doctors = departmentService.getDoctorsByDepartment(id);
        return ResponseEntity.ok(ApiResponse.success(doctors));
    }

    @PostMapping
    @Operation(summary = "Create department", description = "Create a new department")
    public ResponseEntity<ApiResponse<DepartmentDTO>> createDepartment(@RequestBody DepartmentDTO dto) {
        DepartmentDTO department = departmentService.createDepartment(dto);
        return ResponseEntity.ok(ApiResponse.success("Department created successfully", department));
    }
}
