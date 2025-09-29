package Group2.Car.Rental.System.controller;

import Group2.Car.Rental.System.dto.UserProfileDTO;
import Group2.Car.Rental.System.entity.User;
import Group2.Car.Rental.System.service.UserProfileService;
import Group2.Car.Rental.System.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/profile")
public class UserProfileController {

    @Autowired
    private UserProfileService userProfileService;

    @Autowired
    private UserService userService;

    /**
     * Display the profile page for the current logged-in user
     */
    @GetMapping
    public String viewOwnProfile(Model model, @RequestParam(required = false) String email,
                                @RequestParam(required = false) String success,
                                @RequestParam(required = false) String error) {
        try {
            // First try to get email from request parameter
            String userEmail = email;

            // If not provided in request, try to get from authentication context
            if (userEmail == null || userEmail.isEmpty()) {
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                // Check if user is authenticated
                if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
                    try {
                        return "redirect:/login?message=" +
                               java.net.URLEncoder.encode("Please log in to view your profile", "UTF-8") +
                               "&type=info";
                    } catch (Exception e) {
                        return "redirect:/login";
                    }
                }
                userEmail = auth.getName();
            }

            // If still no email, show error
            if (userEmail == null || userEmail.isEmpty() || "anonymousUser".equals(userEmail)) {
                try {
                    return "redirect:/login?message=" +
                           java.net.URLEncoder.encode("User not found. Please log in again.", "UTF-8") +
                           "&type=error";
                } catch (Exception e) {
                    return "redirect:/login";
                }
            }

            // Check if the user exists in the database
            if (!userService.existsByEmail(userEmail)) {
                try {
                    return "redirect:/login?message=" +
                           java.net.URLEncoder.encode("User account not found. Please log in again.", "UTF-8") +
                           "&type=error";
                } catch (Exception e) {
                    return "redirect:/login";
                }
            }

            UserProfileDTO profileDTO = userProfileService.getUserProfile(userEmail);
            model.addAttribute("profile", profileDTO);
            model.addAttribute("isOwnProfile", true);

            // Add success message if requested
            if (success != null && success.equals("true")) {
                model.addAttribute("successMessage", "Profile updated successfully!");
            }

            // Add error message if present
            if (error != null && !error.isEmpty()) {
                model.addAttribute("errorMessage", error);
            }

            return "user-profile";
        } catch (Exception e) {
            model.addAttribute("error", "Failed to load profile: " + e.getMessage());
            return "error";
        }
    }

    /**
     * Display a user profile by ID (admin access only)
     */
    @GetMapping("/{userId}")
    public String viewUserProfile(@PathVariable Long userId, Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        // Check if user is authenticated
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            try {
                return "redirect:/login?message=" +
                       java.net.URLEncoder.encode("Please log in to view this profile", "UTF-8") +
                       "&type=info";
            } catch (Exception e) {
                return "redirect:/login";
            }
        }

        String currentUserEmail = auth.getName();

        // Check if the user exists in the database
        if (!userService.existsByEmail(currentUserEmail)) {
            try {
                return "redirect:/login?message=" +
                       java.net.URLEncoder.encode("User account not found. Please log in again.", "UTF-8") +
                       "&type=error";
            } catch (Exception e) {
                return "redirect:/login";
            }
        }

        User currentUser = userService.getUserByEmail(currentUserEmail);

        // Check if the current user is null
        if (currentUser == null) {
            try {
                return "redirect:/login?message=" +
                       java.net.URLEncoder.encode("User account not found. Please log in again.", "UTF-8") +
                       "&type=error";
            } catch (Exception e) {
                return "redirect:/login";
            }
        }

        boolean isAdmin = auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN")) ||
                          auth.getAuthorities().contains(new SimpleGrantedAuthority("ADMIN")) ||
                          auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_SYSTEM_ADMIN"));

        // Check if viewing own profile
        boolean isOwnProfile = currentUser.getId().equals(userId);

        // Only allow if admin or own profile
        if (isAdmin || isOwnProfile) {
            try {
                UserProfileDTO profileDTO = userProfileService.getUserProfileById(userId);
                model.addAttribute("profile", profileDTO);
                model.addAttribute("isOwnProfile", isOwnProfile);
                return "user-profile";
            } catch (Exception e) {
                model.addAttribute("error", "Failed to load profile: " + e.getMessage());
                return "error";
            }
        } else {
            try {
                return "redirect:/profile?error=" +
                       java.net.URLEncoder.encode("Access denied. You can only view your own profile.", "UTF-8");
            } catch (Exception e) {
                return "redirect:/profile";
            }
        }
    }

    /**
     * Handle profile update submission
     */
    @PostMapping("/update")
    public String updateProfile(@ModelAttribute UserProfileDTO profileDTO,
                                RedirectAttributes redirectAttributes) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUserEmail = auth.getName();
        User currentUser = userService.getUserByEmail(currentUserEmail);
        boolean isAdmin = auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN")) ||
                          auth.getAuthorities().contains(new SimpleGrantedAuthority("ADMIN")) ||
                          auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_SYSTEM_ADMIN"));

        // Validate required fields
        if (profileDTO.getUserId() == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "User ID is required");
            return "redirect:/profile";
        }

        if (profileDTO.getFirstName() == null || profileDTO.getFirstName().trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "First name is required");
            return "redirect:/profile";
        }

        if (profileDTO.getLastName() == null || profileDTO.getLastName().trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Last name is required");
            return "redirect:/profile";
        }

        if (profileDTO.getEmail() == null || profileDTO.getEmail().trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Email is required");
            return "redirect:/profile";
        }

        // Only allow users to update their own profile or if they're an admin
        if (currentUser != null && (currentUser.getId().equals(profileDTO.getUserId()) || isAdmin)) {
            try {
                UserProfileDTO updatedProfile = userProfileService.updateUserProfile(profileDTO);
                redirectAttributes.addFlashAttribute("successMessage", "Profile updated successfully");

                // If own profile was updated, redirect to own profile
                if (currentUser.getId().equals(profileDTO.getUserId())) {
                    return "redirect:/profile?success=true";
                } else {
                    // If admin updated someone else's profile
                    return "redirect:/profile/" + profileDTO.getUserId() + "?success=true";
                }
            } catch (IllegalArgumentException e) {
                redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("errorMessage", "Failed to update profile: " + e.getMessage());
            }
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Access denied. You can only update your own profile.");
        }

        if (currentUser != null && currentUser.getId().equals(profileDTO.getUserId())) {
            return "redirect:/profile";
        } else {
            return "redirect:/profile/" + profileDTO.getUserId();
        }
    }

    /**
     * REST API endpoint for profile data (for AJAX requests)
     */
    @GetMapping("/api/data")
    @ResponseBody
    public ResponseEntity<?> getProfileData() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        try {
            UserProfileDTO profileDTO = userProfileService.getUserProfile(email);
            return ResponseEntity.ok(profileDTO);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "Failed to load profile: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * REST API endpoint to update profile (for AJAX requests)
     */
    @PutMapping("/api/update")
    @ResponseBody
    public ResponseEntity<?> updateProfileApi(@RequestBody UserProfileDTO profileDTO) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUserEmail = auth.getName();
        User currentUser = userService.getUserByEmail(currentUserEmail);
        boolean isAdmin = auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN")) ||
                          auth.getAuthorities().contains(new SimpleGrantedAuthority("ADMIN")) ||
                          auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_SYSTEM_ADMIN"));

        // Validate the incoming data
        if (profileDTO == null || profileDTO.getUserId() == null) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "Invalid profile data: missing required fields");
            return ResponseEntity.badRequest().body(response);
        }

        if (currentUser != null && (currentUser.getId().equals(profileDTO.getUserId()) || isAdmin)) {
            try {
                UserProfileDTO updatedProfile = userProfileService.updateUserProfile(profileDTO);

                // Make sure we're returning a valid object with at least the basic fields
                if (updatedProfile == null) {
                    updatedProfile = userProfileService.getUserProfileById(profileDTO.getUserId());
                }

                return ResponseEntity.ok(updatedProfile);
            } catch (IllegalArgumentException e) {
                Map<String, String> response = new HashMap<>();
                response.put("error", e.getMessage());
                return ResponseEntity.badRequest().body(response);
            } catch (Exception e) {
                Map<String, String> response = new HashMap<>();
                response.put("error", "Failed to update profile: " + e.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }
        } else {
            Map<String, String> response = new HashMap<>();
            response.put("error", "Access denied. You can only update your own profile.");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        }
    }

    /**
     * Handle profile update submission via AJAX with JSON data
     */
    @PostMapping(value = "/update", consumes = "application/json")
    @ResponseBody
    public ResponseEntity<?> updateProfileJson(@RequestBody UserProfileDTO profileDTO) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUserEmail = auth.getName();
        User currentUser = userService.getUserByEmail(currentUserEmail);
        boolean isAdmin = auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN")) ||
                          auth.getAuthorities().contains(new SimpleGrantedAuthority("ADMIN")) ||
                          auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_SYSTEM_ADMIN"));

        // Validate required fields
        if (profileDTO.getUserId() == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "User ID is required"));
        }

        if (profileDTO.getFirstName() == null || profileDTO.getFirstName().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "First name is required"));
        }

        if (profileDTO.getLastName() == null || profileDTO.getLastName().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Last name is required"));
        }

        if (profileDTO.getEmail() == null || profileDTO.getEmail().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email is required"));
        }

        // Only allow users to update their own profile or if they're an admin
        if (currentUser != null && (currentUser.getId().equals(profileDTO.getUserId()) || isAdmin)) {
            try {
                UserProfileDTO updatedProfile = userProfileService.updateUserProfile(profileDTO);
                return ResponseEntity.ok(updatedProfile);
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to update profile: " + e.getMessage()));
            }
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("error", "Access denied. You can only update your own profile."));
        }
    }
}
