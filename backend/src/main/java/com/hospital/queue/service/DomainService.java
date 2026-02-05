package com.hospital.queue.service;

import com.hospital.queue.dto.DomainDTO;
import com.hospital.queue.entity.Domain;
import com.hospital.queue.exception.ResourceNotFoundException;
import com.hospital.queue.repository.DomainRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DomainService {
    
    private final DomainRepository domainRepository;

    @Transactional(readOnly = true)
    public List<DomainDTO> getAllActiveDomains() {
        return domainRepository.findByActiveTrue().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public DomainDTO getDomainById(Long id) {
        Domain domain = domainRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Domain", "id", id));
        return convertToDTO(domain);
    }

    @Transactional
    public DomainDTO createDomain(DomainDTO domainDTO) {
        Domain domain = Domain.builder()
                .name(domainDTO.getName())
                .description(domainDTO.getDescription())
                .icon(domainDTO.getIcon())
                .active(true)
                .build();
        
        Domain saved = domainRepository.save(domain);
        return convertToDTO(saved);
    }

    private DomainDTO convertToDTO(Domain domain) {
        return DomainDTO.builder()
                .id(domain.getId())
                .name(domain.getName())
                .description(domain.getDescription())
                .icon(domain.getIcon())
                .active(domain.getActive())
                .build();
    }
}
