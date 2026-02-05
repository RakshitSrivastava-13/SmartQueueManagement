package com.hospital.queue.repository;

import com.hospital.queue.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {

    Optional<Department> findByCode(String code);

    Optional<Department> findByName(String name);

    List<Department> findByIsActiveTrue();

    List<Department> findByDomainIdAndIsActiveTrue(Long domainId);

    @Query("SELECT d FROM Department d LEFT JOIN FETCH d.doctors WHERE d.isActive = true")
    List<Department> findAllWithDoctors();

    boolean existsByCode(String code);

    boolean existsByName(String name);
}
