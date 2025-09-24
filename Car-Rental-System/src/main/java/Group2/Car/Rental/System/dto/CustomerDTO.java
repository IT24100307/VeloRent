package Group2.Car.Rental.System.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerDTO {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String contactNumber;
    private String addressStreet;
    private String addressCity;
    private String addressPostalCode;
    private boolean is2faEnabled;
}
