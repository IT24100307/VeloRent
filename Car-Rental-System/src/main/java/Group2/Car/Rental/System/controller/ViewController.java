package Group2.Car.Rental.System.controller;

import Group2.Car.Rental.System.dto.FeedbackDTO;
import Group2.Car.Rental.System.service.FeedbackService;
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

    @GetMapping("/security-settings")
    public String securitySettings() {
        return "security-settings";
    }

    @GetMapping("/dashboard")
    public String dashboard() {
        // This is a placeholder. In a real application, you would check if the user is
        // authenticated and redirect to login if not.
        return "dashboard";
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

    @GetMapping("/feedback")
    public String listFeedbacks(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "5") int size, Model model) {
        Page<FeedbackDTO> feedbackPage = feedbackService.getAllFeedbacks(page, size);
        model.addAttribute("feedbacks", feedbackPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", feedbackPage.getTotalPages());
        model.addAttribute("newFeedback", new FeedbackDTO());
        return "feedback";
    }

    @GetMapping("/admin/feedback")
    public String listAdminFeedbacks(Model model) {
        List<FeedbackDTO> feedbacks = feedbackService.getAllFeedbacksForAdmin();
        model.addAttribute("feedbacks", feedbacks);
        return "admin/feedback";
    }
}
