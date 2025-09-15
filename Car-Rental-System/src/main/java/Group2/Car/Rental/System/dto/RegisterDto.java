package Group2.Car.Rental.System.dto;

import lombok.Data;

@Data
public class RegisterDto {
    // User details
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private String roleName = "ROLE_CUSTOMER"; // Default role
    private String registrationCode; // Optional, used for admin roles

    // Customer details (for ROLE_CUSTOMER)
    private String contactNumber;
    private String addressStreet;
    private String addressCity;
    private String addressPostalCode;

    // Staff details (for admin roles)
    private String staffIdCode;
}