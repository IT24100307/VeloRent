package Group2.Car.Rental.System.controller;

import Group2.Car.Rental.System.dto.UserProfileDTO;
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

        // Use either the email from the request parameter or from authentication
        String userEmail;
        boolean isAuthenticated = false;

        if (email != null && !email.isEmpty()) {
            userEmail = email;
        } else if (authentication != null && authentication.isAuthenticated()) {
            userEmail = authentication.getName();
            isAuthenticated = true;
        } else {
            // If no email provided and not authenticated, redirect to login
            return "redirect:/login?message=Please log in to view your profile&type=error";
        }

        // Check if user exists
        if (!userService.existsByEmail(userEmail)) {
            return "redirect:/dashboard?message=Profile not found&type=error";
        }

        // Get profile data
        UserProfileDTO profile = userService.getUserProfile(userEmail);

        model.addAttribute("profile", profile);
        model.addAttribute("isCustomer",
                isAuthenticated && authentication.getAuthorities().stream()
                        .anyMatch(auth -> auth.getAuthority().equals("CUSTOMER")));

        return "profile";
    }
}
