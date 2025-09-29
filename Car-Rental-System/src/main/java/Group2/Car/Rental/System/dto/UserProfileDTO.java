package Group2.Car.Rental.System.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileDTO {
    // Core user information
    private Long userId;
    private String firstName;
    private String lastName;
    private String email;
    private String password; // Used only when updating password
    private String roleName; // Display role as name, not ID
    private boolean is2faEnabled;

    // Customer specific fields
    private String contactNumber;
    private String addressStreet;
    private String addressCity;
    private String addressPostalCode;

    // Staff specific fields
    private String staffIdCode;
    private String department;
    private String position;
    private String employeeId;

    // Flag to determine if user is a customer or staff
    private boolean isCustomer;
    private boolean isStaff;
}
