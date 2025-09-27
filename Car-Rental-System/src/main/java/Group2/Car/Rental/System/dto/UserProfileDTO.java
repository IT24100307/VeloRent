package Group2.Car.Rental.System.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileDTO {
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String addressStreet;
    private String addressCity;
    private String addressPostalCode;
    private String country; // This isn't in the database yet, but including for future expansion
}
