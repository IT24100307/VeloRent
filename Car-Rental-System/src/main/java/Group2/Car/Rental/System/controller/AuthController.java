package Group2.Car.Rental.System.controller;

import Group2.Car.Rental.System.dto.*;
import Group2.Car.Rental.System.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponseDto> registerUser(@RequestBody RegisterDto registerDto) {
        try {
            authService.register(registerDto);
            return ResponseEntity.ok(new AuthResponseDto("User registered successfully!", null, true));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new AuthResponseDto(e.getMessage(), null, false));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> loginUser(@RequestBody LoginDto loginDto) {
        try {
            Map<String, String> response = authService.login(loginDto);
            
            if ("2FA required".equals(response.get("status"))) {
                return ResponseEntity.ok(new AuthResponseDto("Please complete 2FA verification.", null, true));
            }
            
            String token = response.get("token");
            String role = response.get("role");
            String redirect = response.get("redirect");
            
            return ResponseEntity.ok(new AuthResponseDto("Login successful!", token, true, role, redirect));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new AuthResponseDto("Invalid credentials.", null, false));
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<AuthResponseDto> forgotPassword(@RequestBody ForgotPasswordDto forgotPasswordDto) {
        try {
            String message = authService.forgotPassword(forgotPasswordDto.getEmail());
            return ResponseEntity.ok(new AuthResponseDto(message, null, true));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new AuthResponseDto(e.getMessage(), null, false));
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<AuthResponseDto> resetPassword(@RequestBody ResetPasswordDto resetPasswordDto) {
        try {
            String message = authService.resetPassword(resetPasswordDto);
            return ResponseEntity.ok(new AuthResponseDto(message, null, true));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new AuthResponseDto(e.getMessage(), null, false));
        }
    }

    @PostMapping("/2fa/setup")
    public ResponseEntity<?> setup2FA(Authentication authentication, @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            String email;
            
            // Try to get email from Authentication object
            if (authentication != null) {
                email = authentication.getName();
            } 
            // If Authentication is null, try to extract from JWT token
            else if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                email = authService.extractEmailFromToken(token);
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "message", "Authentication required"));
            }
            
            String qrCodeUri = authService.setup2FA(email);
            return ResponseEntity.ok(Map.of(
                "qrCodeUri", qrCodeUri, 
                "message", "Scan this QR code with your authenticator app.",
                "success", true
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false, 
                "message", "Failed to set up 2FA: " + e.getMessage()
            ));
        }
    }

    @PostMapping("/2fa/enable")
    public ResponseEntity<?> enable2FA(Authentication authentication, 
                                     @RequestHeader(value = "Authorization", required = false) String authHeader, 
                                     @RequestBody Map<String, String> payload) {
        try {
            String email;
            // Try to get email from Authentication object
            if (authentication != null) {
                email = authentication.getName();
            } 
            // If Authentication is null, try to extract from JWT token
            else if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                email = authService.extractEmailFromToken(token);
            }
            // Finally, try to get from the request body
            else if (payload.containsKey("email")) {
                email = payload.get("email");
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new AuthResponseDto("Authentication required", null, false));
            }
            
            authService.enable2FA(email, payload.get("code"));
            return ResponseEntity.ok(new AuthResponseDto("2FA enabled successfully!", null, true));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new AuthResponseDto(e.getMessage(), null, false));
        }
    }

    @PostMapping("/2fa/verify")
    public ResponseEntity<AuthResponseDto> verify2FA(@RequestBody Map<String, String> payload) {
        try {
            String email = payload.get("email");
            String code = payload.get("code");
            Map<String, String> verificationResult = authService.verify2FA(email, code);

            String token = verificationResult.get("token");
            String role = verificationResult.get("role");
            String redirect = verificationResult.get("redirect");

            return ResponseEntity.ok(new AuthResponseDto("Login successful!", token, true, role, redirect));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new AuthResponseDto(e.getMessage(), null, false));
        }
    }
}