package com.hospital.queue.repository;

import com.hospital.queue.entity.Token;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TokenRepository extends JpaRepository<Token, Long> {

    Optional<Token> findByTokenNumberAndTokenDate(String tokenNumber, LocalDate tokenDate);

    List<Token> findByPatientIdAndTokenDate(Long patientId, LocalDate tokenDate);

    List<Token> findByPatientId(Long patientId);

    List<Token> findByDoctorIdAndTokenDate(Long doctorId, LocalDate tokenDate);

    List<Token> findByDepartmentIdAndTokenDate(Long departmentId, LocalDate tokenDate);

    // Queue queries - ordered by priority and generation time
    @Query("SELECT t FROM Token t JOIN FETCH t.patient JOIN FETCH t.department LEFT JOIN FETCH t.doctor " +
           "WHERE t.doctor.id = :doctorId AND t.tokenDate = :date AND t.status IN ('WAITING', 'CALLED') " +
           "ORDER BY t.priorityScore DESC, t.generatedAt ASC")
    List<Token> findActiveQueueByDoctor(@Param("doctorId") Long doctorId, @Param("date") LocalDate date);

    @Query("SELECT t FROM Token t JOIN FETCH t.patient JOIN FETCH t.department LEFT JOIN FETCH t.doctor " +
           "WHERE t.department.id = :deptId AND t.tokenDate = :date AND t.status IN ('WAITING', 'CALLED') " +
           "ORDER BY t.priorityScore DESC, t.generatedAt ASC")
    List<Token> findActiveQueueByDepartment(@Param("deptId") Long departmentId, @Param("date") LocalDate date);

    // Current token being served (includes CALLED and IN_CONSULTATION)
    @Query("SELECT t FROM Token t JOIN FETCH t.patient JOIN FETCH t.department LEFT JOIN FETCH t.doctor " +
           "WHERE t.doctor.id = :doctorId AND t.tokenDate = :date AND t.status IN ('CALLED', 'IN_CONSULTATION') ORDER BY t.calledAt DESC")
    Optional<Token> findCurrentTokenByDoctor(@Param("doctorId") Long doctorId, @Param("date") LocalDate date);

    // Find any active token (for global check)
    @Query("SELECT t FROM Token t WHERE t.tokenDate = :date AND t.status IN ('CALLED', 'IN_CONSULTATION')")
    List<Token> findAllActiveTokens(@Param("date") LocalDate date);

    @Query("SELECT t FROM Token t WHERE t.department.id = :deptId AND t.tokenDate = :date AND t.status = 'IN_CONSULTATION'")
    List<Token> findCurrentTokensByDepartment(@Param("deptId") Long departmentId, @Param("date") LocalDate date);

    // Next waiting token
    @Query("SELECT t FROM Token t JOIN FETCH t.patient JOIN FETCH t.department LEFT JOIN FETCH t.doctor " +
           "WHERE t.doctor.id = :doctorId AND t.tokenDate = :date AND t.status = 'WAITING' " +
           "ORDER BY t.priorityScore DESC, t.generatedAt ASC LIMIT 1")
    Optional<Token> findNextWaitingTokenByDoctor(@Param("doctorId") Long doctorId, @Param("date") LocalDate date);

    // Count queries
    @Query("SELECT COUNT(t) FROM Token t WHERE t.doctor.id = :doctorId AND t.tokenDate = :date AND t.status = 'WAITING'")
    int countWaitingByDoctor(@Param("doctorId") Long doctorId, @Param("date") LocalDate date);

    @Query("SELECT COUNT(t) FROM Token t WHERE t.department.id = :deptId AND t.tokenDate = :date AND t.status = 'WAITING'")
    int countWaitingByDepartment(@Param("deptId") Long departmentId, @Param("date") LocalDate date);

    @Query("SELECT COUNT(t) FROM Token t WHERE t.doctor.id = :doctorId AND t.tokenDate = :date")
    int countTotalByDoctorAndDate(@Param("doctorId") Long doctorId, @Param("date") LocalDate date);

    @Query("SELECT COUNT(t) FROM Token t WHERE t.department.id = :deptId AND t.tokenDate = :date")
    int countTotalByDepartmentAndDate(@Param("deptId") Long departmentId, @Param("date") LocalDate date);

    // Position in queue
    @Query("SELECT COUNT(t) FROM Token t WHERE t.doctor.id = :doctorId AND t.tokenDate = :date AND t.status = 'WAITING' " +
           "AND (t.priorityScore > :priorityScore OR (t.priorityScore = :priorityScore AND t.generatedAt < :generatedAt))")
    int findPositionInDoctorQueue(@Param("doctorId") Long doctorId, @Param("date") LocalDate date, 
                                   @Param("priorityScore") Integer priorityScore, @Param("generatedAt") LocalDateTime generatedAt);

    // Average consultation time calculation
    @Query("SELECT AVG(TIMESTAMPDIFF(MINUTE, t.consultationStartedAt, t.consultationEndedAt)) FROM Token t " +
           "WHERE t.doctor.id = :doctorId AND t.status = 'COMPLETED' AND t.consultationEndedAt >= :since")
    Double calculateAverageConsultationTime(@Param("doctorId") Long doctorId, @Param("since") LocalDateTime since);

    // Completed tokens for statistics
    @Query("SELECT t FROM Token t WHERE t.doctor.id = :doctorId AND t.tokenDate = :date AND t.status = 'COMPLETED' " +
           "ORDER BY t.consultationEndedAt DESC")
    List<Token> findCompletedByDoctorAndDate(@Param("doctorId") Long doctorId, @Param("date") LocalDate date);

    // Token number generation helper
    @Query("SELECT COUNT(t) FROM Token t WHERE t.department.id = :deptId AND t.tokenDate = :date")
    int countByDepartmentAndDate(@Param("deptId") Long departmentId, @Param("date") LocalDate date);

    // All tokens for a date
    @Query("SELECT t FROM Token t JOIN FETCH t.patient JOIN FETCH t.department LEFT JOIN FETCH t.doctor " +
           "WHERE t.tokenDate = :date ORDER BY t.generatedAt DESC")
    List<Token> findAllByDateWithDetails(@Param("date") LocalDate date);

    // Statistics
    @Query("SELECT t.status, COUNT(t) FROM Token t WHERE t.tokenDate = :date GROUP BY t.status")
    List<Object[]> getStatusCountsByDate(@Param("date") LocalDate date);

    @Query("SELECT t.department.name, COUNT(t) FROM Token t WHERE t.tokenDate = :date GROUP BY t.department.name")
    List<Object[]> getDepartmentCountsByDate(@Param("date") LocalDate date);

    // All waiting tokens for notification service
    @Query("SELECT t FROM Token t JOIN FETCH t.patient JOIN FETCH t.department LEFT JOIN FETCH t.doctor " +
           "WHERE t.tokenDate = :date AND t.status = 'WAITING' ORDER BY t.priorityScore DESC, t.generatedAt ASC")
    List<Token> findAllWaitingTokensForToday(@Param("date") LocalDate date);

    // Waiting tokens by doctor ordered for queue
    @Query("SELECT t FROM Token t JOIN FETCH t.patient JOIN FETCH t.department JOIN FETCH t.doctor " +
           "WHERE t.doctor.id = :doctorId AND t.tokenDate = :date AND t.status = 'WAITING' " +
           "ORDER BY t.priorityScore DESC, t.generatedAt ASC")
    List<Token> findWaitingTokensByDoctor(@Param("doctorId") Long doctorId, @Param("date") LocalDate date);

    // Find token by ID with all relationships eagerly loaded
    @Query("SELECT t FROM Token t JOIN FETCH t.patient JOIN FETCH t.department LEFT JOIN FETCH t.doctor " +
           "WHERE t.id = :tokenId")
    Optional<Token> findByIdWithDetails(@Param("tokenId") Long tokenId);
}
