package Group2.Car.Rental.System.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class BookingRequest {
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private BigDecimal totalCost;
    private Integer vehicleId;
    private Integer customerId;

    // Default constructor
    public BookingRequest() {
    }

    // Constructor with parameters
    public BookingRequest(LocalDateTime startDate, LocalDateTime endDate, BigDecimal totalCost,
                         Integer vehicleId, Integer customerId) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.totalCost = totalCost;
        this.vehicleId = vehicleId;
        this.customerId = customerId;
    }
}
