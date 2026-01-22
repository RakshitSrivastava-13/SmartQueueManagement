package com.hospital.queue.repository;

import com.hospital.queue.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {

    Optional<Patient> findByPatientId(String patientId);

    Optional<Patient> findByPhone(String phone);

    List<Patient> findByPhoneContaining(String phone);

    Optional<Patient> findByEmail(String email);

    @Query("SELECT p FROM Patient p WHERE LOWER(p.firstName) LIKE LOWER(CONCAT('%', :name, '%')) OR LOWER(p.lastName) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Patient> searchByName(@Param("name") String name);

    @Query("SELECT p FROM Patient p WHERE p.phone = :phone OR p.patientId = :patientId")
    Optional<Patient> findByPhoneOrPatientId(@Param("phone") String phone, @Param("patientId") String patientId);

    boolean existsByPhone(String phone);

    boolean existsByPatientId(String patientId);

    @Query("SELECT COUNT(p) FROM Patient p")
    long countTotalPatients();

    @Query("SELECT MAX(CAST(SUBSTRING(p.patientId, 4) AS integer)) FROM Patient p WHERE p.patientId LIKE 'PAT%'")
    Integer findMaxPatientIdNumber();
}
