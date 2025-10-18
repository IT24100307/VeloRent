package Group2.Car.Rental.System.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @GetMapping("/fleet-dashboard")
    @PreAuthorize("hasRole('ROLE_FLEET_MANAGER')")
    public ResponseEntity<String> getFleetDashboard() {
        return ResponseEntity.ok("Welcome Fleet Manager!");
    }

    @GetMapping("/system-dashboard")
    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN')")
    public ResponseEntity<String> getSystemDashboard() {
        return ResponseEntity.ok("Welcome System Admin!");
    }

    @GetMapping("/owner-dashboard")
    @PreAuthorize("hasRole('ROLE_OWNER')")
    public ResponseEntity<String> getOwnerDashboard() {
        return ResponseEntity.ok("Welcome Owner!");
    }

    // Secured endpoint that all admin types can access
    @GetMapping("/common")
    @PreAuthorize("hasAnyRole('ROLE_FLEET_MANAGER', 'ROLE_SYSTEM_ADMIN', 'ROLE_OWNER')")
    public ResponseEntity<String> getCommonAdminArea() {
        return ResponseEntity.ok("This is a common area for all admin types");
    }
}
