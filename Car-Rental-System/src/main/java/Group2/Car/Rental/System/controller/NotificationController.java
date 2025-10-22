package Group2.Car.Rental.System.controller;

import Group2.Car.Rental.System.dto.NotificationDTO;
import Group2.Car.Rental.System.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
// Note: Admin notifications are already provided by AdminNotificationController at /api/admin/notifications.
// To avoid mapping conflicts, this controller is moved under /api/admin/extra and is unused.
@RequestMapping("/api/admin/extra")
public class NotificationController {

    private final NotificationService notificationService;

    @Autowired
    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping("/notifications")
    public ResponseEntity<List<NotificationDTO>> getNotifications() {
        List<NotificationDTO> notifications = notificationService.getRecentNotifications();
        return ResponseEntity.ok(notifications);
    }
}
