package Group2.Car.Rental.System.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ViewController {

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

    @GetMapping("/account-settings")
    public String accountSettings() {
        return "account-settings";
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
}
