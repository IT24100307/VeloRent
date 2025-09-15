package Group2.Car.Rental.System.dto;

import lombok.Data;

@Data
public class ResetPasswordDto {
    private String email;
    private String otp;
    private String newPassword;
}
