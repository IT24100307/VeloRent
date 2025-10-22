package Group2.Car.Rental.System.controller;

import Group2.Car.Rental.System.dto.DashboardDataDto;
import Group2.Car.Rental.System.dto.FeedbackDTO;
import Group2.Car.Rental.System.service.DashboardService;
import Group2.Car.Rental.System.service.FeedbackService;
import Group2.Car.Rental.System.service.VehiclePackageService;
import Group2.Car.Rental.System.entity.VehiclePackage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class ViewController {

    @Autowired
    private FeedbackService feedbackService;
    
    @Autowired
    private VehiclePackageService vehiclePackageService;

    @Autowired
    private DashboardService dashboardService;

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String register() {
        return "register";
    }

    @GetMapping("/forgot-password")
    public String forgotPassword() {
        return "forgot-password";
    }

    @GetMapping("/reset-password")
    public String resetPassword() {
        return "reset-password";
    }

    @GetMapping("/verify-2fa")
    public String verify2FA() {
        return "verify-2fa";
    }

    @GetMapping("/logout")
    public String logoutPage() {
        // Renders a tiny page that clears client auth and redirects to login
        return "logout";
    }

    @GetMapping("/security-settings")
    public String securitySettings() {
        return "security-settings";
    }

    // Alias path for admins to reach the same security settings page from the admin area
    @GetMapping("/admin/security-settings")
    public String adminSecuritySettings() {
        return "security-settings";
    }

    @GetMapping("/dashboard")
    public String dashboard() {
        // This is a placeholder. In a real application, you would check if the user is
        // authenticated and redirect to login if not.
        return "dashboard";
    }

    @GetMapping("/packages")
    public String packages(Model model) {
        try {
            List<VehiclePackage> activePackages = vehiclePackageService.getVisiblePackages();
            model.addAttribute("packages", activePackages);
        } catch (Exception e) {
            model.addAttribute("packages", java.util.Collections.emptyList());
            model.addAttribute("error", "Unable to load packages at this time. Please try again later.");
        }
        return "packages";
    }

    // Return the index page for the root path
    @GetMapping("/")
    public String root() {
        return "index";
    }
    
    // Admin dashboard
    @GetMapping("/admin/dashboard")
    public String adminDashboard() {
        return "admin/dashboard";
    }

    @GetMapping("/admin/offers")
    public String adminOffer() {
        return "admin/offer";
    }

    @GetMapping("/admin/system-dashboard")
    public String systemAdminDashboard() {
        return "admin/dashboard"; // Using the existing dashboard template or create a specific one if needed
    }

    @GetMapping("/admin/owner-dashboard")
    public String ownerDashboard() {
        return "admin/dashboard"; // Using the existing dashboard template or create a specific one if needed
    }

    @GetMapping("/admin/fleet-dashboard")
    public String fleetDashboard() {
        return "admin/dashboard"; // Using the existing dashboard template or create a specific one if needed
    }

    @GetMapping("/admin/login-history")
    public String loginHistory() {
        return "admin/login-history-simple";
    }

    @GetMapping("/feedback")
    public String listFeedbacks(@RequestParam(defaultValue = "0") int page, 
                              @RequestParam(defaultValue = "10") int size, 
                              Model model) {
        try {
            // Validate page parameters
            if (page < 0) page = 0;
            if (size < 1 || size > 50) size = 10; // Limit size to prevent performance issues
            
            Page<FeedbackDTO> feedbackPage = feedbackService.getAllFeedbacks(page, size);
            
            model.addAttribute("feedbacks", feedbackPage.getContent());
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", feedbackPage.getTotalPages());
            model.addAttribute("totalElements", feedbackPage.getTotalElements());
            model.addAttribute("hasNext", feedbackPage.hasNext());
            model.addAttribute("hasPrevious", feedbackPage.hasPrevious());
            model.addAttribute("newFeedback", new FeedbackDTO());
            
        } catch (Exception e) {
            // Log the error and provide a fallback
            model.addAttribute("feedbacks", java.util.Collections.emptyList());
            model.addAttribute("currentPage", 0);
            model.addAttribute("totalPages", 0);
            model.addAttribute("error", "Unable to load feedback at this time. Please try again later.");
        }
        
        return "feedback";
    }

    @GetMapping("/admin/feedback")
    public String listAdminFeedbacks(Model model) {
        List<FeedbackDTO> feedbacks = feedbackService.getAllFeedbacksForAdmin();
        model.addAttribute("feedbacks", feedbacks);
        return "admin/feedback";
    }

    @GetMapping("/rental-history")
    public String rentalHistory() {
        return "rental-history";
    }

    // New: admin payments page
    @GetMapping("/admin/payments")
    public String adminPayments() {
        return "admin/payments";
    }

    @GetMapping("/owner/dashboard")
    public String dashboard(Model model) {
        DashboardDataDto data = dashboardService.getDashboardData();
        model.addAttribute("data", data);
        return "/owner/dashboard"; // Thymeleaf template
    }
}
