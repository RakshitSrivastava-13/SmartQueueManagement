package com.hospital.queue.service;

import com.hospital.queue.entity.Token;
import com.hospital.queue.repository.TokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Event-driven queue notification service.
 * Sends email notifications only when specific queue events occur:
 * 1. Token generated - handled by TokenService
 * 2. Queue position changes (advancement or due to priority insertion)
 * 3. User becomes first in queue
 * 4. Service completed - handled by StaffService
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class QueueNotificationService {

    private final TokenRepository tokenRepository;
    private final EmailService emailService;

    // Track the last notified position for each token to detect changes
    private final Map<Long, Integer> lastNotifiedPosition = new ConcurrentHashMap<>();

    /**
     * Called when a new token is generated.
     * Records the initial position for future change detection.
     */
    public void recordInitialPosition(Token token) {
        if (token.getDoctor() == null) {
            return;
        }
        
        int position = calculateQueuePosition(token);
        lastNotifiedPosition.put(token.getId(), position);
        log.debug("Recorded initial position {} for token {}", position, token.getTokenNumber());
    }

    /**
     * Called when a consultation ends or a token is cancelled/no-show.
     * Notifies all affected patients about their new queue position.
     * Runs asynchronously to avoid transaction conflicts.
     */
    @Async
    @Transactional(readOnly = true)
    public void notifyQueueAdvancement(Long doctorId) {
        try {
            // Small delay to ensure the calling transaction has committed
            Thread.sleep(500);
            
            List<Token> waitingTokens = tokenRepository.findWaitingTokensByDoctor(doctorId, LocalDate.now());
            
            int position = 1;
            for (Token token : waitingTokens) {
                if (token.getPatient().getEmail() == null) {
                    position++;
                    continue;
                }
                
                Integer previousPosition = lastNotifiedPosition.get(token.getId());
                
                // Only notify if position actually changed (moved forward)
                if (previousPosition != null && position < previousPosition) {
                    int estimatedWaitMinutes = calculateEstimatedWaitTime(token, position);
                    
                    // Send appropriate notification based on new position
                    if (position == 1) {
                        // User is now first in queue - special notification
                        emailService.sendQueueUpdateEmail(token, position, estimatedWaitMinutes);
                        log.info("First-in-queue notification sent for token {} (moved from {} to 1)", 
                                token.getTokenNumber(), previousPosition);
                    } else {
                        // Regular position advancement
                        emailService.sendQueueUpdateEmail(token, position, estimatedWaitMinutes);
                        log.info("Queue advancement notification sent for token {} (moved from {} to {})", 
                                token.getTokenNumber(), previousPosition, position);
                    }
                    
                    lastNotifiedPosition.put(token.getId(), position);
                } else if (previousPosition == null) {
                    // First time tracking this token, just record position
                    lastNotifiedPosition.put(token.getId(), position);
                }
                
                position++;
            }
        } catch (Exception e) {
            log.error("Error notifying queue advancement for doctor {}", doctorId, e);
        }
    }

    /**
     * Called when a priority token is inserted into the queue.
     * Notifies affected patients that their position has moved back with explanation.
     * Runs asynchronously to avoid transaction conflicts.
     */
    @Async
    @Transactional(readOnly = true)
    public void notifyPriorityInsertion(Long doctorId, Token priorityToken) {
        try {
            // Small delay to ensure the calling transaction has committed
            Thread.sleep(500);
            
            List<Token> waitingTokens = tokenRepository.findWaitingTokensByDoctor(doctorId, LocalDate.now());
            
            String priorityType = getPriorityTypeDescription(priorityToken.getPriority());
            String reason = String.format("A %s case has been added to the queue and given priority as per our hospital's queue policy.", 
                    priorityType);
            
            int position = 1;
            for (Token token : waitingTokens) {
                // Skip the priority token itself and tokens without email
                if (token.getPatient().getEmail() == null || token.getId().equals(priorityToken.getId())) {
                    if (!token.getId().equals(priorityToken.getId())) {
                        position++;
                    }
                    continue;
                }
                
                Integer previousPosition = lastNotifiedPosition.get(token.getId());
                
                // Only notify if position actually moved back
                if (previousPosition != null && position > previousPosition) {
                    int estimatedWaitMinutes = calculateEstimatedWaitTime(token, position);
                    emailService.sendQueueUpdateEmail(token, position, estimatedWaitMinutes, previousPosition, reason);
                    
                    lastNotifiedPosition.put(token.getId(), position);
                    
                    log.info("Priority insertion notification sent for token {} (moved from {} to {})", 
                            token.getTokenNumber(), previousPosition, position);
                }
                
                position++;
            }
        } catch (Exception e) {
            log.error("Error notifying priority insertion for doctor {}", doctorId, e);
        }
    }

    /**
     * Called when a token is cancelled by the user.
     * Triggers queue advancement notifications for other patients.
     * Runs asynchronously to avoid transaction conflicts.
     */
    @Async
    public void notifyTokenCancellation(Token cancelledToken) {
        if (cancelledToken.getDoctor() != null) {
            // Remove from tracking
            lastNotifiedPosition.remove(cancelledToken.getId());
            
            // Notify remaining patients about advancement
            notifyQueueAdvancement(cancelledToken.getDoctor().getId());
        }
    }

    /**
     * Called when a token's priority is changed.
     * Recalculates positions and notifies affected patients.
     * Runs asynchronously to avoid transaction conflicts.
     */
    @Async
    @Transactional(readOnly = true)
    public void notifyPriorityChange(Token changedToken) {
        if (changedToken.getDoctor() == null) {
            return;
        }
        
        try {
            // Small delay to ensure the calling transaction has committed
            Thread.sleep(500);
            
            List<Token> waitingTokens = tokenRepository.findWaitingTokensByDoctor(
                    changedToken.getDoctor().getId(), LocalDate.now());
            
            String reason = "Queue order has been adjusted based on priority updates.";
            
            int position = 1;
            for (Token token : waitingTokens) {
                if (token.getPatient().getEmail() == null) {
                    position++;
                    continue;
                }
                
                Integer previousPosition = lastNotifiedPosition.get(token.getId());
                
                if (previousPosition != null && !previousPosition.equals(position)) {
                    int estimatedWaitMinutes = calculateEstimatedWaitTime(token, position);
                    
                    if (position > previousPosition) {
                        // Position moved back
                        emailService.sendQueueUpdateEmail(token, position, estimatedWaitMinutes, previousPosition, reason);
                    } else {
                        // Position moved forward
                        emailService.sendQueueUpdateEmail(token, position, estimatedWaitMinutes);
                    }
                    
                    lastNotifiedPosition.put(token.getId(), position);
                    log.info("Priority change notification sent for token {} (position: {} -> {})", 
                            token.getTokenNumber(), previousPosition, position);
                }
                
                position++;
            }
        } catch (Exception e) {
            log.error("Error notifying priority change", e);
        }
    }

    /**
     * Called when a patient is marked as no-show or skipped.
     * Notifies other patients about queue advancement.
     * Runs asynchronously to avoid transaction conflicts.
     */
    @Async
    public void notifyNoShowOrSkip(Token token) {
        if (token.getDoctor() != null) {
            notifyQueueAdvancement(token.getDoctor().getId());
        }
    }

    /**
     * Removes a token from position tracking (called when token is completed/cancelled)
     */
    public void removeFromTracking(Long tokenId) {
        lastNotifiedPosition.remove(tokenId);
    }

    /**
     * Calculates the current position in queue for a token
     */
    private int calculateQueuePosition(Token token) {
        if (token.getDoctor() == null) {
            return 0;
        }
        
        return tokenRepository.findPositionInDoctorQueue(
                token.getDoctor().getId(),
                token.getTokenDate(),
                token.getPriorityScore(),
                token.getGeneratedAt()
        ) + 1; // +1 because the query returns 0-indexed position
    }

    /**
     * Calculates estimated wait time based on position and average consultation time
     */
    private int calculateEstimatedWaitTime(Token token, int position) {
        if (token.getDoctor() == null) {
            return 0;
        }
        
        Double avgTime = tokenRepository.calculateAverageConsultationTime(
                token.getDoctor().getId(),
                LocalDateTime.now().minusDays(30)
        );
        
        int consultationTime = avgTime != null ? avgTime.intValue() : 
                token.getDoctor().getConsultationDurationMinutes();
        
        return (position - 1) * consultationTime;
    }

    /**
     * Gets a human-readable description of the priority type
     */
    private String getPriorityTypeDescription(Token.Priority priority) {
        if (priority == Token.Priority.EMERGENCY) {
            return "Emergency";
        } else if (priority == Token.Priority.SENIOR_CITIZEN) {
            return "Senior Citizen (65+)";
        } else if (priority == Token.Priority.PREGNANT) {
            return "Pregnant Woman";
        } else if (priority == Token.Priority.VIP) {
            return "VIP";
        } else {
            return "Priority";
        }
    }
}
