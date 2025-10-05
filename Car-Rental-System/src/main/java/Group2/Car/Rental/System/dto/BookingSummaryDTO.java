package Group2.Car.Rental.System.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class BookingSummaryDTO {
    private Integer bookingId;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String bookingStatus;
    private BigDecimal totalCost;
    private String bookingType; // VEHICLE or PACKAGE

    // Vehicle-specific fields
    private Integer vehicleId;
    private String vehicleMake;
    private String vehicleModel;
    private String registrationNumber;
    private String imageUrl;

    // Package-specific fields
    private Integer packageId;
    private String packageName;
    private String packageImageUrl;
    private Integer packageDuration;
    private List<VehicleInfo> packageVehicles;

    // Inner class for package vehicle information
    public static class VehicleInfo {
        private Integer vehicleId;
        private String make;
        private String model;
        private String registrationNumber;
        private String imageUrl;

        public VehicleInfo() {}

        public VehicleInfo(Integer vehicleId, String make, String model, String registrationNumber, String imageUrl) {
            this.vehicleId = vehicleId;
            this.make = make;
            this.model = model;
            this.registrationNumber = registrationNumber;
            this.imageUrl = imageUrl;
        }

        // Getters and setters
        public Integer getVehicleId() { return vehicleId; }
        public void setVehicleId(Integer vehicleId) { this.vehicleId = vehicleId; }
        public String getMake() { return make; }
        public void setMake(String make) { this.make = make; }
        public String getModel() { return model; }
        public void setModel(String model) { this.model = model; }
        public String getRegistrationNumber() { return registrationNumber; }
        public void setRegistrationNumber(String registrationNumber) { this.registrationNumber = registrationNumber; }
        public String getImageUrl() { return imageUrl; }
        public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    }

    public BookingSummaryDTO() {}

    // Constructor for vehicle bookings
    public BookingSummaryDTO(Integer bookingId, LocalDateTime startDate, LocalDateTime endDate, String bookingStatus,
                             BigDecimal totalCost, Integer vehicleId, String vehicleMake, String vehicleModel,
                             String registrationNumber, String imageUrl) {
        this.bookingId = bookingId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.bookingStatus = bookingStatus;
        this.totalCost = totalCost;
        this.bookingType = "VEHICLE";
        this.vehicleId = vehicleId;
        this.vehicleMake = vehicleMake;
        this.vehicleModel = vehicleModel;
        this.registrationNumber = registrationNumber;
        this.imageUrl = imageUrl;
    }

    // Constructor for package bookings
    public BookingSummaryDTO(Integer bookingId, LocalDateTime startDate, LocalDateTime endDate, String bookingStatus,
                             BigDecimal totalCost, Integer packageId, String packageName, String packageImageUrl,
                             Integer packageDuration, List<VehicleInfo> packageVehicles) {
        this.bookingId = bookingId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.bookingStatus = bookingStatus;
        this.totalCost = totalCost;
        this.bookingType = "PACKAGE";
        this.packageId = packageId;
        this.packageName = packageName;
        this.packageImageUrl = packageImageUrl;
        this.packageDuration = packageDuration;
        this.packageVehicles = packageVehicles;
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
    public String getBookingType() { return bookingType; }
    public void setBookingType(String bookingType) { this.bookingType = bookingType; }

    public Integer getVehicleId() { return vehicleId; }
    public void setVehicleId(Integer vehicleId) { this.vehicleId = vehicleId; }
    public String getVehicleMake() { return vehicleMake; }
    public void setVehicleMake(String vehicleMake) { this.vehicleMake = vehicleMake; }
    public String getVehicleModel() { return vehicleModel; }
    public void setVehicleModel(String vehicleModel) { this.vehicleModel = vehicleModel; }
    public String getRegistrationNumber() { return registrationNumber; }
    public void setRegistrationNumber(String registrationNumber) { this.registrationNumber = registrationNumber; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public Integer getPackageId() { return packageId; }
    public void setPackageId(Integer packageId) { this.packageId = packageId; }
    public String getPackageName() { return packageName; }
    public void setPackageName(String packageName) { this.packageName = packageName; }
    public String getPackageImageUrl() { return packageImageUrl; }
    public void setPackageImageUrl(String packageImageUrl) { this.packageImageUrl = packageImageUrl; }
    public Integer getPackageDuration() { return packageDuration; }
    public void setPackageDuration(Integer packageDuration) { this.packageDuration = packageDuration; }
    public List<VehicleInfo> getPackageVehicles() { return packageVehicles; }
    public void setPackageVehicles(List<VehicleInfo> packageVehicles) { this.packageVehicles = packageVehicles; }
}
