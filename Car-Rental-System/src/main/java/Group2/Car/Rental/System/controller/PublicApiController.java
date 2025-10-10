package Group2.Car.Rental.System.controller;

import Group2.Car.Rental.System.dto.FeedbackDTO;
import Group2.Car.Rental.System.entity.VehiclePackage;
import Group2.Car.Rental.System.service.FeedbackService;
import Group2.Car.Rental.System.service.VehiclePackageService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/public")
public class PublicApiController {

    private final VehiclePackageService vehiclePackageService;
    private final FeedbackService feedbackService;

    public PublicApiController(VehiclePackageService vehiclePackageService, FeedbackService feedbackService) {
        this.vehiclePackageService = vehiclePackageService;
        this.feedbackService = feedbackService;
    }

    /**
     * Get all visible packages for homepage display (including partially reserved ones)
     * This endpoint is publicly accessible and doesn't require authentication
     */
    @GetMapping("/packages")
    public ResponseEntity<List<VehiclePackage>> getActivatedPackages() {
        List<VehiclePackage> visiblePackages = vehiclePackageService.getVisiblePackages();
        return ResponseEntity.ok(visiblePackages);
    }

    /**
     * Get package availability status and message
     */
    @GetMapping("/packages/{packageId}/availability")
    public ResponseEntity<Map<String, Object>> getPackageAvailability(@PathVariable Integer packageId) {
        Map<String, Object> response = new HashMap<>();
        boolean isAvailable = vehiclePackageService.isPackageAvailableForBooking(packageId);
        String message = vehiclePackageService.getPackageAvailabilityMessage(packageId);
        
        response.put("available", isAvailable);
        response.put("message", message);
        response.put("packageId", packageId);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get recent customer feedback for homepage display
     * This endpoint is publicly accessible and doesn't require authentication
     * Returns the most recent feedback with positive ratings for testimonials
     */
    @GetMapping("/feedback")
    public ResponseEntity<List<FeedbackDTO>> getRecentFeedback(@RequestParam(defaultValue = "0") int page, 
                                                               @RequestParam(defaultValue = "6") int size) {
        Page<FeedbackDTO> feedbackPage = feedbackService.getAllFeedbacks(page, size);
        return ResponseEntity.ok(feedbackPage.getContent());
    }
}