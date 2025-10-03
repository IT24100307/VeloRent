package Group2.Car.Rental.System.controller;

import Group2.Car.Rental.System.dto.UserProfileDTO;
import Group2.Car.Rental.System.entity.User;
import Group2.Car.Rental.System.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/profile")
public class ProfileController {

    @Autowired
    private UserService userService;

    @GetMapping
    public String viewProfile(
            Model model,
            Authentication authentication,
            @RequestParam(required = false) String email) {

        // Determine which user's profile to show: either explicit email or the authenticated user
        String userEmail = null;
        if (email != null && !email.isEmpty()) {
            userEmail = email;
        } else if (authentication != null && authentication.isAuthenticated()) {
            userEmail = authentication.getName();
        } else {
            // If no email provided and not authenticated, redirect to login
            return "redirect:/login?message=Please log in to view your profile&type=error";
        }

        // Check if user exists
        if (!userService.existsByEmail(userEmail)) {
            return "redirect:/dashboard?message=Profile not found&type=error";
        }

        // Fetch profile data for the requested user
        UserProfileDTO profile = userService.getUserProfile(userEmail);
        model.addAttribute("profile", profile);

        // Determine if the requested user is a customer (not just the authenticated principal)
        boolean isCustomer = false;
        try {
            User targetUser = userService.getUserByEmail(userEmail);
            if (targetUser != null) {
                // Consider either role name or existence of a Customer record
                String roleName = (targetUser.getRole() != null) ? targetUser.getRole().getName() : null;
                if (roleName != null) {
                    String rn = roleName.trim();
                    isCustomer = rn.equals("CUSTOMER") || rn.equals("ROLE_CUSTOMER") || rn.contains("CUSTOMER");
                }
                // Fallback: if a Customer record exists, also treat as customer
                if (!isCustomer && targetUser.getCustomer() != null) {
                    isCustomer = true;
                }
            }
        } catch (Exception ignored) {
            // If anything goes wrong determining role, default to non-customer
        }

        model.addAttribute("isCustomer", isCustomer);

        return "profile";
    }
}
