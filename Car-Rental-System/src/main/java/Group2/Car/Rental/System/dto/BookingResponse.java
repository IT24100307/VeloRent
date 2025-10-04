package Group2.Car.Rental.System.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class BookingResponse {
    private Integer bookingId;
    private String bookingType;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private BigDecimal totalCost;
    private String bookingStatus;
    private String customerName;
    private String packageName;
    private String vehicleName;
    private LocalDateTime createdAt;

    // Default constructor
    public BookingResponse() {
    }

    // Constructor for package booking
    public BookingResponse(Integer bookingId, String bookingType, LocalDateTime startDate,
                          LocalDateTime endDate, BigDecimal totalCost, String bookingStatus,
                          String customerName, String packageName, LocalDateTime createdAt) {
        this.bookingId = bookingId;
        this.bookingType = bookingType;
        this.startDate = startDate;
        this.endDate = endDate;
        this.totalCost = totalCost;
        this.bookingStatus = bookingStatus;
        this.customerName = customerName;
        this.packageName = packageName;
        this.createdAt = createdAt;
    }
}
