package Group2.Car.Rental.System.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "vehicle_packages")
@Data
public class VehiclePackage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "package_id")
    private Integer packageId;

    @Column(name = "package_name", nullable = false, length = 100)
    private String packageName;

    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "status", length = 30)
    private String status = "Activated"; // Activated, Deactivated, or Temporarily Unavailable

    @Column(name = "duration", nullable = false)
    private Integer duration; // in days

    @Column(name = "image_url", length = 255)
    private String imageUrl;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "vehicle_package_vehicles",
            joinColumns = @JoinColumn(name = "package_id"),
            inverseJoinColumns = @JoinColumn(name = "vehicle_id"),
            uniqueConstraints = @UniqueConstraint(columnNames = {"package_id", "vehicle_id"})
    )
    private Set<Vehicle> vehicles = new HashSet<>();

    // ...existing code...
}
