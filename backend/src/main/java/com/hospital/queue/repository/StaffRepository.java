package com.hospital.queue.repository;

import com.hospital.queue.entity.Staff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StaffRepository extends JpaRepository<Staff, Long> {

    Optional<Staff> findByUsername(String username);

    Optional<Staff> findByEmployeeId(String employeeId);

    Optional<Staff> findByEmail(String email);

    List<Staff> findByRole(Staff.Role role);

    List<Staff> findByDepartmentId(Long departmentId);

    List<Staff> findByIsActiveTrue();

    @Query("SELECT s FROM Staff s WHERE s.doctor.id = :doctorId")
    Optional<Staff> findByDoctorId(@Param("doctorId") Long doctorId);

    @Query("SELECT s FROM Staff s LEFT JOIN FETCH s.department LEFT JOIN FETCH s.doctor WHERE s.username = :username")
    Optional<Staff> findByUsernameWithDetails(@Param("username") String username);

    boolean existsByUsername(String username);

    boolean existsByEmployeeId(String employeeId);

    boolean existsByEmail(String email);
}
