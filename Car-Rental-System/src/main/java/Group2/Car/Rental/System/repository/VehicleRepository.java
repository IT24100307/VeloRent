package Group2.Car.Rental.System.repository;

import Group2.Car.Rental.System.entity.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Integer> {
    // Recent vehicles by creation timestamp
    List<Vehicle> findTop10ByOrderByCreatedAtDesc();

    // Fallback if createdAt is null or not populated
    List<Vehicle> findTop10ByOrderByVehicleIdDesc();
}
