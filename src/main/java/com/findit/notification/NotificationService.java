package com.findit.notification;

import com.findit.common.ForbiddenException;
import com.findit.common.ResourceNotFoundException;
import com.findit.user.User;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Creates notifications through the {@link NotificationFactory}, delivers them through the {@link NotificationSender}
 * adapter and lets users list, mark and delete their own notifications.
 */
@Service
@Transactional
public class NotificationService {

    private final NotificationFactory factory;
    private final NotificationSender sender;
    private final NotificationRepository repository;

    public NotificationService(NotificationFactory factory, NotificationSender sender,
                               NotificationRepository repository) {
        this.factory = factory;
        this.sender = sender;
        this.repository = repository;
    }

    public void notify(NotificationType type, User recipient, NotificationContext context) {
        sender.send(factory.create(type, recipient, context));
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> listOwn(User actor) {
        return repository.findByRecipientIdOrderByCreatedAtDesc(actor.getId()).stream()
                .map(NotificationResponse::from)
                .toList();
    }

    public NotificationResponse markRead(Long id, User actor) {
        Notification notification = findOwn(id, actor);
        notification.setRead(true);
        return NotificationResponse.from(repository.save(notification));
    }

    public void delete(Long id, User actor) {
        repository.delete(findOwn(id, actor));
    }

    private Notification findOwn(Long id, User actor) {
        Notification notification = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found: " + id));
        if (!notification.getRecipient().getId().equals(actor.getId())) {
            throw new ForbiddenException("This notification belongs to another user");
        }
        return notification;
    }
}
