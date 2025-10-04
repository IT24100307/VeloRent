package Group2.Car.Rental.System.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class PackageBookingRequest {
    private Integer packageId;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String customerName;
    private String customerEmail;
    private String customerPhone;
    private String customerAddress;
    private String paymentMethod;

    // Default constructor
    public PackageBookingRequest() {
    }

    // Constructor
    public PackageBookingRequest(Integer packageId, LocalDateTime startDate, LocalDateTime endDate,
                               String customerName, String customerEmail, String customerPhone,
                               String customerAddress, String paymentMethod) {
        this.packageId = packageId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.customerName = customerName;
        this.customerEmail = customerEmail;
        this.customerPhone = customerPhone;
        this.customerAddress = customerAddress;
        this.paymentMethod = paymentMethod;
    }
}
