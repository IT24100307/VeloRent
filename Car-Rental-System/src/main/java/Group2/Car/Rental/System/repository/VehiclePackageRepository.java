package Group2.Car.Rental.System.repository;

import Group2.Car.Rental.System.entity.VehiclePackage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VehiclePackageRepository extends JpaRepository<VehiclePackage, Integer> {
    List<VehiclePackage> findByStatus(String status);
}

