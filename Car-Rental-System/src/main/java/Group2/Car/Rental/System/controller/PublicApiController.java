package Group2.Car.Rental.System.controller;

import Group2.Car.Rental.System.dto.FeedbackDTO;
import Group2.Car.Rental.System.entity.VehiclePackage;
import Group2.Car.Rental.System.service.FeedbackService;
import Group2.Car.Rental.System.service.VehiclePackageService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
     * Get all activated packages for homepage display
     * This endpoint is publicly accessible and doesn't require authentication
     */
    @GetMapping("/packages")
    public ResponseEntity<List<VehiclePackage>> getActivatedPackages() {
        List<VehiclePackage> activatedPackages = vehiclePackageService.getActivatedPackages();
        return ResponseEntity.ok(activatedPackages);
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