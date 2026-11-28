package com.findit.notification;

import com.findit.auth.UserPrincipal;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications")
@Tag(name = "Notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public List<NotificationResponse> listOwn(@AuthenticationPrincipal UserPrincipal principal) {
        return notificationService.listOwn(principal.getUser());
    }

    @PutMapping("/{id}/read")
    @PreAuthorize("isAuthenticated()")
    public NotificationResponse markRead(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal principal) {
        return notificationService.markRead(id, principal.getUser());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> delete(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal principal) {
        notificationService.delete(id, principal.getUser());
        return ResponseEntity.noContent().build();
    }
}
