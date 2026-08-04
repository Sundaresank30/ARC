package com.arc.embossing.service;

import com.arc.embossing.dto.EmbossingProgressDTO;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import org.springframework.transaction.support.TransactionSynchronizationManager;

@Component
public class EmbossingProgressPublisher {
    public static final String TOPIC = "/topic/embossing-progress";

    private final ApplicationEventPublisher eventPublisher;
    private final SimpMessagingTemplate messagingTemplate;

    public EmbossingProgressPublisher(ApplicationEventPublisher eventPublisher, SimpMessagingTemplate messagingTemplate) {
        this.eventPublisher = eventPublisher;
        this.messagingTemplate = messagingTemplate;
    }

    /** Queues publishing until the enclosing transaction is committed, or sends immediately if outside transaction. */
    public void publishAfterCommit(EmbossingProgressDTO progress) {
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            eventPublisher.publishEvent(new EmbossingProgressUpdatedEvent(progress));
        } else {
            messagingTemplate.convertAndSend(TOPIC, progress);
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void publishCommittedProgress(EmbossingProgressUpdatedEvent event) {
        messagingTemplate.convertAndSend(TOPIC, event.progress());
    }
}
