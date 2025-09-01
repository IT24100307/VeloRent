package Group2.Car.Rental.System.dto;

import lombok.Data;

@Data
public class RegisterDto {
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private String roleName = "ROLE_CUSTOMER"; // Default role
    private String registrationCode; // Optional, used for admin roles
}