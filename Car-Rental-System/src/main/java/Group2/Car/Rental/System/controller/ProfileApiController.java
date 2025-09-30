package Group2.Car.Rental.System.controller;

import Group2.Car.Rental.System.dto.ChangePasswordDto;
import Group2.Car.Rental.System.dto.UserProfileDTO;
import Group2.Car.Rental.System.service.ProfileService;
import Group2.Car.Rental.System.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/profile")
public class ProfileApiController {

    @Autowired
    private UserService userService;

    @Autowired
    private ProfileService profileService;

    @GetMapping
    public ResponseEntity<?> getUserProfile(
            Authentication authentication,
            @RequestParam(required = false) String email) {
        try {
            String userEmail = getUserEmail(authentication, email);
            if (userEmail == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("success", false, "message", "Authentication required"));
            }

            // Use the new profileService to get user profile
            UserProfileDTO profileDto = profileService.getUserProfile(userEmail);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "profile", profileDto
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PostMapping("/update")
    public ResponseEntity<?> updateUserProfile(
            Authentication authentication,
            @RequestParam(required = false) String email,
            @RequestBody UserProfileDTO profileDto) {
        try {
            String userEmail = getUserEmail(authentication, email);
            if (userEmail == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("success", false, "message", "Authentication required"));
            }

            // Log the update attempt
            System.out.println("ProfileApiController: Profile update requested for: " + userEmail);

            // Use the new profileService instead of userService
            profileService.updateProfile(userEmail, profileDto);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Profile updated successfully"
            ));
        } catch (Exception e) {
            System.err.println("ProfileApiController: Error updating profile: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Error updating profile: " + e.getMessage()));
        }
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(
            Authentication authentication,
            @RequestParam(required = false) String email,
            @RequestBody ChangePasswordDto passwordDto) {
        try {
            String userEmail = getUserEmail(authentication, email);
            if (userEmail == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("success", false, "message", "Authentication required"));
            }

            userService.changePassword(userEmail, passwordDto);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Password changed successfully"
            ));
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("success", false, "message", "Current password is incorrect"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    /**
     * Helper method to get user email from either authentication or request parameter
     */
    private String getUserEmail(Authentication authentication, String email) {
        if (email != null && !email.isEmpty()) {
            return email;
        } else if (authentication != null && authentication.isAuthenticated()) {
            return authentication.getName();
        }
        return null;
    }
}
