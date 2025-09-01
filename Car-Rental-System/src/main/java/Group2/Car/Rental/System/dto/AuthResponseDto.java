package Group2.Car.Rental.System.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AuthResponseDto {
    private String message;
    private String token; // Optional: used for login success
    private boolean success;
    private String role; // User's role
    private String redirect; // Redirect URL based on role
    
    // Constructor for backward compatibility
    public AuthResponseDto(String message, String token, boolean success) {
        this.message = message;
        this.token = token;
        this.success = success;
    }
    
    // Full constructor
    public AuthResponseDto(String message, String token, boolean success, String role, String redirect) {
        this.message = message;
        this.token = token;
        this.success = success;
        this.role = role;
        this.redirect = redirect;
    }
}