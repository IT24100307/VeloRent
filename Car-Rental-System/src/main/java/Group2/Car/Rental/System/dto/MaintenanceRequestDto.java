package Group2.Car.Rental.System.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class MaintenanceRequestDto {
    private Integer vehicleId;
    private LocalDate maintenanceDate;
    private String description;
    private BigDecimal cost; // Optional for scheduling (null = scheduled, not null = completed)
    
    public MaintenanceRequestDto() {}
    
    public MaintenanceRequestDto(Integer vehicleId, LocalDate maintenanceDate, String description, BigDecimal cost) {
        this.vehicleId = vehicleId;
        this.maintenanceDate = maintenanceDate;
        this.description = description;
        this.cost = cost;
    }
    
    // Getters and Setters
    public Integer getVehicleId() { return vehicleId; }
    public void setVehicleId(Integer vehicleId) { this.vehicleId = vehicleId; }
    
    public LocalDate getMaintenanceDate() { return maintenanceDate; }
    public void setMaintenanceDate(LocalDate maintenanceDate) { this.maintenanceDate = maintenanceDate; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public BigDecimal getCost() { return cost; }
    public void setCost(BigDecimal cost) { this.cost = cost; }
}