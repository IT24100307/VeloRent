package Group2.Car.Rental.System.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class BookingSummaryDTO {
    private Integer bookingId;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String bookingStatus;
    private BigDecimal totalCost;
    private Integer vehicleId;
    private String vehicleMake;
    private String vehicleModel;
    private String registrationNumber;

    public BookingSummaryDTO() {}

    public BookingSummaryDTO(Integer bookingId, LocalDateTime startDate, LocalDateTime endDate, String bookingStatus,
                             BigDecimal totalCost, Integer vehicleId, String vehicleMake, String vehicleModel, String registrationNumber) {
        this.bookingId = bookingId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.bookingStatus = bookingStatus;
        this.totalCost = totalCost;
        this.vehicleId = vehicleId;
        this.vehicleMake = vehicleMake;
        this.vehicleModel = vehicleModel;
        this.registrationNumber = registrationNumber;
    }

    public Integer getBookingId() { return bookingId; }
    public void setBookingId(Integer bookingId) { this.bookingId = bookingId; }
    public LocalDateTime getStartDate() { return startDate; }
    public void setStartDate(LocalDateTime startDate) { this.startDate = startDate; }
    public LocalDateTime getEndDate() { return endDate; }
    public void setEndDate(LocalDateTime endDate) { this.endDate = endDate; }
    public String getBookingStatus() { return bookingStatus; }
    public void setBookingStatus(String bookingStatus) { this.bookingStatus = bookingStatus; }
    public BigDecimal getTotalCost() { return totalCost; }
    public void setTotalCost(BigDecimal totalCost) { this.totalCost = totalCost; }
    public Integer getVehicleId() { return vehicleId; }
    public void setVehicleId(Integer vehicleId) { this.vehicleId = vehicleId; }
    public String getVehicleMake() { return vehicleMake; }
    public void setVehicleMake(String vehicleMake) { this.vehicleMake = vehicleMake; }
    public String getVehicleModel() { return vehicleModel; }
    public void setVehicleModel(String vehicleModel) { this.vehicleModel = vehicleModel; }
    public String getRegistrationNumber() { return registrationNumber; }
    public void setRegistrationNumber(String registrationNumber) { this.registrationNumber = registrationNumber; }
}
