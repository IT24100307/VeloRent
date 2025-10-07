package Group2.Car.Rental.System.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class VehicleUsageHistoryDTO {
    private Integer vehicleId;
    private String vehicleName;
    private String vehicleImage;
    private String registrationNumber;
    private Integer totalBookings;
    private Integer completedBookings;
    private Integer activeBookings;
    private Integer cancelledBookings;
    private BigDecimal totalRevenue;
    private LocalDateTime lastBookingDate;
    private String mostFrequentCustomer;
    private Double averageBookingDuration; // in days
    private String utilizationStatus; // High, Medium, Low

    // Constructors
    public VehicleUsageHistoryDTO() {}

    public VehicleUsageHistoryDTO(Integer vehicleId, String vehicleName, String vehicleImage, 
                                 String registrationNumber, Integer totalBookings, 
                                 Integer completedBookings, Integer activeBookings, 
                                 Integer cancelledBookings, BigDecimal totalRevenue, 
                                 LocalDateTime lastBookingDate, String mostFrequentCustomer, 
                                 Double averageBookingDuration) {
        this.vehicleId = vehicleId;
        this.vehicleName = vehicleName;
        this.vehicleImage = vehicleImage;
        this.registrationNumber = registrationNumber;
        this.totalBookings = totalBookings;
        this.completedBookings = completedBookings;
        this.activeBookings = activeBookings;
        this.cancelledBookings = cancelledBookings;
        this.totalRevenue = totalRevenue;
        this.lastBookingDate = lastBookingDate;
        this.mostFrequentCustomer = mostFrequentCustomer;
        this.averageBookingDuration = averageBookingDuration;
        
        // Calculate utilization status based on total bookings
        if (totalBookings >= 10) {
            this.utilizationStatus = "High";
        } else if (totalBookings >= 5) {
            this.utilizationStatus = "Medium";
        } else {
            this.utilizationStatus = "Low";
        }
    }

    // Getters and Setters
    public Integer getVehicleId() { return vehicleId; }
    public void setVehicleId(Integer vehicleId) { this.vehicleId = vehicleId; }

    public String getVehicleName() { return vehicleName; }
    public void setVehicleName(String vehicleName) { this.vehicleName = vehicleName; }

    public String getVehicleImage() { return vehicleImage; }
    public void setVehicleImage(String vehicleImage) { this.vehicleImage = vehicleImage; }

    public String getRegistrationNumber() { return registrationNumber; }
    public void setRegistrationNumber(String registrationNumber) { this.registrationNumber = registrationNumber; }

    public Integer getTotalBookings() { return totalBookings; }
    public void setTotalBookings(Integer totalBookings) { this.totalBookings = totalBookings; }

    public Integer getCompletedBookings() { return completedBookings; }
    public void setCompletedBookings(Integer completedBookings) { this.completedBookings = completedBookings; }

    public Integer getActiveBookings() { return activeBookings; }
    public void setActiveBookings(Integer activeBookings) { this.activeBookings = activeBookings; }

    public Integer getCancelledBookings() { return cancelledBookings; }
    public void setCancelledBookings(Integer cancelledBookings) { this.cancelledBookings = cancelledBookings; }

    public BigDecimal getTotalRevenue() { return totalRevenue; }
    public void setTotalRevenue(BigDecimal totalRevenue) { this.totalRevenue = totalRevenue; }

    public LocalDateTime getLastBookingDate() { return lastBookingDate; }
    public void setLastBookingDate(LocalDateTime lastBookingDate) { this.lastBookingDate = lastBookingDate; }

    public String getMostFrequentCustomer() { return mostFrequentCustomer; }
    public void setMostFrequentCustomer(String mostFrequentCustomer) { this.mostFrequentCustomer = mostFrequentCustomer; }

    public Double getAverageBookingDuration() { return averageBookingDuration; }
    public void setAverageBookingDuration(Double averageBookingDuration) { this.averageBookingDuration = averageBookingDuration; }

    public String getUtilizationStatus() { return utilizationStatus; }
    public void setUtilizationStatus(String utilizationStatus) { this.utilizationStatus = utilizationStatus; }
}