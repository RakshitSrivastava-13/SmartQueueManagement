package com.hospital.queue.repository;

import com.hospital.queue.entity.QueueStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QueueStatusRepository extends JpaRepository<QueueStatus, Long> {

    Optional<QueueStatus> findByDoctorId(Long doctorId);

    List<QueueStatus> findByDepartmentId(Long departmentId);

    @Query("SELECT qs FROM QueueStatus qs JOIN FETCH qs.department LEFT JOIN FETCH qs.doctor LEFT JOIN FETCH qs.currentToken")
    List<QueueStatus> findAllWithDetails();

    @Query("SELECT qs FROM QueueStatus qs JOIN FETCH qs.department LEFT JOIN FETCH qs.doctor LEFT JOIN FETCH qs.currentToken " +
           "WHERE qs.department.id = :deptId")
    List<QueueStatus> findByDepartmentWithDetails(@Param("deptId") Long departmentId);

    @Query("SELECT qs FROM QueueStatus qs JOIN FETCH qs.department LEFT JOIN FETCH qs.doctor LEFT JOIN FETCH qs.currentToken " +
           "WHERE qs.doctor.id = :doctorId")
    Optional<QueueStatus> findByDoctorWithDetails(@Param("doctorId") Long doctorId);
}
