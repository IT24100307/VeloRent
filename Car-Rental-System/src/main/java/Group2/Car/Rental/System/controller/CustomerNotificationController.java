package Group2.Car.Rental.System.controller;

import Group2.Car.Rental.System.dto.NotificationDTO;
import Group2.Car.Rental.System.service.CustomerNotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/customer")
public class CustomerNotificationController {

    @Autowired
    private CustomerNotificationService customerNotificationService;

    @GetMapping("/notifications")
    public ResponseEntity<List<NotificationDTO>> getCustomerNotifications(Authentication authentication) {
        try {
            String email = authentication != null ? authentication.getName() : null;
            List<NotificationDTO> list = customerNotificationService.getNotificationsForUserEmail(email);
            return ResponseEntity.ok(list);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok(Collections.emptyList());
        }
    }
}

