package Group2.Car.Rental.System.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "maintenance_records")
@Data
public class MaintenanceRecord {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "maintenance_id")
    private Integer maintenanceId;
    
    @Column(name = "maintenance_date", nullable = false)
    private LocalDate maintenanceDate;
    
    @Column(name = "description", nullable = false, length = 255)
    private String description;
    
    @Column(name = "cost", precision = 10, scale = 2)
    private BigDecimal cost;
    
    @Column(name = "vehicle_id", nullable = false)
    private Integer vehicleId;
    
    // Add reference to Vehicle entity
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id", insertable = false, updatable = false)
    private Vehicle vehicle;
    
    // Constructors
    public MaintenanceRecord() {}
    
    public MaintenanceRecord(LocalDate maintenanceDate, String description, BigDecimal cost, Integer vehicleId) {
        this.maintenanceDate = maintenanceDate;
        this.description = description;
        this.cost = cost;
        this.vehicleId = vehicleId;
    }
    
    // Getters and Setters
    public Integer getMaintenanceId() { return maintenanceId; }
    public void setMaintenanceId(Integer maintenanceId) { this.maintenanceId = maintenanceId; }
    
    public LocalDate getMaintenanceDate() { return maintenanceDate; }
    public void setMaintenanceDate(LocalDate maintenanceDate) { this.maintenanceDate = maintenanceDate; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public BigDecimal getCost() { return cost; }
    public void setCost(BigDecimal cost) { this.cost = cost; }
    
    public Integer getVehicleId() { return vehicleId; }
    public void setVehicleId(Integer vehicleId) { this.vehicleId = vehicleId; }
    
    public Vehicle getVehicle() { return vehicle; }
    public void setVehicle(Vehicle vehicle) { this.vehicle = vehicle; }
}