package Group2.Car.Rental.System.controller;

import Group2.Car.Rental.System.dto.VehicleUsageHistoryDTO;
import Group2.Car.Rental.System.service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/fleet")
public class FleetApiController {

    @Autowired
    private BookingService bookingService;

    /**
     * Test endpoint to check if API is working
     */
    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("Fleet API is working!");
    }

    /**
     * Get vehicle usage history for fleet manager dashboard
     * Temporarily allow access for testing
     */
    @GetMapping("/vehicle-usage-history")
    public ResponseEntity<List<VehicleUsageHistoryDTO>> getVehicleUsageHistory() {
        try {
            System.out.println("API endpoint called: /api/fleet/vehicle-usage-history");
            List<VehicleUsageHistoryDTO> usageHistory = bookingService.getVehicleUsageHistory();
            System.out.println("Returning " + usageHistory.size() + " records");
            return ResponseEntity.ok(usageHistory);
        } catch (Exception e) {
            System.err.println("Error in vehicle usage history endpoint: " + e.getMessage());
            e.printStackTrace();
            // Return empty list instead of error for testing
            return ResponseEntity.ok(new java.util.ArrayList<>());
        }
    }
}