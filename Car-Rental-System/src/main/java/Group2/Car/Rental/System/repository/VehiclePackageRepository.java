package Group2.Car.Rental.System.repository;

import Group2.Car.Rental.System.entity.VehiclePackage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface VehiclePackageRepository extends JpaRepository<VehiclePackage, Integer> {
    List<VehiclePackage> findByStatus(String status);
    
    // Find all packages containing a specific vehicle
    @Query("SELECT vp FROM VehiclePackage vp JOIN vp.vehicles v WHERE v.vehicleId = :vehicleId")
    List<VehiclePackage> findPackagesContainingVehicle(@Param("vehicleId") Integer vehicleId);
    
    // Find all activated packages containing a specific vehicle
    @Query("SELECT vp FROM VehiclePackage vp JOIN vp.vehicles v WHERE v.vehicleId = :vehicleId AND vp.status = 'Activated'")
    List<VehiclePackage> findActivatedPackagesContainingVehicle(@Param("vehicleId") Integer vehicleId);
}

