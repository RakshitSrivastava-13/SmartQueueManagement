package com.hospital.queue.repository;

import com.hospital.queue.entity.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Long> {

    Optional<Doctor> findByEmployeeId(String employeeId);

    List<Doctor> findByDepartmentId(Long departmentId);

    List<Doctor> findByDepartmentIdAndIsActiveTrue(Long departmentId);

    List<Doctor> findByIsActiveTrue();

    List<Doctor> findByIsAvailableTrueAndIsActiveTrue();

    @Query("SELECT d FROM Doctor d JOIN FETCH d.department WHERE d.id = :id")
    Optional<Doctor> findByIdWithDepartment(@Param("id") Long id);

    @Query("SELECT d FROM Doctor d JOIN FETCH d.department WHERE d.isActive = true")
    List<Doctor> findAllWithDepartment();

    @Query("SELECT d FROM Doctor d WHERE d.department.id = :deptId AND d.isAvailable = true AND d.isActive = true")
    List<Doctor> findAvailableDoctorsByDepartment(@Param("deptId") Long departmentId);

    @Query("SELECT AVG(d.consultationDurationMinutes) FROM Doctor d WHERE d.department.id = :deptId")
    Double getAverageConsultationTimeByDepartment(@Param("deptId") Long departmentId);

    boolean existsByEmployeeId(String employeeId);
}
