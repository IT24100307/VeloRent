package Group2.Car.Rental.System.controller;

import Group2.Car.Rental.System.dto.VehicleUsageHistoryDTO;
import Group2.Car.Rental.System.service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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

    /**
     * Get dashboard statistics including currently rented vehicles
     * @return dashboard statistics
     */
    @GetMapping("/dashboard-stats")
    public ResponseEntity<java.util.Map<String, Object>> getDashboardStats() {
        try {
            System.out.println("API endpoint called: /api/fleet/dashboard-stats");
            java.util.Map<String, Object> stats = bookingService.getDashboardStats();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            System.err.println("Error in dashboard stats endpoint: " + e.getMessage());
            e.printStackTrace();
            // Return default stats
            java.util.Map<String, Object> defaultStats = new java.util.HashMap<>();
            defaultStats.put("totalVehicles", 0);
            defaultStats.put("availableVehicles", 0);
            defaultStats.put("currentlyRentedVehicles", 0);
            defaultStats.put("maintenanceVehicles", 0);
            return ResponseEntity.ok(defaultStats);
        }
    }

    /**
     * Get current customer details for a rented vehicle
     * @param vehicleId the vehicle ID
     * @return current customer details if vehicle is rented
     */
    @GetMapping("/vehicle/{vehicleId}/current-customer")
    public ResponseEntity<java.util.Map<String, Object>> getCurrentCustomer(@PathVariable Integer vehicleId) {
        try {
            System.out.println("API endpoint called: /api/fleet/vehicle/" + vehicleId + "/current-customer");
            java.util.Map<String, Object> customerDetails = bookingService.getCurrentCustomerForVehicle(vehicleId);
            return ResponseEntity.ok(customerDetails);
        } catch (Exception e) {
            System.err.println("Error getting current customer for vehicle " + vehicleId + ": " + e.getMessage());
            e.printStackTrace();
            java.util.Map<String, Object> error = new java.util.HashMap<>();
            error.put("error", "Unable to retrieve customer details");
            return ResponseEntity.ok(error);
        }
    }
}