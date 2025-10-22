package Group2.Car.Rental.System.controller;

import Group2.Car.Rental.System.dto.NotificationDTO;
import Group2.Car.Rental.System.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Admin notifications aggregator.
 * Returns recent events: booking confirmations, payments, new vehicles, new users, feedback, etc.
 */
@RestController
@RequestMapping("/api/admin")
public class AdminNotificationController {

    @Autowired
    private NotificationService notificationService;

    @GetMapping("/notifications")
    public ResponseEntity<List<NotificationDTO>> getAdminNotifications(){
        try {
            return ResponseEntity.ok(notificationService.getRecentNotifications());
        } catch (Exception e){
            return ResponseEntity.ok(java.util.Collections.emptyList());
        }
    }
}
