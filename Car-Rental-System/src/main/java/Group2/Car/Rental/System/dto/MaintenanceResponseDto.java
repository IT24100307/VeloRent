package Group2.Car.Rental.System.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class MaintenanceResponseDto {
    private Integer maintenanceId;
    private Integer vehicleId;
    private String vehicleMake;
    private String vehicleModel;
    private Integer vehicleYear;
    private String vehicleRegistrationNumber;
    private LocalDate maintenanceDate;
    private String description;
    private BigDecimal cost;
    private String status; // "Scheduled", "Completed", "Overdue"
    
    public MaintenanceResponseDto() {}
    
    public MaintenanceResponseDto(Integer maintenanceId, Integer vehicleId, String vehicleMake, 
                                String vehicleModel, Integer vehicleYear, String vehicleRegistrationNumber,
                                LocalDate maintenanceDate, String description, BigDecimal cost) {
        this.maintenanceId = maintenanceId;
        this.vehicleId = vehicleId;
        this.vehicleMake = vehicleMake;
        this.vehicleModel = vehicleModel;
        this.vehicleYear = vehicleYear;
        this.vehicleRegistrationNumber = vehicleRegistrationNumber;
        this.maintenanceDate = maintenanceDate;
        this.description = description;
        this.cost = cost;
        this.status = determineStatus(maintenanceDate, cost);
    }
    
    private String determineStatus(LocalDate maintenanceDate, BigDecimal cost) {
        if (cost != null) {
            return "Completed";
        } else if (maintenanceDate.isBefore(LocalDate.now())) {
            return "Overdue";
        } else {
            return "Scheduled";
        }
    }
    
    // Getters and Setters
    public Integer getMaintenanceId() { return maintenanceId; }
    public void setMaintenanceId(Integer maintenanceId) { this.maintenanceId = maintenanceId; }
    
    public Integer getVehicleId() { return vehicleId; }
    public void setVehicleId(Integer vehicleId) { this.vehicleId = vehicleId; }
    
    public String getVehicleMake() { return vehicleMake; }
    public void setVehicleMake(String vehicleMake) { this.vehicleMake = vehicleMake; }
    
    public String getVehicleModel() { return vehicleModel; }
    public void setVehicleModel(String vehicleModel) { this.vehicleModel = vehicleModel; }
    
    public Integer getVehicleYear() { return vehicleYear; }
    public void setVehicleYear(Integer vehicleYear) { this.vehicleYear = vehicleYear; }
    
    public String getVehicleRegistrationNumber() { return vehicleRegistrationNumber; }
    public void setVehicleRegistrationNumber(String vehicleRegistrationNumber) { this.vehicleRegistrationNumber = vehicleRegistrationNumber; }
    
    public LocalDate getMaintenanceDate() { return maintenanceDate; }
    public void setMaintenanceDate(LocalDate maintenanceDate) { 
        this.maintenanceDate = maintenanceDate;
        this.status = determineStatus(maintenanceDate, this.cost);
    }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public BigDecimal getCost() { return cost; }
    public void setCost(BigDecimal cost) { 
        this.cost = cost;
        this.status = determineStatus(this.maintenanceDate, cost);
    }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}