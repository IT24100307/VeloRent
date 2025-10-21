package Group2.Car.Rental.System.dto;

import lombok.Data;

@Data
public class CreateAdminUserDTO {
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    // One of: ROLE_FLEET_MANAGER, ROLE_SYSTEM_ADMIN, ROLE_OWNER
    private String roleName;

    // Optional staff metadata
    private String staffIdCode;
    private String department;
    private String position;
    private String employeeId;
}
