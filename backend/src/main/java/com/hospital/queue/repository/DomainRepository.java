package com.hospital.queue.repository;

import com.hospital.queue.entity.Domain;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DomainRepository extends JpaRepository<Domain, Long> {
    List<Domain> findByActiveTrue();
    Optional<Domain> findByName(String name);
}
